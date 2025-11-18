package com.rovits.poisyncservice.example

import com.rovits.poisyncservice.dto.response.ApiResponse
import com.rovits.poisyncservice.dto.response.ErrorDetail
import com.rovits.poisyncservice.dto.response.FieldError
import com.rovits.poisyncservice.exception.ErrorCodes
import com.rovits.poisyncservice.util.ResponseHelper
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Example controller demonstrating response standardization
 */
@RestController
@RequestMapping("/api/examples")
class ResponseUsageExamples {

    // ========================================
    // SUCCESS RESPONSES
    // ========================================

    /**
     * Example 1: Simple success response with data
     * Returns: 200 OK
     */
    @GetMapping("/success-with-data")
    fun exampleSuccessWithData(): ResponseEntity<ApiResponse<UserDto>> {
        val user = UserDto(id = "123", name = "John", email = "john@example.com")
        return ResponseHelper.ok(user)
    }
    // Response:
    // {
    //   "success": true,
    //   "data": {
    //     "id": "123",
    //     "name": "John",
    //     "email": "john@example.com"
    //   },
    //   "timestamp": "2025-11-18T10:30:00"
    // }

    /**
     * Example 2: Success response without data
     * Returns: 200 OK
     */
    @DeleteMapping("/success-no-data")
    fun exampleSuccessNoData(): ResponseEntity<ApiResponse<Unit>> {
        return ResponseHelper.ok()
    }
    // Response:
    // {
    //   "success": true,
    //   "timestamp": "2025-11-18T10:30:00"
    // }

    /**
     * Example 3: Created response
     * Returns: 201 Created
     */
    @PostMapping("/created")
    fun exampleCreated(): ResponseEntity<ApiResponse<UserDto>> {
        val newUser = UserDto(id = "124", name = "Jane", email = "jane@example.com")
        return ResponseHelper.created(newUser)
    }
    // Response: 201 Created
    // {
    //   "success": true,
    //   "data": { ... },
    //   "timestamp": "2025-11-18T10:30:00"
    // }

    /**
     * Example 4: Accepted response (for async operations)
     * Returns: 202 Accepted
     */
    @PostMapping("/async-operation")
    fun exampleAccepted(): ResponseEntity<ApiResponse<String>> {
        return ResponseHelper.accepted("Synchronization started")
    }
    // Response: 202 Accepted
    // {
    //   "success": true,
    //   "data": "Synchronization started",
    //   "timestamp": "2025-11-18T10:30:00"
    // }

    /**
     * Example 5: No content response
     * Returns: 204 No Content
     */
    @DeleteMapping("/no-content")
    fun exampleNoContent(): ResponseEntity<Void> {
        return ResponseHelper.noContent()
    }
    // Response: 204 No Content (empty body)

    // ========================================
    // ERROR RESPONSES
    // ========================================

    /**
     * Example 6: Not found error
     * Returns: 404 Not Found
     */
    @GetMapping("/not-found")
    fun exampleNotFound(): ResponseEntity<ApiResponse<UserDto>> {
        return ResponseHelper.notFound(
            ErrorDetail.of(
                ErrorCodes.USER_NOT_FOUND,
                "User not found with email: john@example.com"
            )
        )
    }
    // Response: 404 Not Found
    // {
    //   "success": false,
    //   "error": {
    //     "code": "USER_001",
    //     "message": "User not found with email: john@example.com"
    //   },
    //   "timestamp": "2025-11-18T10:30:00"
    // }

    /**
     * Example 7: Conflict error
     * Returns: 409 Conflict
     */
    @PostMapping("/conflict")
    fun exampleConflict(): ResponseEntity<ApiResponse<UserDto>> {
        return ResponseHelper.conflict(
            ErrorDetail.of(
                ErrorCodes.USER_ALREADY_EXISTS,
                "User already exists with email: john@example.com"
            )
        )
    }
    // Response: 409 Conflict
    // {
    //   "success": false,
    //   "error": {
    //     "code": "USER_002",
    //     "message": "User already exists with email: john@example.com"
    //   },
    //   "timestamp": "2025-11-18T10:30:00"
    // }

    /**
     * Example 8: Unauthorized error
     * Returns: 401 Unauthorized
     */
    @GetMapping("/unauthorized")
    fun exampleUnauthorized(): ResponseEntity<ApiResponse<UserDto>> {
        return ResponseHelper.unauthorized(
            ErrorDetail.of(
                ErrorCodes.INVALID_CREDENTIALS,
                "Invalid email or password"
            )
        )
    }
    // Response: 401 Unauthorized

