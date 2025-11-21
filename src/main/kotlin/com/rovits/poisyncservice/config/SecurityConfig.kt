package com.rovits.poisyncservice.config

import com.rovits.poisyncservice.constants.ApiEndpoints
import com.rovits.poisyncservice.domain.enums.UserRole
import com.rovits.poisyncservice.exception.SecurityExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val apiKeyFilter: ApiKeyFilter,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val rateLimitFilter: RateLimitFilter,
    private val securityExceptionHandler: SecurityExceptionHandler
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager {
        return config.authenticationManager
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .exceptionHandling {
                it.authenticationEntryPoint(securityExceptionHandler)
                it.accessDeniedHandler(securityExceptionHandler)
            }
            .authorizeHttpRequests {
                // 1. Herkese Açık (Public)
                it.requestMatchers(
                    ApiEndpoints.PATTERN_AUTH,
                    ApiEndpoints.ACTUATOR_HEALTH,
                    ApiEndpoints.PATTERN_OPENAPI_DOCS,
                    ApiEndpoints.SWAGGER_UI_HTML,
                    ApiEndpoints.PATTERN_SWAGGER_UI,
                    ApiEndpoints.PATTERN_TEST
                ).permitAll()
                it.requestMatchers(ApiEndpoints.PATTERN_SYNC).hasAuthority(UserRole.ROLE_ADMIN.name)
                it.anyRequest().authenticated()
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterAfter(rateLimitFilter, ApiKeyFilter::class.java)
            .addFilterAfter(jwtAuthenticationFilter, ApiKeyFilter::class.java)

        return http.build()
    }
}