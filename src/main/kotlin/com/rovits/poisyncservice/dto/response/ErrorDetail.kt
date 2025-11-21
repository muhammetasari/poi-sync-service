package com.rovits.poisyncservice.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.JsonNode
import java.net.URI

/**
 * Detailed error information for API responses.
 * unified to support RFC 7807 style fields and validation errors.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorDetail(
    val type: URI? = null,
    val title: String? = null,
    val status: Int? = null, // RFC 7807 uyumlu
    val detail: String? = null, // RFC 7807 uyumlu
    val instance: String? = null,
    val traceId: String? = null,
    val fieldErrors: List<FieldError>? = null,
    val code: String,
    val message: String,
    val details: JsonNode? = null
) {
    companion object {
        fun of(code: String, message: String, title: String? = null, status: Int? = null, detail: String? = null, traceId: String? = null): ErrorDetail {
            return ErrorDetail(
                code = code,
                message = message,
                title = title,
                status = status,
                detail = detail,
                traceId = traceId
            )
        }

        fun validation(
            code: String,
            message: String,
            fieldErrors: List<FieldError>,
            status: Int? = null,
            traceId: String? = null
        ): ErrorDetail {
            return ErrorDetail(
                code = code,
                message = message,
                title = "Validation Failed",
                fieldErrors = fieldErrors,
                status = status,
                traceId = traceId
            )
        }

        fun withDetails(
            code: String,
            message: String,
            details: JsonNode,
            status: Int? = null,
            detail: String? = null,
            traceId: String? = null
        ): ErrorDetail {
            return ErrorDetail(
                code = code,
                message = message,
                details = details,
                status = status,
                detail = detail,
                traceId = traceId
            )
        }

        fun withField(code: String, message: String, field: String, status: Int? = null, traceId: String? = null): ErrorDetail {
            return ErrorDetail(
                code = code,
                message = message,
                fieldErrors = listOf(FieldError(field, message)),
                status = status,
                traceId = traceId
            )
        }
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class FieldError(
    val field: String,
    val message: String,
    val rejectedValue: Any? = null
)