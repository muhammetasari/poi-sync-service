package com.rovits.poisyncservice.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.rovits.poisyncservice.dto.response.ApiResponse
import com.rovits.poisyncservice.dto.response.ErrorDetail
import com.rovits.poisyncservice.exception.ErrorCodes
import com.rovits.poisyncservice.service.JwtService
import com.rovits.poisyncservice.service.RateLimitService
import com.rovits.poisyncservice.util.MessageResolver
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class RateLimitFilter(
    private val rateLimitService: RateLimitService,
    private val jwtService: JwtService,
    private val objectMapper: ObjectMapper,
    private val messageResolver: MessageResolver
) : OncePerRequestFilter() {

    companion object {
        // Limit Ayarları
        private const val ANONYMOUS_LIMIT = 20
        private const val AUTHENTICATED_LIMIT = 100
        private const val PERIOD_SECONDS = 60L
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // Health check endpoint'ini atla
        if (request.requestURI.startsWith("/actuator/health")) {
            filterChain.doFilter(request, response)
            return
        }

        val token = extractToken(request)
        val userEmail = if (token != null && jwtService.validateToken(token)) {
            jwtService.getEmailFromToken(token)
        } else null

        val (key, limit) = if (userEmail != null) {
            // Giriş yapmış kullanıcı: Email bazlı limit
            Pair("user:$userEmail", AUTHENTICATED_LIMIT)
        } else {
            // Anonim kullanıcı: IP bazlı limit
            Pair("ip:${getClientIp(request)}", ANONYMOUS_LIMIT)
        }

        if (rateLimitService.isRateLimitExceeded(key, limit, PERIOD_SECONDS)) {
            writeTooManyRequestsResponse(response)
            return
        }

        filterChain.doFilter(request, response)
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val header = request.getHeader("Authorization")
        return if (header != null && header.startsWith("Bearer ")) {
            header.substring(7)
        } else null
    }

    private fun getClientIp(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        return if (!xForwardedFor.isNullOrEmpty()) {
            xForwardedFor.split(",")[0].trim()
        } else {
            request.remoteAddr
        }
    }

    private fun writeTooManyRequestsResponse(response: HttpServletResponse) {
        response.status = HttpStatus.TOO_MANY_REQUESTS.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"

        val message = messageResolver.resolve("error.too.many.requests")

        val errorDetail = ErrorDetail.of(
            ErrorCodes.RATE_LIMIT_EXCEEDED,
            message
        )

        val apiResponse = ApiResponse.error<Any>(errorDetail)
        objectMapper.writeValue(response.writer, apiResponse)
    }
}