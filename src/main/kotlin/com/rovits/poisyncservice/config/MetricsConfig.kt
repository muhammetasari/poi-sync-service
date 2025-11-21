package com.rovits.poisyncservice.config

import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.autoconfigure.metrics.export.otlp.OtlpMetricsExportAutoConfiguration
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment

@Configuration
class MetricsConfig(
    private val environment: Environment
) {
    private val logger = LoggerFactory.getLogger(MetricsConfig::class.java)

    @EventListener(ApplicationReadyEvent::class)
    fun logMetricsConfiguration() {
        logger.info("=".repeat(60))
        logger.info("Metrics Configuration Status")
        logger.info("=".repeat(60))

        val otlpEnabled = environment.getProperty("management.otlp.metrics.export.enabled", "false")
        val otlpUrl = environment.getProperty("management.otlp.metrics.export.url", "NOT_SET")
        val otlpStep = environment.getProperty("management.otlp.metrics.export.step", "60s")
        val serviceName = environment.getProperty("management.otlp.metrics.export.resource-attributes[service.name]", "NOT_SET")
        val hasAuthHeader = !environment.getProperty("management.otlp.metrics.export.headers.Authorization", "").isNullOrEmpty()

        logger.info("OTLP Metrics Export Enabled: $otlpEnabled")
        logger.info("OTLP Endpoint: ${maskUrl(otlpUrl)}")
        logger.info("OTLP Export Interval: $otlpStep")
        logger.info("Service Name: $serviceName")
        logger.info("Authorization Header Set: $hasAuthHeader")
        logger.info("Active Profile: ${environment.activeProfiles.joinToString()}")
        logger.info("=".repeat(60))

        if (otlpEnabled == "true" && otlpUrl != "NOT_SET") {
            logger.info("✅ OTLP metrics export is ENABLED and configured")
        } else {
            logger.warn("⚠️ OTLP metrics export is NOT properly configured!")
            logger.warn("   - Check GRAFANA_OTLP_ENDPOINT environment variable")
            logger.warn("   - Check GRAFANA_API_TOKEN_BASE64 environment variable")
        }
    }

    private fun maskUrl(url: String): String {
        if (url == "NOT_SET") return url
        return try {
            val parts = url.split("//")
            if (parts.size == 2) {
                "${parts[0]}//${parts[1].substringBefore("/")}/..."
            } else {
                url.take(30) + "..."
            }
        } catch (e: Exception) {
            "***MASKED***"
        }
    }
}

