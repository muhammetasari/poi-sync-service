package com.rovits.poisyncservice.config

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
                it.requestMatchers(
                    "/api/auth/**",
                    "/actuator/health",
                    "/v3/api-docs/**",
                    "/swagger-ui/**"
                ).permitAll()
                it.anyRequest().authenticated()
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterAfter(jwtAuthenticationFilter, ApiKeyFilter::class.java)

        return http.build()
    }
}