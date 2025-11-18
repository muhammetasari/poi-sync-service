package com.rovits.poisyncservice.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

/**
 * Specialized response for validation errors.
 * Used when multiple fields fail validation (Bean Validation).
 *
 * Example:
 * ```json
 * {
 *   "success": false,
 *   "code": "VAL_001",
 *   "message": "Validation failed",
 *   "errors": [
 *     {
 *       "field": "email",
 *       "message": "Invalid email format",
 *       "rejectedValue": "invalidemail"
 *     },
 *     {
 *       "field": "password",
 *       "message": "Password must be at least 8 characters",
 *       "rejectedValue": "123"
 *     }
 *   ],
 *   "timestamp": "2025-11-18T10:30:00"
 * }
 * ```
 *
 * @property success Always false for validation errors
 * @property code Error code (typically "VAL_001")
 * @property message General validation error message
 * @property errors List of field-specific validation errors
 * @property timestamp Timestamp of the response
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ValidationErrorResponse(
    val success: Boolean = false,
    val code: String,
    val message: String,
    val errors: List<FieldError>,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

/**
 * Represents a validation error for a specific field.
 *
 * @property field Name of the field that failed validation
 * @property message Localized validation error message
 * @property rejectedValue The value that was rejected (can be null)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class FieldError(
    val field: String,
    val message: String,
    val rejectedValue: Any? = null
)