package com.rovits.poisyncservice.domain.enums

enum class AuthProvider {
    GOOGLE, FACEBOOK, APPLE, EMAIL;

    companion object {
        fun fromString(value: String?): AuthProvider? =
            values().find { it.name.equals(value, ignoreCase = true) }
    }
}

