package com.rovits.poisyncservice.config

import com.rovits.poisyncservice.dto.response.ApiResponse
import com.rovits.poisyncservice.exception.BaseException
import com.rovits.poisyncservice.exception.ErrorCodes
import com.rovits.poisyncservice.util.MessageResolver
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.core.MethodParameter

class GlobalExceptionHandlerTest {

    @Mock
    private lateinit var messageResolver: MessageResolver

    private lateinit var globalExceptionHandler: GlobalExceptionHandler

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        globalExceptionHandler = GlobalExceptionHandler(messageResolver, "dev")
    }

    @Test
    fun `should handle BaseException`() {
        val exception = object : BaseException(
            httpStatus = HttpStatus.BAD_REQUEST,
            errorCode = "TEST_ERROR",
            messageKey = "error.test"
        ) {}
        `when`(messageResolver.resolve("error.test")).thenReturn("Test error message")

        val response: ResponseEntity<ApiResponse<Nothing>> = globalExceptionHandler.handleBaseException(exception)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("TEST_ERROR", response.body?.error?.code)
        assertEquals("Test error message", response.body?.error?.message)
    }

    @Test
    fun `should handle MethodArgumentNotValidException`() {
        val fieldError = FieldError("testObject", "testField", "default message")
        val bindingResult = org.mockito.Mockito.mock(BindingResult::class.java)
        `when`(bindingResult.fieldErrors).thenReturn(listOf(fieldError))
        `when`(bindingResult.errorCount).thenReturn(1)

        val parameter = org.mockito.Mockito.mock(MethodParameter::class.java)
        val exception = MethodArgumentNotValidException(parameter, bindingResult)
        `when`(messageResolver.resolveOrDefault("default message", "default message", "")).thenReturn("Validation message")
        `when`(messageResolver.resolve("error.validation.failed")).thenReturn("Validation failed")


        val response = globalExceptionHandler.handleValidationException(exception)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals(ErrorCodes.VALIDATION_FAILED, response.body?.code)
        assertEquals("Validation failed", response.body?.message)
        assertEquals(1, response.body?.errors?.size)
        assertEquals("testField", response.body?.errors?.get(0)?.field)
        assertEquals("Validation message", response.body?.errors?.get(0)?.message)
    }

    @Test
    fun `should handle AccessDeniedException`() {
        val exception = AccessDeniedException("Access is denied")
        `when`(messageResolver.resolve("error.access.denied", "REQUIRED_PERMISSION")).thenReturn("Access denied message")

        val response = globalExceptionHandler.handleAccessDenied(exception)

        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
        assertEquals(ErrorCodes.ACCESS_DENIED, response.body?.error?.code)
        assertEquals("Access denied message", response.body?.error?.message)
    }
}
