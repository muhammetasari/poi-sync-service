package com.rovits.poisyncservice.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig(
    @Value("\${google.api.key}") private val apiKey: String
) {

    @Bean("googlePlacesWebClient")
    fun googlePlacesWebClient(): WebClient {
        return WebClient.builder()
            .baseUrl("https://places.googleapis.com/v1")
            .defaultHeader("X-Goog-Api-Key", apiKey)
            .build()
    }
}