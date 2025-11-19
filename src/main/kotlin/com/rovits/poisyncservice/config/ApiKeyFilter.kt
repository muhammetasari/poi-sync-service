package com.rovits.poisyncservice.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.rovits.poisyncservice.dto.response.ApiResponse
import com.rovits.poisyncservice.dto.response.ErrorDetail
import com.rovits.poisyncservice.exception.ErrorCodes
import com.rovits.poisyncservice.util.MessageResolver
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class ApiKeyFilter(
    @Value("\${api.key.header}") private val apiKeyHeader: String,
    @Value("\${api.key.value}") private val apiKeyValue: String,
    private val objectMapper: ObjectMapper,
    private val messageResolver: MessageResolver
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (request.requestURI.startsWith("/actuator/health")) {
            filterChain.doFilter(request, response)
            return
        }

        val providedKey = request.getHeader(apiKeyHeader)

        if (providedKey == null || providedKey != apiKeyValue) {
            // Standart Error Response Formatı
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.characterEncoding = "UTF-8"

            val message = messageResolver.resolve("error.unauthorized")

            val errorDetail = ErrorDetail.of(
                ErrorCodes.UNAUTHORIZED,
                "$message (API Key Missing or Invalid)"
            )

            val apiResponse = ApiResponse.error<Any>(errorDetail)

            objectMapper.writeValue(response.writer, apiResponse)
            return
        }

        filterChain.doFilter(request, response)
    }
}