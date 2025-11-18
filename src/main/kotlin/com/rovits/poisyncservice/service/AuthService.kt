package com.rovits.poisyncservice.service

import com.google.firebase.auth.FirebaseAuth
import com.rovits.poisyncservice.domain.document.UserDocument
import com.rovits.poisyncservice.domain.dto.*
import com.rovits.poisyncservice.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder
) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    @Transactional
    fun socialLogin(request: SocialLoginRequest): AuthResponse {
        logger.info("Processing social login: provider={}", request.provider)

        val decodedToken = try {
            FirebaseAuth.getInstance().verifyIdToken(request.firebaseToken)
        } catch (e: Exception) {
            logger.error("Invalid Firebase token", e)
            throw IllegalArgumentException("Invalid Firebase token")
        }

        val email = decodedToken.email ?: throw IllegalArgumentException("Email not found in token")
        val name = decodedToken.name

        val user = userRepository.findByEmail(email).orElseGet {
            logger.info("Creating new user: email={}", email)
            val newUser = UserDocument(
                email = email,
                name = name,
                password = null,
                provider = request.provider
            )
            userRepository.save(newUser)
        }

        logger.info("Social login successful: email={}", email)
        return generateAuthResponse(user)
    }

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        logger.info("Processing registration: email={}", request.email)

        if (userRepository.findByEmail(request.email).isPresent) {
            logger.warn("Registration failed: Email already exists - {}", request.email)
            throw IllegalArgumentException("Email already in use")
        }

        val hashedPassword = passwordEncoder.encode(request.password)

        val newUser = UserDocument(
            email = request.email,
            name = request.name,
            password = hashedPassword,
            provider = "email"
        )

        val savedUser = userRepository.save(newUser)
        logger.info("User registered successfully: email={}, id={}", savedUser.email, savedUser.id)

        return generateAuthResponse(savedUser)
    }

    @Transactional(readOnly = true)
    fun login(request: LoginRequest): AuthResponse {
        logger.info("Processing login: email={}", request.email)

        val user = userRepository.findByEmail(request.email)
            .orElseThrow {
                logger.warn("Login failed: User not found - {}", request.email)
                BadCredentialsException("Invalid email or password")
            }

        if (user.provider != "email" || user.password == null) {
            logger.warn("Login failed: Wrong provider ({}) - {}", user.provider, request.email)
            throw BadCredentialsException("This account uses ${user.provider} login")
        }

        if (!passwordEncoder.matches(request.password, user.password)) {
            logger.warn("Login failed: Wrong password - {}", request.email)
            throw BadCredentialsException("Invalid email or password")
        }

        logger.info("Login successful: email={}", user.email)
        return generateAuthResponse(user)
    }

    private fun generateAuthResponse(user: UserDocument): AuthResponse {
        val token = jwtService.generateToken(user)
        val refreshToken = jwtService.generateRefreshToken(user)

        val userDto = UserDto(
            id = user.id,
            email = user.email,
            name = user.name
        )

        return AuthResponse(
            token = token,
            refreshToken = refreshToken,
            user = userDto
        )
    }
}
