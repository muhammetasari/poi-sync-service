package com.rovits.poisyncservice.example

import com.rovits.poisyncservice.exception.*
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.*

/**
 * Example scenarios demonstrating how GlobalExceptionHandler works
 * These are test endpoints to see different error responses
 */
@RestController
@RequestMapping("/api/test/exceptions")
class ExceptionHandlerExamples {

    // ========================================
    // CUSTOM EXCEPTION SCENARIOS
    // ========================================

    /**
     * Test Scenario 1: User not found (ResourceNotFoundException)
     *
     * Request:
     * GET /api/test/exceptions/user-not-found
     * Accept-Language: en
     *
     * Response: 404 NOT FOUND
     * {
     *   "success": false,
     *   "error": {
     *     "code": "USER_001",
     *     "message": "User not found with email: john@example.com"
     *   },
     *   "timestamp": "2025-11-18T10:30:00"
     * }
     */
    @GetMapping("/user-not-found")
    fun testUserNotFound() {
        throw ResourceNotFoundException(
            errorCode = ErrorCodes.USER_NOT_FOUND,
            messageKey = "error.user.not.found",
            messageArgs = arrayOf("john@example.com")
        )
    }

    /**
     * Test Scenario 2: User already exists (BusinessException)
     *
     * Request:
     * POST /api/test/exceptions/user-exists
     * Accept-Language: tr
     *
     * Response: 409 CONFLICT
     * {
     *   "success": false,
     *   "error": {
     *     "code": "USER_002",
     *     "message": "john@example.com email adresi zaten kullanımda"
     *   },
     *   "timestamp": "2025-11-18T10:30:00"
     * }
     */
    @PostMapping("/user-exists")
    fun testUserExists() {
        throw BusinessException(
            errorCode = ErrorCodes.USER_ALREADY_EXISTS,
            messageKey = "error.user.already.exists",
            messageArgs = arrayOf("john@example.com"),
            httpStatus = HttpStatus.CONFLICT
        )
    }

    /**
     * Test Scenario 3: Invalid credentials (AuthenticationException)
     *
     * Response: 401 UNAUTHORIZED
     */
    @PostMapping("/invalid-credentials")
    fun testInvalidCredentials() {
        throw AuthenticationException(
            errorCode = ErrorCodes.INVALID_CREDENTIALS,
            messageKey = "error.invalid.credentials"
        )
    }

    /**
     * Test Scenario 4: Access denied (AuthorizationException)
     *
     * Response: 403 FORBIDDEN
     */
    @GetMapping("/access-denied")
    fun testAccessDenied() {
        throw AuthorizationException(
            errorCode = ErrorCodes.ACCESS_DENIED,
            messageKey = "error.access.denied",
            messageArgs = arrayOf("ADMIN"),
            requiredPermission = "ADMIN"
        )
    }

    /**
     * Test Scenario 5: Validation error with field (ValidationException)
     *
     * Response: 400 BAD REQUEST
     * {
     *   "success": false,
     *   "error": {
     *     "code": "VAL_002",
     *     "message": "Invalid email format: testexample.com",
     *     "field": "email"
     *   },
     *   "timestamp": "2025-11-18T10:30:00"
     * }
     */
    @PostMapping("/validation-error")
    fun testValidationError() {
        throw ValidationException(
            errorCode = ErrorCodes.INVALID_EMAIL_FORMAT,
            messageKey = "error.validation.email",
            messageArgs = arrayOf("testexample.com"),
            fieldName = "email"
        )
    }

    /**
     * Test Scenario 6: External service error (ExternalServiceException)
     *
     * Response: 503 SERVICE UNAVAILABLE
     */
    @GetMapping("/external-service-error")
    fun testExternalServiceError() {
        throw ExternalServiceException(
            errorCode = ErrorCodes.GOOGLE_API_UNAVAILABLE,
            messageKey = "error.google.api.unavailable",
            serviceName = "Google Places API"
        )
    }

    /**
     * Test Scenario 7: Cache error (CacheException)
     *
     * Response: 500 INTERNAL SERVER ERROR
     */
    @GetMapping("/cache-error")
    fun testCacheError() {
        throw CacheException(
            errorCode = ErrorCodes.CACHE_ERROR,
            messageKey = "error.cache.operation.failed",
            messageArgs = arrayOf("GET"),
            operation = "GET"
        )
    }

    // ========================================
    // SPRING VALIDATION SCENARIOS
    // ========================================

