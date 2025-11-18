package com.rovits.poisyncservice.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class ApiKeyFilter(
    @Value("\${api.key.header}") private val apiKeyHeader: String,
    @Value("\${api.key.value}") private val apiKeyValue: String
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // Skip API key check for health endpoint
        if (request.requestURI == "/actuator/health") {
            filterChain.doFilter(request, response)
            return
        }

        val providedKey = request.getHeader(apiKeyHeader)

        if (providedKey == null || providedKey != apiKeyValue) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = "application/json"
            response.writer.write("{\"error\": \"Unauthorized - Invalid or missing API key\"}")
            return
        }

        filterChain.doFilter(request, response)
    }
}