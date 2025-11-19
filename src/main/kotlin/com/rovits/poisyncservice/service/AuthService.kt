package com.rovits.poisyncservice.service

import com.google.firebase.auth.FirebaseAuth
import com.rovits.poisyncservice.domain.document.UserDocument
import com.rovits.poisyncservice.domain.dto.*
import com.rovits.poisyncservice.exception.*
import com.rovits.poisyncservice.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder,
    private val tokenBlacklistService: TokenBlacklistService // YENİ: Inject edildi
) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    /**
     * Kullanıcı çıkış işlemi.
     * Access Token ve (opsiyonel) Refresh Token'ı kara listeye alır.
     */
    fun logout(accessToken: String, refreshToken: String?) {
        // 1. Access Token'ı blacklist'e al
        val accessExpiry = jwtService.getExpirationDateFromToken(accessToken)
        if (accessExpiry != null) {
            tokenBlacklistService.blacklistToken(accessToken, accessExpiry.time)
            logger.info("Access token blacklisted")
        }

        // 2. Refresh Token varsa onu da blacklist'e al
        if (!refreshToken.isNullOrBlank()) {
            val refreshExpiry = jwtService.getExpirationDateFromToken(refreshToken)
            if (refreshExpiry != null) {
                tokenBlacklistService.blacklistToken(refreshToken, refreshExpiry.time)
                logger.info("Refresh token blacklisted")
            }
        }
    }

    @Transactional
    fun socialLogin(request: SocialLoginRequest): AuthResponse {
        logger.info("Processing social login: provider={}", request.provider)

        val decodedToken = try {
            FirebaseAuth.getInstance().verifyIdToken(request.firebaseToken)
        } catch (e: Exception) {
            logger.error("Invalid Firebase token", e)
            throw ExternalServiceException(
                errorCode = ErrorCodes.FIREBASE_TOKEN_INVALID,
                messageKey = "error.firebase.token.invalid",
                serviceName = "Firebase",
                cause = e
            )
        }

        val email = decodedToken.email ?: throw ValidationException(
            errorCode = ErrorCodes.FIELD_REQUIRED,
            messageKey = "error.validation.required",
            messageArgs = arrayOf("email"),
            fieldName = "email"
        )

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
            throw BusinessException(
                errorCode = ErrorCodes.USER_ALREADY_EXISTS,
                messageKey = "error.user.already.exists",
                messageArgs = arrayOf(request.email)
            )
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
                AuthenticationException(
                    errorCode = ErrorCodes.INVALID_CREDENTIALS,
                    messageKey = "error.invalid.credentials"
                )
            }

        if (user.provider != "email" || user.password == null) {
            logger.warn("Login failed: Wrong provider ({}) - {}", user.provider, request.email)
            throw AuthenticationException(
                errorCode = ErrorCodes.INVALID_CREDENTIALS,
                messageKey = "error.invalid.credentials"
            )
        }

        if (!passwordEncoder.matches(request.password, user.password)) {
            logger.warn("Login failed: Wrong password - {}", request.email)
            throw AuthenticationException(
                errorCode = ErrorCodes.INVALID_CREDENTIALS,
                messageKey = "error.invalid.credentials"
            )
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