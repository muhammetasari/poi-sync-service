package com.rovits.poisyncservice.service

import com.rovits.poisyncservice.repository.UserRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.util.Collections

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByEmail(email)
            .orElseThrow { UsernameNotFoundException("User not found with email: $email") }

        // Burada kullanıcının rolleri varsa eklenebilir, şimdilik boş liste geçiyoruz
        return User(user.email, user.password ?: "", Collections.emptyList())
    }
}