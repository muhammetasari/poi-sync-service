package com.rovits.poisyncservice.config

import com.rovits.poisyncservice.constants.HttpConstants
import com.rovits.poisyncservice.exception.TokenInvalidException
import com.rovits.poisyncservice.service.CustomUserDetailsService
import com.rovits.poisyncservice.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userDetailsService: CustomUserDetailsService,
    private val tokenBlacklistService: com.rovits.poisyncservice.service.TokenBlacklistService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader(HttpConstants.HEADER_AUTHORIZATION)

        if (authHeader == null || !authHeader.startsWith(HttpConstants.BEARER_PREFIX)) {
            filterChain.doFilter(request, response)
            return
        }

        val token = authHeader.substring(HttpConstants.BEARER_PREFIX_LENGTH)

        if (tokenBlacklistService.isTokenBlacklisted(token)) {
            filterChain.doFilter(request, response)
            return
        }

        val email = try {
            if (!jwtService.validateToken(token)) {
                throw TokenInvalidException()
            }
            jwtService.getEmailFromToken(token)
        } catch (ex: Exception) {
            throw TokenInvalidException(ex)
        }

        if (email != null && SecurityContextHolder.getContext().authentication == null) {
            val userDetails = userDetailsService.loadUserByUsername(email)

            val authToken = UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.authorities
            )
            authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
            SecurityContextHolder.getContext().authentication = authToken
        }

        filterChain.doFilter(request, response)
    }
}