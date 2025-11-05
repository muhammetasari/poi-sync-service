package com.rovits.poisyncservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PoiSyncServiceApplication

fun main(args: Array<String>) {
    runApplication<PoiSyncServiceApplication>(*args)
}