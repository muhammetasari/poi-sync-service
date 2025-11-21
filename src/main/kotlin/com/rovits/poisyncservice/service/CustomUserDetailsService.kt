package com.rovits.poisyncservice.service

import com.rovits.poisyncservice.exception.ErrorCodes
import com.rovits.poisyncservice.exception.ResourceNotFoundException
import com.rovits.poisyncservice.repository.UserRepository
import com.rovits.poisyncservice.util.MessageKeys
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByEmail(email)
            .orElseThrow {
                ResourceNotFoundException(
                    ErrorCodes.USER_NOT_FOUND,
                    MessageKeys.USER_NOT_FOUND,
                    arrayOf(email)
                )
            }

        val authorities = user.roles.map { SimpleGrantedAuthority(it.name) }

        return User(user.email, user.password ?: "", authorities)
    }
}