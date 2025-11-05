package com.rovits.poisyncservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration // Spring configuration sınıfı
@EnableWebSecurity // Spring Security'yi aktif eder
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() } // CSRF korumasını kapat (REST API için gerekli)
            .authorizeHttpRequests { it.anyRequest().permitAll() } // Tüm isteklere izin ver (auth yok)
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) } // Session kullanma (stateless API)

        return http.build()
    }
}
