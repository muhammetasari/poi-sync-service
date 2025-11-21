package com.rovits.poisyncservice.config

import com.rovits.poisyncservice.constants.ApiEndpoints
import com.rovits.poisyncservice.constants.HttpConstants
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
            .baseUrl(ApiEndpoints.GOOGLE_PLACES_BASE_URL)
            .defaultHeader(HttpConstants.HEADER_X_GOOG_API_KEY, apiKey)
            .build()
    }
}