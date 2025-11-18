package com.rovits.poisyncservice.example

import com.rovits.poisyncservice.domain.dto.LoginRequest
import com.rovits.poisyncservice.domain.dto.RegisterRequest
import com.rovits.poisyncservice.dto.response.ApiResponse
import com.rovits.poisyncservice.util.ResponseHelper
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Test controller to demonstrate Bean Validation
 *
 * Test with:
 * curl -X POST http://localhost:8080/api/test/validation/register \
 *   -H "Content-Type: application/json" \
 *   -H "Accept-Language: en" \
 *   -d '{"name":"A","email":"invalid","password":"123"}'
 */
@RestController
@RequestMapping("/api/test/validation")
class ValidationTestController {

    @PostMapping("/register")
    fun testRegister(
        @Valid @RequestBody request: RegisterRequest
    ): ResponseEntity<ApiResponse<String>> {
        return ResponseHelper.ok("Validation passed! User: ${request.name}")
    }

    @PostMapping("/login")
    fun testLogin(
        @Valid @RequestBody request: LoginRequest
    ): ResponseEntity<ApiResponse<String>> {
        return ResponseHelper.ok("Validation passed! Email: ${request.email}")
    }
}