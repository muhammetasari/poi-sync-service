package com.rovits.poisyncservice.dto.response

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Detailed error information for API responses.
 * Used in ApiResponse when an error occurs.
 *
 * Example:
 * ```json
 * {
 *   "code": "USER_001",
 *   "message": "User not found with email: john@example.com",
 *   "field": "email",
 *   "details": {
 *     "requestId": "abc-123",
 *     "path": "/api/users/john@example.com"
 *   }
 * }
 * ```
 *
 * @property code Unique error code (e.g., "USER_001", "VAL_002")
 * @property message Localized error message based on Accept-Language header
 * @property field Field name that caused the error (for validation errors)
 * @property details Additional error context (optional)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorDetail(
    val code: String,
    val message: String,
    val field: String? = null,
    val details: Map<String, Any>? = null
) {
    companion object {
        /**
         * Creates a simple error detail with code and message
         */
        fun of(code: String, message: String): ErrorDetail {
            return ErrorDetail(
                code = code,
                message = message
            )
        }

        /**
         * Creates an error detail with field information
         */
        fun withField(code: String, message: String, field: String): ErrorDetail {
            return ErrorDetail(
                code = code,
                message = message,
                field = field
            )
        }

        /**
         * Creates an error detail with additional context
         */
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
    }
}