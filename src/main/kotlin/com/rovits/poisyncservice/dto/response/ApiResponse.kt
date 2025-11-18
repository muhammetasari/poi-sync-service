package com.rovits.poisyncservice.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

/**
 * Standardized API response wrapper for all endpoints.
 * Provides consistent response structure across the application.
 *
 * Success response example:
 * ```json
 * {
 *   "success": true,
 *   "data": { "id": "123", "name": "John" },
 *   "timestamp": "2025-11-18T10:30:00"
 * }
 * ```
 *
 * Error response example:
 * ```json
 * {
 *   "success": false,
 *   "error": {
 *     "code": "USER_001",
 *     "message": "User not found with email: john@example.com"
 *   },
 *   "timestamp": "2025-11-18T10:30:00"
 * }
 * ```
 *
 * @param T Type of the data payload
 * @property success Indicates if the request was successful
 * @property data Response data (null if error occurred)
 * @property error Error details (null if successful)
 * @property timestamp Timestamp of the response
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorDetail? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        /**
         * Creates a successful API response
         */
        fun <T> success(data: T): ApiResponse<T> {
            return ApiResponse(
                success = true,
                data = data,
                error = null
            )
        }

        /**
         * Creates a successful API response without data
         */
        fun success(): ApiResponse<Unit> {
            return ApiResponse(
                success = true,
                data = Unit,
                error = null
            )
        }

        /**
         * Creates an error API response
         */
        fun <T> error(errorDetail: ErrorDetail): ApiResponse<T> {
            return ApiResponse(
                success = false,
                data = null,
                error = errorDetail
            )
        }
    }
}