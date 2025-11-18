package com.rovits.poisyncservice.config

import com.rovits.poisyncservice.dto.response.ApiResponse
import com.rovits.poisyncservice.dto.response.ErrorDetail
import com.rovits.poisyncservice.dto.response.FieldError
import com.rovits.poisyncservice.dto.response.ValidationErrorResponse
import com.rovits.poisyncservice.exception.*
import com.rovits.poisyncservice.util.MessageResolver
import com.rovits.poisyncservice.util.ResponseHelper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.dao.DataAccessException
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.reactive.function.client.WebClientException
import org.springframework.web.reactive.function.client.WebClientResponseException

/**
 * Global exception handler for the entire application.
 * Catches all exceptions and converts them to standardized API responses with i18n support.
 *
 * Features:
 * - i18n message resolution based on Accept-Language header
 * - Standardized error response format
 * - Proper HTTP status codes
 * - Detailed logging with correlation ID
 * - Environment-aware responses (dev vs prod)
 */
@RestControllerAdvice
class GlobalExceptionHandler(
    private val messageResolver: MessageResolver,
    @Value("\${spring.profiles.active:dev}") private val activeProfile: String
) {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    private val isDevelopment: Boolean
        get() = activeProfile.contains("dev") || activeProfile.contains("local")

    // ========================================
    // CUSTOM EXCEPTIONS
    // ========================================

    /**
     * Handles all custom exceptions (BaseException and its subclasses)
     * Resolves i18n messages based on messageKey and messageArgs
     */
    @ExceptionHandler(BaseException::class)
    fun handleBaseException(ex: BaseException): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Business exception occurred: errorCode={}, messageKey={}", ex.errorCode, ex.messageKey, ex)

        val localizedMessage = if (ex.messageArgs != null) {
            messageResolver.resolve(ex.messageKey, *ex.messageArgs)
        } else {
            messageResolver.resolve(ex.messageKey)
        }

        val errorDetail = when (ex) {
            is ValidationException -> ErrorDetail.withField(
                code = ex.errorCode,
                message = localizedMessage,
                field = ex.fieldName ?: "unknown"
            )
            else -> ErrorDetail.of(
                code = ex.errorCode,
                message = localizedMessage
            )
        }

        return ResponseHelper.error(errorDetail, ex.httpStatus)
    }

    // ========================================
    // SPRING VALIDATION ERRORS
    // ========================================

    /**
     * Handles Bean Validation errors (@Valid annotation)
     * Returns multiple field errors with localized messages
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException
    ): ResponseEntity<ValidationErrorResponse> {
        logger.warn("Validation failed: {} field errors", ex.bindingResult.errorCount)

        val fieldErrors = ex.bindingResult.fieldErrors.map { fieldError ->
            val localizedMessage = messageResolver.resolveOrDefault(
                messageKey = fieldError.defaultMessage ?: "error.validation.failed",
                defaultMessage = fieldError.defaultMessage ?: "Validation failed",
                fieldError.rejectedValue ?: ""
            )

            FieldError(
                field = fieldError.field,
                message = localizedMessage,
                rejectedValue = fieldError.rejectedValue
            )
        }

        val generalMessage = messageResolver.resolve("error.validation.failed")

        return ResponseHelper.validationError(
            code = ErrorCodes.VALIDATION_FAILED,
            message = generalMessage,
            errors = fieldErrors
        )
    }

    /**
     * Handles BindException (form data binding errors)
     */
    @ExceptionHandler(BindException::class)
    fun handleBindException(ex: BindException): ResponseEntity<ValidationErrorResponse> {
        logger.warn("Bind exception occurred: {} field errors", ex.bindingResult.errorCount)

        val fieldErrors = ex.bindingResult.fieldErrors.map { fieldError ->
            FieldError(
                field = fieldError.field,
                message = fieldError.defaultMessage ?: "Invalid value",
                rejectedValue = fieldError.rejectedValue
            )
        }

        return ResponseHelper.validationError(
            code = ErrorCodes.VALIDATION_FAILED,
            message = messageResolver.resolve("error.validation.failed"),
            errors = fieldErrors
        )
    }

    // ========================================
    // REQUEST PARAMETER ERRORS
    // ========================================

    /**
     * Handles missing required request parameters
     */
    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParameter(
        ex: MissingServletRequestParameterException
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Missing request parameter: {}", ex.parameterName)

        val message = messageResolver.resolve(
            "error.validation.required",
            ex.parameterName
        )

        val errorDetail = ErrorDetail.withField(
            code = ErrorCodes.FIELD_REQUIRED,
            message = message,
            field = ex.parameterName
        )

        return ResponseHelper.badRequest(errorDetail)
    }

    /**
     * Handles type mismatch in request parameters
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(
        ex: MethodArgumentTypeMismatchException
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Type mismatch for parameter: {}", ex.name)

        val message = "Invalid value for parameter '${ex.name}'. Expected type: ${ex.requiredType?.simpleName}"

        val errorDetail = ErrorDetail.withField(
            code = ErrorCodes.VALIDATION_FAILED,
            message = message,
            field = ex.name
        )

        return ResponseHelper.badRequest(errorDetail)
    }

    /**
     * Handles malformed JSON in request body
     */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Malformed JSON request: {}", ex.message)

        val message = "Malformed JSON request body"

        val errorDetail = ErrorDetail.of(
            code = ErrorCodes.VALIDATION_FAILED,
            message = message
        )

        return ResponseHelper.badRequest(errorDetail)
    }

    // ========================================
    // SECURITY EXCEPTIONS
    // ========================================

    /**
     * Handles Spring Security access denied errors
     */
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Access denied: {}", ex.message)

        val message = messageResolver.resolve("error.access.denied", "REQUIRED_PERMISSION")

        val errorDetail = ErrorDetail.of(
            code = ErrorCodes.ACCESS_DENIED,
            message = message
        )

        return ResponseHelper.forbidden(errorDetail)
    }

    /**
     * Handles Spring Security authentication errors
     */
    @ExceptionHandler(org.springframework.security.core.AuthenticationException::class)
    fun handleAuthenticationException(
        ex: org.springframework.security.core.AuthenticationException
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Authentication failed: {}", ex.message)

        val message = messageResolver.resolve("error.unauthorized")

        val errorDetail = ErrorDetail.of(
            code = ErrorCodes.UNAUTHORIZED,
            message = message
        )

        return ResponseHelper.unauthorized(errorDetail)
    }

    // ========================================
    // DATABASE EXCEPTIONS
    // ========================================

    /**
     * Handles duplicate key errors from MongoDB
     */
    @ExceptionHandler(DuplicateKeyException::class)
    fun handleDuplicateKey(ex: DuplicateKeyException): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Duplicate key error: {}", ex.message)

        val message = messageResolver.resolve("error.database.duplicate.key")

        val errorDetail = ErrorDetail.of(
            code = ErrorCodes.DUPLICATE_KEY_ERROR,
            message = message
        )

        return ResponseHelper.conflict(errorDetail)
    }

    /**
     * Handles general database access errors
     */
    @ExceptionHandler(DataAccessException::class)
    fun handleDataAccessException(ex: DataAccessException): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Database error occurred", ex)

        val message = messageResolver.resolve("error.database.failed")

        val errorDetail = if (isDevelopment) {
            ErrorDetail.withDetails(
                code = ErrorCodes.DATABASE_ERROR,
                message = message,
                details = mapOf(
                    "exceptionType" to ex::class.simpleName!!,
                    "cause" to (ex.cause?.message ?: "Unknown")
                )
            )
        } else {
            ErrorDetail.of(
                code = ErrorCodes.DATABASE_ERROR,
                message = message
            )
        }

        return ResponseHelper.internalServerError(errorDetail)
    }

    // ========================================
    // EXTERNAL API EXCEPTIONS
    // ========================================

    /**
     * Handles WebClient response exceptions (4xx, 5xx from external APIs)
     */
    @ExceptionHandler(WebClientResponseException::class)
    fun handleWebClientResponseException(
        ex: WebClientResponseException
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("External API error: status={}, body={}", ex.statusCode, ex.responseBodyAsString)

        val message = messageResolver.resolve("error.google.api.failed")

        val errorDetail = if (isDevelopment) {
            ErrorDetail.withDetails(
                code = ErrorCodes.GOOGLE_API_ERROR,
                message = message,
                details = mapOf(
                    "status" to ex.statusCode.value(),
                    "response" to ex.responseBodyAsString
                )
            )
        } else {
            ErrorDetail.of(
                code = ErrorCodes.GOOGLE_API_ERROR,
                message = message
            )
        }

        return ResponseHelper.serviceUnavailable(errorDetail)
    }

    /**
     * Handles generic WebClient errors (connection timeout, etc.)
     */
    @ExceptionHandler(WebClientException::class)
    fun handleWebClientException(ex: WebClientException): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("External service connection error", ex)

        val message = messageResolver.resolve("error.external.service.timeout", "External Service")

        val errorDetail = ErrorDetail.of(
            code = ErrorCodes.EXTERNAL_SERVICE_TIMEOUT,
            message = message
        )

        return ResponseHelper.serviceUnavailable(errorDetail)
    }

    // ========================================
    // GENERIC EXCEPTION (CATCH-ALL)
    // ========================================

    /**
     * Handles all uncaught exceptions
     * This is the last line of defense
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Unexpected error occurred", ex)

        val message = messageResolver.resolve("error.internal.server")

        val errorDetail = if (isDevelopment) {
            ErrorDetail.withDetails(
                code = ErrorCodes.INTERNAL_SERVER_ERROR,
                message = message,
                details = mapOf(
                    "exceptionType" to ex::class.simpleName!!,
                    "message" to (ex.message ?: "No message"),
                    "stackTrace" to ex.stackTrace.take(5).map { it.toString() }
                )
            )
        } else {
            ErrorDetail.of(
                code = ErrorCodes.INTERNAL_SERVER_ERROR,
                message = message
            )
        }

        return ResponseHelper.internalServerError(errorDetail)
    }
}