    /**
     * Example 9: Forbidden error
     * Returns: 403 Forbidden
     */
    @GetMapping("/forbidden")
    fun exampleForbidden(): ResponseEntity<ApiResponse<UserDto>> {
        return ResponseHelper.forbidden(
            ErrorDetail.of(
                ErrorCodes.ACCESS_DENIED,
                "Access denied. Required permission: ADMIN"
            )
        )
    }
    // Response: 403 Forbidden

    /**
     * Example 10: Bad request error with field
     * Returns: 400 Bad Request
     */
    @PostMapping("/bad-request")
    fun exampleBadRequest(): ResponseEntity<ApiResponse<UserDto>> {
        return ResponseHelper.badRequest(
            ErrorDetail.withField(
                ErrorCodes.INVALID_EMAIL_FORMAT,
                "Invalid email format: testexample.com",
                "email"
            )
        )
    }
    // Response: 400 Bad Request
    // {
    //   "success": false,
    //   "error": {
    //     "code": "VAL_002",
    //     "message": "Invalid email format: testexample.com",
    //     "field": "email"
    //   },
    //   "timestamp": "2025-11-18T10:30:00"
    // }

    /**
     * Example 11: Validation error with multiple fields
     * Returns: 400 Bad Request
     */
    @PostMapping("/validation-error")
    fun exampleValidationError(): ResponseEntity<*> {
        return ResponseHelper.validationError(
            code = ErrorCodes.VALIDATION_FAILED,
            message = "Validation failed",
            errors = listOf(
                FieldError(
                    field = "email",
                    message = "Invalid email format",
                    rejectedValue = "invalidemail"
                ),
                FieldError(
                    field = "password",
                    message = "Password must be at least 8 characters",
                    rejectedValue = "123"
                )
            )
        )
    }
    // Response: 400 Bad Request
    // {
    //   "success": false,
    //   "code": "VAL_001",
    //   "message": "Validation failed",
    //   "errors": [
    //     {
    //       "field": "email",
    //       "message": "Invalid email format",
    //       "rejectedValue": "invalidemail"
    //     },
    //     {
    //       "field": "password",
    //       "message": "Password must be at least 8 characters",
    //       "rejectedValue": "123"
    //     }
    //   ],
    //   "timestamp": "2025-11-18T10:30:00"
    // }

    /**
     * Example 12: Error with additional details
     * Returns: 500 Internal Server Error
     */
    @GetMapping("/error-with-details")
    fun exampleErrorWithDetails(): ResponseEntity<ApiResponse<UserDto>> {
        return ResponseHelper.internalServerError(
            ErrorDetail.withDetails(
                ErrorCodes.INTERNAL_SERVER_ERROR,
                "An internal server error occurred",
                mapOf(
                    "requestId" to "abc-123",
                    "timestamp" to System.currentTimeMillis(),
                    "path" to "/api/examples/error-with-details"
                )
            )
        )
    }
    // Response: 500 Internal Server Error
    // {
    //   "success": false,
    //   "error": {
    //     "code": "SYS_001",
    //     "message": "An internal server error occurred",
    //     "details": {
    //       "requestId": "abc-123",
    //       "timestamp": 1700308200000,
    //       "path": "/api/examples/error-with-details"
    //     }
    //   },
    //   "timestamp": "2025-11-18T10:30:00"
    // }

    // ========================================
    // MANUAL RESPONSE BUILDING
    // ========================================

    /**
     * Example 13: Manual success response building
     */
    @GetMapping("/manual-success")
    fun exampleManualSuccess(): ResponseEntity<ApiResponse<UserDto>> {
        val user = UserDto(id = "125", name = "Bob", email = "bob@example.com")
        return ResponseEntity.ok(ApiResponse.success(user))
    }

    /**
     * Example 14: Manual error response building
     */
    @GetMapping("/manual-error")
    fun exampleManualError(): ResponseEntity<ApiResponse<UserDto>> {
        val errorDetail = ErrorDetail(
            code = ErrorCodes.USER_NOT_FOUND,
            message = "User not found",
            field = null,
            details = mapOf("userId" to "999")
        )
        return ResponseEntity.status(404)
            .body(ApiResponse.error(errorDetail))
    }
}

// Example DTO
data class UserDto(
    val id: String,
    val name: String,
    val email: String
)