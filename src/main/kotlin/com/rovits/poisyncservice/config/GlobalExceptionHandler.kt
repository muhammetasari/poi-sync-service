package com.rovits.poisyncservice.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.rovits.poisyncservice.dto.response.ApiResponse
import com.rovits.poisyncservice.dto.response.ErrorDetail
import com.rovits.poisyncservice.dto.response.FieldError
import com.rovits.poisyncservice.exception.*
import com.rovits.poisyncservice.util.MessageKeys
import com.rovits.poisyncservice.util.MessageResolver
import com.rovits.poisyncservice.util.ResponseHelper
import jakarta.servlet.http.HttpServletRequest
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
import java.net.URI

@RestControllerAdvice
class GlobalExceptionHandler(
    private val messageResolver: MessageResolver,
    private val objectMapper: ObjectMapper,
    @Value("\${spring.profiles.active:dev}") private val activeProfile: String
) {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    private val isDevelopment: Boolean
        get() = activeProfile.contains("dev") || activeProfile.contains("local")

    /**
     * Convert Map to JsonNode for ErrorDetail.details field
     */
    private fun mapToJsonNode(map: Map<String, Any?>): com.fasterxml.jackson.databind.JsonNode {
        return objectMapper.valueToTree(map)
    }

    // ========================================
    // CUSTOM EXCEPTIONS
    // ========================================

    @ExceptionHandler(BaseException::class)
    fun handleBaseException(ex: BaseException, request: HttpServletRequest): ResponseEntity<ApiResponse<Nothing>> {
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
            ).copy(instance = request.requestURI, type = URI.create("urn:problem-type:validation"))
            else -> ErrorDetail.of(
                code = ex.errorCode,
                message = localizedMessage
            ).copy(instance = request.requestURI, type = URI.create("urn:problem-type:business"))
        }

        val httpStatus = when (ex) {
            is ResourceNotFoundException -> HttpStatus.NOT_FOUND
            is AuthenticationException -> HttpStatus.UNAUTHORIZED
            is AuthorizationException -> HttpStatus.FORBIDDEN
            is ValidationException -> HttpStatus.BAD_REQUEST
            is BusinessException -> HttpStatus.CONFLICT
            is ExternalServiceException -> HttpStatus.SERVICE_UNAVAILABLE
            is CacheException -> HttpStatus.INTERNAL_SERVER_ERROR
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }

        return ResponseHelper.error(errorDetail, httpStatus)
    }

    // ========================================
    // SPRING VALIDATION ERRORS
    // ========================================

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Validation failed: {} field errors", ex.bindingResult.errorCount)

        val fieldErrors = ex.bindingResult.fieldErrors.map { fieldError ->
            val localizedMessage = messageResolver.resolveOrDefault(
                messageKey = fieldError.defaultMessage ?: MessageKeys.VALIDATION_FAILED,
                defaultMessage = fieldError.defaultMessage ?: "Validation failed",
                fieldError.rejectedValue ?: ""
            )

            FieldError(
                field = fieldError.field,
                message = localizedMessage,
                rejectedValue = fieldError.rejectedValue
            )
        }

        val generalMessage = messageResolver.resolve(MessageKeys.VALIDATION_FAILED)

        return ResponseHelper.validationError(
            code = ErrorCodes.VALIDATION_FAILED,
            message = generalMessage,
            errors = fieldErrors
        )
    }

    @ExceptionHandler(BindException::class)
    fun handleBindException(ex: BindException): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Bind exception occurred: {} field errors", ex.bindingResult.errorCount)

        val fieldErrors = ex.bindingResult.fieldErrors.map { fieldError ->
            val localizedMessage = messageResolver.resolveOrDefault(
                messageKey = fieldError.defaultMessage ?: MessageKeys.VALIDATION_FAILED,
                defaultMessage = "Invalid value",
                fieldError.rejectedValue ?: ""
            )

            FieldError(
                field = fieldError.field,
                message = localizedMessage,
                rejectedValue = fieldError.rejectedValue
            )
        }

        return ResponseHelper.validationError(
            code = ErrorCodes.VALIDATION_FAILED,
            message = messageResolver.resolve(MessageKeys.VALIDATION_FAILED),
            errors = fieldErrors
        )
    }

    // ========================================
    // REQUEST PARAMETER ERRORS
    // ========================================

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParameter(
        ex: MissingServletRequestParameterException
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Missing request parameter: {}", ex.parameterName)

        val message = messageResolver.resolve(
            MessageKeys.VALIDATION_REQUIRED,
            ex.parameterName
        )

        val errorDetail = ErrorDetail.withField(
            code = ErrorCodes.FIELD_REQUIRED,
            message = message,
            field = ex.parameterName
        ).copy(type = URI.create("urn:problem-type:validation"))

        return ResponseHelper.badRequest(errorDetail)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(
        ex: MethodArgumentTypeMismatchException
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Type mismatch for parameter: {}", ex.name)

        val message = messageResolver.resolve(
            MessageKeys.VALIDATION_TYPE_MISMATCH,
            ex.name,
            ex.requiredType?.simpleName ?: "Unknown"
        )

        val errorDetail = ErrorDetail.withField(
            code = ErrorCodes.VALIDATION_FAILED,
            message = message,
            field = ex.name
        ).copy(type = URI.create("urn:problem-type:validation"))

        return ResponseHelper.badRequest(errorDetail)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Malformed JSON request: {}", ex.message)

        val message = messageResolver.resolve(MessageKeys.VALIDATION_JSON_MALFORMED)

        val errorDetail = ErrorDetail.of(
            code = ErrorCodes.VALIDATION_FAILED,
            message = message
        ).copy(type = URI.create("urn:problem-type:validation"))

        return ResponseHelper.badRequest(errorDetail)
    }

    // ========================================
    // SECURITY EXCEPTIONS
    // ========================================

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Access denied: {}", ex.message)

        val message = messageResolver.resolve(MessageKeys.ACCESS_DENIED, "REQUIRED_PERMISSION")

        val errorDetail = ErrorDetail.of(
            code = ErrorCodes.ACCESS_DENIED,
            message = message
        )

        return ResponseHelper.forbidden(errorDetail)
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException::class)
    fun handleAuthenticationException(
        ex: org.springframework.security.core.AuthenticationException
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Authentication failed: {}", ex.message)

        val message = messageResolver.resolve(MessageKeys.UNAUTHORIZED)

        val errorDetail = ErrorDetail.of(
            code = ErrorCodes.UNAUTHORIZED,
            message = message
        )

        return ResponseHelper.unauthorized(errorDetail)
    }

    // ========================================
    // DATABASE EXCEPTIONS
    // ========================================

    @ExceptionHandler(DuplicateKeyException::class)
    fun handleDuplicateKey(ex: DuplicateKeyException): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Duplicate key error: {}", ex.message)

        val message = messageResolver.resolve(MessageKeys.DATABASE_DUPLICATE_KEY)

        val errorDetail = ErrorDetail.of(
            code = ErrorCodes.DUPLICATE_KEY_ERROR,
            message = message
        )

        return ResponseHelper.conflict(errorDetail)
    }

    @ExceptionHandler(DataAccessException::class)
    fun handleDataAccessException(ex: DataAccessException): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Database error occurred", ex)

        val message = messageResolver.resolve(MessageKeys.DATABASE_FAILED)

        val errorDetail = if (isDevelopment) {
            ErrorDetail.withDetails(
                code = ErrorCodes.DATABASE_ERROR,
                message = message,
                details = mapToJsonNode(mapOf(
                    "exceptionType" to ex::class.simpleName!!,
                    "cause" to (ex.cause?.message ?: "Unknown")
                ))
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

    @ExceptionHandler(WebClientResponseException::class)
    fun handleWebClientResponseException(
        ex: WebClientResponseException
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("External API error: status={}, body={}", ex.statusCode, ex.responseBodyAsString)

        val message = messageResolver.resolve(MessageKeys.GOOGLE_API_FAILED)

        val errorDetail = if (isDevelopment) {
            ErrorDetail.withDetails(
                code = ErrorCodes.GOOGLE_API_ERROR,
                message = message,
                details = mapToJsonNode(mapOf(
                    "status" to ex.statusCode.value(),
                    "response" to ex.responseBodyAsString
                ))
            )
        } else {
            ErrorDetail.of(
                code = ErrorCodes.GOOGLE_API_ERROR,
                message = message
            )
        }

        return ResponseHelper.serviceUnavailable(errorDetail)
    }

    @ExceptionHandler(WebClientException::class)
    fun handleWebClientException(ex: WebClientException): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("External service connection error", ex)

        val message = messageResolver.resolve(MessageKeys.GOOGLE_API_UNAVAILABLE)

        val errorDetail = ErrorDetail.of(
            code = ErrorCodes.GOOGLE_API_UNAVAILABLE,
            message = message
        )

        return ResponseHelper.serviceUnavailable(errorDetail)
    }

    // ========================================
    // GENERIC EXCEPTION (CATCH-ALL)
    // ========================================

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception, request: HttpServletRequest): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Unexpected error occurred", ex)

        val message = messageResolver.resolve(MessageKeys.INTERNAL_SERVER_ERROR)

        val errorDetail = if (isDevelopment) {
            ErrorDetail.withDetails(
                code = ErrorCodes.INTERNAL_SERVER_ERROR,
                message = message,
                details = mapToJsonNode(mapOf(
                    "exceptionType" to ex::class.simpleName!!,
                    "message" to (ex.message ?: "No message"),
                    "stackTrace" to ex.stackTrace.take(5).map { it.toString() }
                ))
            )
        } else {
            ErrorDetail.of(
                code = ErrorCodes.INTERNAL_SERVER_ERROR,
                message = message
            )
        }

        val finalError = errorDetail.copy(instance = request.requestURI, type = URI.create("urn:problem-type:server-error"))

        return ResponseHelper.internalServerError(finalError)
    }
}