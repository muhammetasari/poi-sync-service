package com.rovits.poisyncservice.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import java.net.URI

/**
 * Detailed error information for API responses.
 * unified to support RFC 7807 style fields and validation errors.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorDetail(
    val code: String,
    val message: String,
    val type: URI? = null,
    val title: String? = null,
    val instance: String? = null,
    val fieldErrors: List<FieldError>? = null,
    val details: Map<String, Any>? = null
) {
    companion object {
        fun of(code: String, message: String, title: String? = null): ErrorDetail {
            return ErrorDetail(
                code = code,
                message = message,
                title = title
            )
        }

        fun validation(
            code: String,
            message: String,
            fieldErrors: List<FieldError>
        ): ErrorDetail {
            return ErrorDetail(
                code = code,
                message = message,
                title = "Validation Failed",
                fieldErrors = fieldErrors
            )
        }

        fun withDetails(
            code: String,
            message: String,
            details: Map<String, Any>
        ): ErrorDetail {
            return ErrorDetail(
                code = code,
                message = message,
                details = details
            )
        }

        fun withField(code: String, message: String, field: String): ErrorDetail {
            return ErrorDetail(
                code = code,
                message = message,
                fieldErrors = listOf(FieldError(field, message))
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