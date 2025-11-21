package com.rovits.poisyncservice.util

import com.rovits.poisyncservice.dto.response.ApiResponse
import com.rovits.poisyncservice.dto.response.ErrorDetail
import com.rovits.poisyncservice.dto.response.FieldError
import com.rovits.poisyncservice.exception.ErrorCodes
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

object ResponseHelper {

    fun <T> ok(data: T): ResponseEntity<ApiResponse<T>> = ResponseEntity.ok(ApiResponse.success(data))
    fun ok(): ResponseEntity<ApiResponse<Unit>> = ResponseEntity.ok(ApiResponse.success(Unit))
    fun <T> created(data: T): ResponseEntity<ApiResponse<T>> = ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(data))
    fun <T> accepted(data: T): ResponseEntity<ApiResponse<T>> = ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.success(data))
    fun noContent(): ResponseEntity<Void> = ResponseEntity.noContent().build()

    // Hata yanıtları için genel metod
    fun error(errorDetail: ErrorDetail, status: HttpStatus? = null): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.status(status?.value() ?: errorDetail.status ?: HttpStatus.INTERNAL_SERVER_ERROR.value())
            .body(ApiResponse.error(errorDetail))
    }

    // Hata yanıtı için Any tipinde overload
    fun errorAny(errorDetail: ErrorDetail, status: HttpStatus? = null): ResponseEntity<ApiResponse<Any>> {
        val response: ResponseEntity<ApiResponse<Nothing>> = error(errorDetail, status)
        @Suppress("UNCHECKED_CAST")
        return response as ResponseEntity<ApiResponse<Any>>
    }

    // Kısayol metodları
    fun badRequest(errorDetail: ErrorDetail) = error(errorDetail, HttpStatus.BAD_REQUEST)
    fun unauthorized(errorDetail: ErrorDetail) = error(errorDetail, HttpStatus.UNAUTHORIZED)
    fun forbidden(errorDetail: ErrorDetail) = error(errorDetail, HttpStatus.FORBIDDEN)
    fun notFound(errorDetail: ErrorDetail) = error(errorDetail, HttpStatus.NOT_FOUND)
    fun conflict(errorDetail: ErrorDetail) = error(errorDetail, HttpStatus.CONFLICT)
    fun internalServerError(errorDetail: ErrorDetail) = error(errorDetail, HttpStatus.INTERNAL_SERVER_ERROR)
    fun serviceUnavailable(errorDetail: ErrorDetail) = error(errorDetail, HttpStatus.SERVICE_UNAVAILABLE)

    fun validationError(
        code: String = ErrorCodes.VALIDATION_FAILED,
        message: String,
        errors: List<FieldError>,
        traceId: String? = null
    ): ResponseEntity<ApiResponse<Nothing>> {
        val errorDetail = ErrorDetail.validation(
            code = code,
            message = message,
            fieldErrors = errors,
            status = HttpStatus.BAD_REQUEST.value(),
            traceId = traceId
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(errorDetail))
    }
}