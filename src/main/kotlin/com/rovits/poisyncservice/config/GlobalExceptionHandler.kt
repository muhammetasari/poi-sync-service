package com.rovits.poisyncservice.config

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice // Tüm controller'lardaki hataları yakalar
class GlobalExceptionHandler {

    @ExceptionHandler(Exception::class) // Genel exception'ları yakala
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR) // 500 hatası döndür
            .body(
                ErrorResponse(
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    error = "INTERNAL_SERVER_ERROR",
                    message = ex.message ?: "Beklenmeyen bir hata oluştu",
                    timestamp = LocalDateTime.now()
                )
            )
    }
    
    @ExceptionHandler(IllegalArgumentException::class) // Geçersiz parametre hatalarını yakala
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST) // 400 hatası döndür
            .body(
                ErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = "BAD_REQUEST",
                    message = ex.message ?: "Geçersiz parametre",
                    timestamp = LocalDateTime.now()
                )
            )
    }
}

data class ErrorResponse( // Hata response'u için model
    val status: Int, // HTTP status code
    val error: String, // Hata tipi
    val message: String, // Hata mesajı
    val timestamp: LocalDateTime // Hata zamanı
)
