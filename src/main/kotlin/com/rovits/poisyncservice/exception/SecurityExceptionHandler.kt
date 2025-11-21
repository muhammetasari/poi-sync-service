package com.rovits.poisyncservice.exception

import com.fasterxml.jackson.databind.ObjectMapper
import com.rovits.poisyncservice.constants.HttpConstants
import com.rovits.poisyncservice.dto.response.ApiResponse
import com.rovits.poisyncservice.dto.response.ErrorDetail
import com.rovits.poisyncservice.util.MessageKeys
import com.rovits.poisyncservice.util.MessageResolver
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

@Component
class SecurityExceptionHandler(
    private val objectMapper: ObjectMapper,
    private val messageResolver: MessageResolver
) : AuthenticationEntryPoint, AccessDeniedHandler {

    // 401 Unauthorized Hataları (Token yok, geçersiz token vs.)
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        val message = messageResolver.resolve(MessageKeys.UNAUTHORIZED)
        val errorDetail = ErrorDetail.of(ErrorCodes.UNAUTHORIZED, message)
        writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED, errorDetail)
    }

    // 403 Forbidden Hataları (Yetki yetersiz)
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        val message = messageResolver.resolve(MessageKeys.ACCESS_DENIED, accessDeniedException.message ?: "ACCESS_DENIED")
        val errorDetail = ErrorDetail.of(ErrorCodes.ACCESS_DENIED, message)
        writeResponse(response, HttpServletResponse.SC_FORBIDDEN, errorDetail)
    }

    private fun writeResponse(response: HttpServletResponse, status: Int, errorDetail: ErrorDetail) {
        response.status = status
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = HttpConstants.ENCODING_UTF_8

        val apiResponse = ApiResponse.error(errorDetail)
        objectMapper.writeValue(response.writer, apiResponse)
    }
}