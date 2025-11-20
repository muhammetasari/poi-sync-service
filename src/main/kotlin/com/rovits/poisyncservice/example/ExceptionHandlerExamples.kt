package com.rovits.poisyncservice.example

import com.rovits.poisyncservice.exception.*
import org.springframework.web.bind.annotation.*
import org.springframework.security.access.AccessDeniedException

/**
 * Example scenarios demonstrating how GlobalExceptionHandler works
 */
@RestController
@RequestMapping("/api/test/exceptions")
class ExceptionHandlerExamples {

    @GetMapping("/user-not-found")
    fun testUserNotFound() {
        throw ResourceNotFoundException(
            errorCode = ErrorCodes.USER_NOT_FOUND,
            messageKey = "error.user.not.found",
            messageArgs = arrayOf("john@example.com")
        )
    }

    @PostMapping("/user-exists")
    fun testUserExists() {
        // httpStatus parametresi kaldırıldı
        throw BusinessException(
            errorCode = ErrorCodes.USER_ALREADY_EXISTS,
            messageKey = "error.user.already.exists",
            messageArgs = arrayOf("john@example.com")
        )
    }

    @PostMapping("/invalid-credentials")
    fun testInvalidCredentials() {
        throw AuthenticationException(
            errorCode = ErrorCodes.INVALID_CREDENTIALS,
            messageKey = "error.invalid.credentials"
        )
    }

    @GetMapping("/access-denied")
    fun testAccessDenied() {
        throw AuthorizationException(
            errorCode = ErrorCodes.ACCESS_DENIED,
            messageKey = "error.access.denied",
            messageArgs = arrayOf("ADMIN"),
            requiredPermission = "ADMIN"
        )
    }

    @PostMapping("/validation-error")
    fun testValidationError() {
        throw ValidationException(
            errorCode = ErrorCodes.INVALID_EMAIL_FORMAT,
            messageKey = "error.validation.email",
            messageArgs = arrayOf("testexample.com"),
            fieldName = "email"
        )
    }

    @GetMapping("/external-service-error")
    fun testExternalServiceError() {
        throw ExternalServiceException(
            errorCode = ErrorCodes.GOOGLE_API_UNAVAILABLE,
            messageKey = "error.google.api.unavailable",
            serviceName = "Google Places API"
        )
    }

    @GetMapping("/cache-error")
    fun testCacheError() {
        throw CacheException(
            errorCode = ErrorCodes.CACHE_ERROR,
            messageKey = "error.cache.operation.failed",
            messageArgs = arrayOf("GET"),
            operation = "GET"
        )
    }

    // ... Diğer test metodları aynı kalabilir ...

    @GetMapping("/spring-security-denied")
    fun testSpringSecurityDenied() {
        throw AccessDeniedException("Access denied by Spring Security")
    }

    @GetMapping("/generic-error")
    fun testGenericError() {
        throw RuntimeException("Something went wrong")
    }

    @GetMapping("/i18n-test")
    fun testI18n() {
        throw ResourceNotFoundException(
            errorCode = ErrorCodes.USER_NOT_FOUND,
            messageKey = "error.user.not.found",
            messageArgs = arrayOf("test@example.com")
        )
    }
}