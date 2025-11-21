package com.rovits.poisyncservice.exception

/**
 * Fırlatıldığında, geçersiz veya bozulmuş JWT token için kullanılır.
 * GlobalExceptionHandler'da 401 UNAUTHORIZED olarak işlenir.
 */
class TokenInvalidException(
    cause: Throwable? = null
) : AuthenticationException(
    errorCode = ErrorCodes.TOKEN_INVALID,
    messageKey = com.rovits.poisyncservice.util.MessageKeys.TOKEN_INVALID,
    cause = cause
)

