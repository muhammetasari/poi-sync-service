package com.rovits.poisyncservice.dto.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.OffsetDateTime
import java.time.ZoneOffset

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorDetail? = null,
    val httpStatus: Int? = null,
    val traceId: String? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    val timestamp: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> = ApiResponse(success = true, data = data)
        fun success(): ApiResponse<Unit?> = ApiResponse(success = true, data = null)
        fun error(
            errorDetail: ErrorDetail,
            httpStatus: Int? = null,
            traceId: String? = null
        ): ApiResponse<Nothing> =
            ApiResponse(
                success = false,
                error = errorDetail,
                httpStatus = httpStatus ?: errorDetail.status,
                traceId = traceId ?: errorDetail.traceId
            )
    }
}