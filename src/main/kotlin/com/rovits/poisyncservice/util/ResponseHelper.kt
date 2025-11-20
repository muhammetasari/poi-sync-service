package com.rovits.poisyncservice.util

import com.rovits.poisyncservice.dto.response.ApiResponse
import com.rovits.poisyncservice.dto.response.ErrorDetail
import com.rovits.poisyncservice.dto.response.FieldError
import com.rovits.poisyncservice.exception.ErrorCodes
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

object ResponseHelper {

    fun <T> ok(data: T): ResponseEntity<ApiResponse<T>> = ResponseEntity.ok(ApiResponse.success(data))
    fun ok(): ResponseEntity<ApiResponse<Unit>> = ResponseEntity.ok(ApiResponse.success())
    fun <T> created(data: T): ResponseEntity<ApiResponse<T>> = ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(data))
    fun <T> accepted(data: T): ResponseEntity<ApiResponse<T>> = ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.success(data))
    fun noContent(): ResponseEntity<Void> = ResponseEntity.noContent().build()

    // Hata yanıtları için genel metod
    fun <T> error(errorDetail: ErrorDetail, status: HttpStatus): ResponseEntity<ApiResponse<T>> {
        return ResponseEntity.status(status).body(ApiResponse.error(errorDetail))
    }

    // Kısayol metodları
    fun <T> badRequest(errorDetail: ErrorDetail) = error<T>(errorDetail, HttpStatus.BAD_REQUEST)
    fun <T> unauthorized(errorDetail: ErrorDetail) = error<T>(errorDetail, HttpStatus.UNAUTHORIZED)
    fun <T> forbidden(errorDetail: ErrorDetail) = error<T>(errorDetail, HttpStatus.FORBIDDEN)
    fun <T> notFound(errorDetail: ErrorDetail) = error<T>(errorDetail, HttpStatus.NOT_FOUND)
    fun <T> conflict(errorDetail: ErrorDetail) = error<T>(errorDetail, HttpStatus.CONFLICT)
    fun <T> internalServerError(errorDetail: ErrorDetail) = error<T>(errorDetail, HttpStatus.INTERNAL_SERVER_ERROR)
    fun <T> serviceUnavailable(errorDetail: ErrorDetail) = error<T>(errorDetail, HttpStatus.SERVICE_UNAVAILABLE)

    fun validationError(
        code: String = ErrorCodes.VALIDATION_FAILED,
        message: String,
        errors: List<FieldError>
    ): ResponseEntity<ApiResponse<Nothing>> {
        val errorDetail = ErrorDetail.validation(
            code = code,
            message = message,
            fieldErrors = errors
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(errorDetail))
    }
}