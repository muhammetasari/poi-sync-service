package com.rovits.poisyncservice.config

import com.rovits.poisyncservice.constants.HttpConstants
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

/**
 * Filter that adds a correlation ID to each request for tracking purposes.
 * The correlation ID is:
 * 1. Taken from X-Correlation-ID header if present
 * 2. Generated as UUID if not present
 * 3. Added to MDC for logging
 * 4. Added to response header
 *
 * Usage in logs:
 * logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%X{correlationId}] %-5level %logger{36} - %msg%n
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class CorrelationIdFilter : OncePerRequestFilter() {

    companion object {
        const val CORRELATION_ID_HEADER = HttpConstants.HEADER_X_CORRELATION_ID
        const val CORRELATION_ID_MDC_KEY = HttpConstants.MDC_KEY_CORRELATION_ID
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // Get correlation ID from request header or generate new one
        val correlationId = request.getHeader(CORRELATION_ID_HEADER)
            ?: UUID.randomUUID().toString()

        // Add to MDC for logging
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId)

        // Add to response header so client can track the request
        response.setHeader(CORRELATION_ID_HEADER, correlationId)

        try {
            filterChain.doFilter(request, response)
        } finally {
            // Always clean up MDC to prevent memory leaks
            MDC.clear()
        }
    }
}