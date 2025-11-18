package com.rovits.poisyncservice.util

import com.rovits.poisyncservice.dto.response.ApiResponse
import com.rovits.poisyncservice.dto.response.ErrorDetail
import com.rovits.poisyncservice.dto.response.FieldError
import com.rovits.poisyncservice.dto.response.ValidationErrorResponse
import com.rovits.poisyncservice.exception.ErrorCodes
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

/**
 * Utility class for building standardized API responses.
 * Provides convenient methods for creating success and error responses.
 */
object ResponseHelper {

    /**
     * Creates a successful response with data
     *
     * Example:
     * ```kotlin
     * return ResponseHelper.ok(userDto)
     * ```
     */
    fun <T> ok(data: T): ResponseEntity<ApiResponse<T>> {
        return ResponseEntity.ok(ApiResponse.success(data))
    }

    /**
     * Creates a successful response without data (200 OK)
     *
     * Example:
     * ```kotlin
     * return ResponseHelper.ok()
     * ```
     */
    fun ok(): ResponseEntity<ApiResponse<Unit>> {
        return ResponseEntity.ok(ApiResponse.success())
    }

    /**
     * Creates a successful created response (201 Created)
     *
     * Example:
     * ```kotlin
     * return ResponseHelper.created(newUser)
     * ```
     */
    fun <T> created(data: T): ResponseEntity<ApiResponse<T>> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(data))
    }

    /**
     * Creates a successful accepted response (202 Accepted)
     * Used for asynchronous operations
     *
     * Example:
     * ```kotlin
     * return ResponseHelper.accepted("Synchronization started")
     * ```
     */
    fun <T> accepted(data: T): ResponseEntity<ApiResponse<T>> {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .body(ApiResponse.success(data))
    }

    /**
     * Creates a successful no content response (204 No Content)
     *
     * Example:
     * ```kotlin
     * return ResponseHelper.noContent()
     * ```
     */
    fun noContent(): ResponseEntity<Void> {
        return ResponseEntity.noContent().build()
    }

    /**
     * Creates an error response with custom status
     *
     * Example:
     * ```kotlin
     * return ResponseHelper.error(
     *     ErrorDetail.of("USER_001", "User not found"),
     *     HttpStatus.NOT_FOUND
     * )
     * ```
     */
    fun <T> error(
        errorDetail: ErrorDetail,
        status: HttpStatus
    ): ResponseEntity<ApiResponse<T>> {
        return ResponseEntity.status(status)
            .body(ApiResponse.error(errorDetail))
    }

    /**
     * Creates a bad request error response (400)
     *
     * Example:
     * ```kotlin
     * return ResponseHelper.badRequest(
     *     ErrorDetail.of("VAL_001", "Invalid input")
     * )
     * ```
     */
    fun <T> badRequest(errorDetail: ErrorDetail): ResponseEntity<ApiResponse<T>> {
        return error(errorDetail, HttpStatus.BAD_REQUEST)
    }

    /**
     * Creates an unauthorized error response (401)
     *
     * Example:
     * ```kotlin
     * return ResponseHelper.unauthorized(
     *     ErrorDetail.of("AUTH_001", "Invalid credentials")
     * )
     * ```
     */
    fun <T> unauthorized(errorDetail: ErrorDetail): ResponseEntity<ApiResponse<T>> {
        return error(errorDetail, HttpStatus.UNAUTHORIZED)
    }

    /**
     * Creates a forbidden error response (403)
     *
     * Example:
     * ```kotlin
     * return ResponseHelper.forbidden(
     *     ErrorDetail.of("AUTH_004", "Access denied")
     * )
     * ```
     */
    fun <T> forbidden(errorDetail: ErrorDetail): ResponseEntity<ApiResponse<T>> {
        return error(errorDetail, HttpStatus.FORBIDDEN)
    }

    /**
     * Creates a not found error response (404)
     *
     * Example:
     * ```kotlin
     * return ResponseHelper.notFound(
     *     ErrorDetail.of("USER_001", "User not found")
     * )
     * ```
     */
    fun <T> notFound(errorDetail: ErrorDetail): ResponseEntity<ApiResponse<T>> {
        return error(errorDetail, HttpStatus.NOT_FOUND)
    }

    /**
     * Creates a conflict error response (409)
     *
     * Example:
     * ```kotlin
     * return ResponseHelper.conflict(
     *     ErrorDetail.of("USER_002", "User already exists")
     * )
     * ```
     */
    fun <T> conflict(errorDetail: ErrorDetail): ResponseEntity<ApiResponse<T>> {
        return error(errorDetail, HttpStatus.CONFLICT)
    }

    /**
     * Creates an internal server error response (500)
     *
     * Example:
     * ```kotlin
     * return ResponseHelper.internalServerError(
     *     ErrorDetail.of("SYS_001", "Internal error")
     * )
     * ```
     */
    fun <T> internalServerError(errorDetail: ErrorDetail): ResponseEntity<ApiResponse<T>> {
        return error(errorDetail, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    /**
     * Creates a service unavailable error response (503)
     *
     * Example:
     * ```kotlin
     * return ResponseHelper.serviceUnavailable(
     *     ErrorDetail.of("EXT_001", "Google API unavailable")
     * )
     * ```
     */
    fun <T> serviceUnavailable(errorDetail: ErrorDetail): ResponseEntity<ApiResponse<T>> {
        return error(errorDetail, HttpStatus.SERVICE_UNAVAILABLE)
    }

    /**
     * Creates a validation error response
     *
     * Example:
     * ```kotlin
     * return ResponseHelper.validationError(
     *     code = "VAL_001",
     *     message = "Validation failed",
     *     errors = listOf(
     *         FieldError("email", "Invalid format", "test")
     *     )
     * )
     * ```
     */
    fun validationError(
        code: String = ErrorCodes.VALIDATION_FAILED,
        message: String,
        errors: List<FieldError>
    ): ResponseEntity<ValidationErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                ValidationErrorResponse(
                    code = code,
                    message = message,
                    errors = errors
                )
            )
    }
}