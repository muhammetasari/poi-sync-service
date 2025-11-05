package com.rovits.poisyncservice.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration // Spring configuration sınıfı
class WebClientConfig(
    @Value("\${google.api.key}") private val apiKey: String // application.properties'ten API key'i al
) {

    @Bean("googlePlacesWebClient") // Google Places için özel WebClient bean'i oluştur
    fun googlePlacesWebClient(): WebClient {
        return WebClient.builder()
            .baseUrl("https://places.googleapis.com/v1") // Google Places API base URL
            .defaultHeader("X-Goog-Api-Key", apiKey) // Her istekte API key'i header'a ekle
            .build()
    }
}
