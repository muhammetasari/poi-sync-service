package com.rovits.poisyncservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication // Spring Boot uygulaması olduğunu belirtir
class PoiSyncServiceApplication

fun main(args: Array<String>) {
    runApplication<PoiSyncServiceApplication>(*args) // Uygulamayı başlatır
}
