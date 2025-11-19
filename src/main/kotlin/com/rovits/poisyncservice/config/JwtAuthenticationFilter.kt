package com.rovits.poisyncservice.config

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
    private val userDetailsService: CustomUserDetailsService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // 1. Authorization header'ı al
        val authHeader = request.getHeader("Authorization")

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        // 2. Token'ı ayıkla
        val token = authHeader.substring(7)
        val email = jwtService.getEmailFromToken(token)

        // 3. Email geçerliyse ve context henüz set edilmemişse doğrula
        if (email != null && SecurityContextHolder.getContext().authentication == null) {
            // Token formatı ve süresi geçerli mi? (JwtService içindeki validateToken)
            if (jwtService.validateToken(token)) {
                // Kullanıcıyı veritabanından yükle (isteğe bağlı, sadece token claimleri de kullanılabilir)
                val userDetails = userDetailsService.loadUserByUsername(email)

                // Güvenli oturum nesnesi oluştur
                val authToken = UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.authorities
                )
                authToken.details = WebAuthenticationDetailsSource().buildDetails(request)

                // Context'e işle
                SecurityContextHolder.getContext().authentication = authToken
            }
        }

        filterChain.doFilter(request, response)
    }
}