    /**
     * Test Scenario 8: Bean Validation error
     * This will be caught by @Valid annotation in controller
     *
     * Request:
     * POST /api/test/exceptions/bean-validation
     * {
     *   "email": "invalidemail",
     *   "password": "123"
     * }
     *
     * Response: 400 BAD REQUEST
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
     */
    // This would be handled automatically by GlobalExceptionHandler
    // when @Valid annotation is used in controllers

    // ========================================
    // REQUEST PARAMETER SCENARIOS
    // ========================================

    /**
     * Test Scenario 9: Missing required parameter
     *
     * Request:
     * GET /api/test/exceptions/missing-param
     * (without required parameter)
     *
     * Response: 400 BAD REQUEST
     * {
     *   "success": false,
     *   "error": {
     *     "code": "VAL_004",
     *     "message": "email is required",
     *     "field": "email"
     *   },
     *   "timestamp": "2025-11-18T10:30:00"
     * }
     */
    @GetMapping("/missing-param")
    fun testMissingParam(@RequestParam email: String): String {
        return "Success"
    }

    /**
     * Test Scenario 10: Type mismatch in parameter
     *
     * Request:
     * GET /api/test/exceptions/type-mismatch?age=notanumber
     *
     * Response: 400 BAD REQUEST
     * {
     *   "success": false,
     *   "error": {
     *     "code": "VAL_001",
     *     "message": "Invalid value for parameter 'age'. Expected type: Int",
     *     "field": "age"
     *   },
     *   "timestamp": "2025-11-18T10:30:00"
     * }
     */
    @GetMapping("/type-mismatch")
    fun testTypeMismatch(@RequestParam age: Int): String {
        return "Success"
    }

    // ========================================
    // SECURITY SCENARIOS
    // ========================================

    /**
     * Test Scenario 11: Spring Security access denied
     *
     * Response: 403 FORBIDDEN
     */
    @GetMapping("/spring-security-denied")
    fun testSpringSecurityDenied() {
        throw AccessDeniedException("Access denied by Spring Security")
    }

    // ========================================
    // GENERIC ERROR SCENARIO
    // ========================================

    /**
     * Test Scenario 12: Generic uncaught exception
     *
     * Development Response: 500 INTERNAL SERVER ERROR
     * {
     *   "success": false,
     *   "error": {
     *     "code": "SYS_001",
     *     "message": "An internal server error occurred",
     *     "details": {
     *       "exceptionType": "RuntimeException",
     *       "message": "Something went wrong",
     *       "stackTrace": [...]
     *     }
     *   },
     *   "timestamp": "2025-11-18T10:30:00"
     * }
     *
     * Production Response: (no stack trace or details)
     * {
     *   "success": false,
     *   "error": {
     *     "code": "SYS_001",
     *     "message": "An internal server error occurred"
     *   },
     *   "timestamp": "2025-11-18T10:30:00"
     * }
     */
    @GetMapping("/generic-error")
    fun testGenericError() {
        throw RuntimeException("Something went wrong")
    }

    // ========================================
    // I18N SCENARIOS
    // ========================================

    /**
     * Test Scenario 13: Same error, different languages
     *
     * Request with Accept-Language: en
     * Response: "User not found with email: test@example.com"
     *
     * Request with Accept-Language: tr
     * Response: "test@example.com email adresine sahip kullanıcı bulunamadı"
     */
    @GetMapping("/i18n-test")
    fun testI18n() {
        throw ResourceNotFoundException(
            errorCode = ErrorCodes.USER_NOT_FOUND,
            messageKey = "error.user.not.found",
            messageArgs = arrayOf("test@example.com")
        )
    }
}

/**
 * HOW TO TEST:
 *
 * 1. Start the application
 * 2. Use curl or Postman to call these endpoints
 * 3. Observe the standardized error responses
 * 4. Test different languages by changing Accept-Language header
 *
 * Example curl commands:
 *
 * # Test user not found (English)
 * curl -H "Accept-Language: en" http://localhost:8080/api/test/exceptions/user-not-found
 *
 * # Test user not found (Turkish)
 * curl -H "Accept-Language: tr" http://localhost:8080/api/test/exceptions/user-not-found
 *
 * # Test missing parameter
 * curl http://localhost:8080/api/test/exceptions/missing-param
 *
 * # Test type mismatch
 * curl "http://localhost:8080/api/test/exceptions/type-mismatch?age=notanumber"
 *
 * # Test generic error
 * curl http://localhost:8080/api/test/exceptions/generic-error
 */