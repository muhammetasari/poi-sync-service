package com.rovits.poisyncservice.config

import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import kotlin.system.measureTimeMillis

@Aspect
@Component
class CacheEventLogger {
    private val logger = LoggerFactory.getLogger(CacheEventLogger::class.java)

    @Around("@annotation(cacheable)")
    fun logCacheableAccess(joinPoint: ProceedingJoinPoint, cacheable: Cacheable): Any? {
        val cacheName = cacheable.cacheNames.firstOrNull() ?: "unknown"
        val methodName = joinPoint.signature.name
        val key = joinPoint.args.firstOrNull()?.toString() ?: "unknown"

        logger.debug("🔍 Cache kontrolü - [$cacheName] key: $key")

        var result: Any?
        val executionTime = measureTimeMillis {
            result = joinPoint.proceed()
        }

        // Eğer işlem çok hızlıysa (< 50ms) muhtemelen cache'ten geldi
        if (executionTime < 50) {
            logger.info("✅ CACHE HIT - [$cacheName] key: $key (~${executionTime}ms)")
        } else {
            logger.info("❌ CACHE MISS - [$cacheName] key: $key | API çağrısı yapıldı (~${executionTime}ms)")
        }

        return result
    }
}