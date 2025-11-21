package com.rovits.poisyncservice.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.rovits.poisyncservice.constants.ApiEndpoints
import com.rovits.poisyncservice.constants.CacheConstants
import com.rovits.poisyncservice.constants.HttpConstants
import com.rovits.poisyncservice.dto.response.ApiResponse
import com.rovits.poisyncservice.dto.response.ErrorDetail
import com.rovits.poisyncservice.exception.ErrorCodes
import com.rovits.poisyncservice.util.MessageKeys
import com.rovits.poisyncservice.util.MessageResolver
import com.rovits.poisyncservice.service.RateLimitService
import com.rovits.poisyncservice.constants.DefaultValues
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
    private val messageResolver: MessageResolver,
    private val rateLimitService: RateLimitService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val path = request.requestURI

        if (path.startsWith(ApiEndpoints.ACTUATOR_HEALTH) ||
            path.startsWith(ApiEndpoints.OPENAPI_DOCS) ||
            path.startsWith(ApiEndpoints.SWAGGER_UI) ||
            path == ApiEndpoints.SWAGGER_UI_HTML
        ) {
            filterChain.doFilter(request, response)
            return
        }

        val providedKey = request.getHeader(apiKeyHeader)

        // Rate limit kontrolü (API Key bazlı)
        val rateLimitKey = "${CacheConstants.PREFIX_APIKEY}${providedKey ?: CacheConstants.UNKNOWN_API_KEY}"
        val limit = DefaultValues.DEFAULT_RATE_LIMIT_AUTHENTICATED
        val period = DefaultValues.DEFAULT_RATE_LIMIT_PERIOD_SECONDS.toInt()
        if (rateLimitService.isRateLimitExceeded(rateLimitKey, limit, period)) {
            response.status = HttpConstants.STATUS_TOO_MANY_REQUESTS
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.characterEncoding = HttpConstants.ENCODING_UTF_8
            val message = messageResolver.resolve(MessageKeys.RATE_LIMIT_EXCEEDED)
            val errorDetail = ErrorDetail.of(
                ErrorCodes.RATE_LIMIT_EXCEEDED,
                message
            )
            val apiResponse = ApiResponse.error(errorDetail)
            objectMapper.writeValue(response.writer, apiResponse)
            return
        }

        if (providedKey == null || providedKey != apiKeyValue) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.characterEncoding = HttpConstants.ENCODING_UTF_8

            val message = messageResolver.resolve(MessageKeys.UNAUTHORIZED)
            val detailMessage = messageResolver.resolve(MessageKeys.API_KEY_MISSING_OR_INVALID)

            val errorDetail = ErrorDetail.of(
                ErrorCodes.UNAUTHORIZED,
                "$message ($detailMessage)"
            )

            val apiResponse = ApiResponse.error(errorDetail)

            objectMapper.writeValue(response.writer, apiResponse)
            return
        }

        filterChain.doFilter(request, response)
    }
}