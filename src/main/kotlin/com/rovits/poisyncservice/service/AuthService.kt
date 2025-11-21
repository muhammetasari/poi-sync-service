package com.rovits.poisyncservice.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import com.rovits.poisyncservice.constants.SecurityConstants
import com.rovits.poisyncservice.domain.document.UserDocument
import com.rovits.poisyncservice.domain.dto.*
import com.rovits.poisyncservice.domain.enums.AuthProvider
import com.rovits.poisyncservice.domain.enums.UserRole
import com.rovits.poisyncservice.exception.*
import com.rovits.poisyncservice.repository.UserRepository
import com.rovits.poisyncservice.util.MessageKeys
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder,
    private val tokenBlacklistService: TokenBlacklistService,
    private val rateLimitService: RateLimitService
) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    companion object {
        private const val MIN_PASSWORD_LENGTH = SecurityConstants.MIN_PASSWORD_LENGTH
        private val PASSWORD_BLACKLIST = SecurityConstants.PASSWORD_BLACKLIST
    }

    fun logout(accessToken: String, refreshToken: String?) {
        jwtService.getExpirationDateFromToken(accessToken)?.let {
            tokenBlacklistService.blacklistToken(accessToken, it.time)
        }
        refreshToken?.takeIf { it.isNotBlank() }?.let {
            jwtService.getExpirationDateFromToken(it)?.let { expiry ->
                tokenBlacklistService.blacklistToken(refreshToken, expiry.time)
            }
        }
        logger.info("Tokens blacklisted for logout")
    }

    @Transactional
    fun socialLogin(request: SocialLoginRequest): AuthResponse {
        logger.info("Processing social login: provider={}", request.provider)

        val provider = AuthProvider.fromString(request.provider)
            ?: throw ValidationException(
                errorCode = ErrorCodes.VALIDATION_PROVIDER,
                messageKey = MessageKeys.VALIDATION_PROVIDER,
                messageArgs = arrayOf(request.provider),
                fieldName = "provider"
            )

        val decodedToken = verifyFirebaseToken(request.firebaseToken)
        val email = decodedToken.email
            ?: throw ValidationException(
                errorCode = ErrorCodes.FIELD_REQUIRED,
                messageKey = MessageKeys.VALIDATION_REQUIRED,
                messageArgs = arrayOf("email"),
                fieldName = "email"
            )

        if (!decodedToken.isEmailVerified) {
            throw AuthenticationException(
                errorCode = ErrorCodes.AUTH_EMAIL_NOT_VERIFIED,
                messageKey = MessageKeys.AUTH_EMAIL_NOT_VERIFIED,
                messageArgs = arrayOf(email)
            )
        }

        val user = getOrCreateSocialUser(email, decodedToken.name, provider)
        logger.info("Social login successful: email={}", email)
        return generateAuthResponse(user)
    }

    private fun verifyFirebaseToken(firebaseToken: String): FirebaseToken {
        return try {
            FirebaseAuth.getInstance().verifyIdToken(firebaseToken)
        } catch (e: Exception) {
            logger.error("Firebase token verification failed", e)
            throw ExternalServiceException(
                errorCode = ErrorCodes.FIREBASE_TOKEN_INVALID,
                messageKey = MessageKeys.FIREBASE_TOKEN_INVALID,
                messageArgs = arrayOf("Firebase", e.message ?: "Invalid token"),
                serviceName = "Firebase",
                cause = e
            )
        }
    }

    private fun getOrCreateSocialUser(email: String, name: String?, provider: AuthProvider): UserDocument {
        return userRepository.findByEmail(email).map { existing ->
            if (existing.provider != provider) {
                throw BusinessException(
                    errorCode = ErrorCodes.VALIDATION_PROVIDER,
                    messageKey = MessageKeys.VALIDATION_PROVIDER,
                    messageArgs = arrayOf(provider.name, existing.provider.name)
                )
            }
            existing
        }.orElseGet {
            try {
                userRepository.save(UserDocument(
                    email = email,
                    name = name,
                    password = null,
                    provider = provider,
                    roles = setOf(UserRole.ROLE_USER)
                ))
            } catch (e: org.springframework.dao.DuplicateKeyException) {
                userRepository.findByEmail(email).orElseThrow {
                    BusinessException(
                        errorCode = ErrorCodes.USER_ALREADY_EXISTS,
                        messageKey = MessageKeys.USER_ALREADY_EXISTS,
                        messageArgs = arrayOf(email),
                        cause = e
                    )
                }
            }
        }
    }

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        logger.info("Processing registration: email={}", request.email)

        rateLimitService.checkAndIncrease(request.email)
        validatePassword(request.password)

        val newUser = UserDocument(
            email = request.email,
            name = request.name,
            password = passwordEncoder.encode(request.password),
            provider = AuthProvider.EMAIL,
            roles = setOf(UserRole.ROLE_USER)
        )

        val savedUser = try {
            userRepository.save(newUser)
        } catch (e: org.springframework.dao.DuplicateKeyException) {
            throw BusinessException(
                errorCode = ErrorCodes.USER_ALREADY_EXISTS,
                messageKey = MessageKeys.USER_ALREADY_EXISTS,
                messageArgs = arrayOf(request.email),
                cause = e
            )
        }

        logger.info("User registered: email={}", savedUser.email)
        return generateAuthResponse(savedUser)
    }

    @Transactional(readOnly = true)
    fun login(request: LoginRequest): AuthResponse {
        logger.info("Processing login: email={}", request.email)

        rateLimitService.checkAndIncrease(request.email)

        val user = userRepository.findByEmail(request.email)
            .orElseThrow {
                AuthenticationException(
                    errorCode = ErrorCodes.INVALID_CREDENTIALS,
                    messageKey = MessageKeys.INVALID_CREDENTIALS,
                    messageArgs = arrayOf(request.email)
                )
            }

        if (user.provider != AuthProvider.EMAIL) {
            throw AuthenticationException(
                errorCode = ErrorCodes.AUTH_PROVIDER_MISMATCH,
                messageKey = MessageKeys.AUTH_PROVIDER_MISMATCH,
                messageArgs = arrayOf(user.provider.name)
            )
        }

        if (user.password == null || !passwordEncoder.matches(request.password, user.password)) {
            throw AuthenticationException(
                errorCode = ErrorCodes.INVALID_CREDENTIALS,
                messageKey = MessageKeys.INVALID_CREDENTIALS,
                messageArgs = arrayOf(request.email)
            )
        }

        logger.info("Login successful: email={}", user.email)
        return generateAuthResponse(user)
    }

    private fun generateAuthResponse(user: UserDocument): AuthResponse {
        return AuthResponse(
            token = jwtService.generateToken(user),
            refreshToken = jwtService.generateRefreshToken(user),
            user = UserDto(id = user.id, email = user.email, name = user.name)
        )
    }
    private fun validatePassword(password: String) {
        when {
            password.length < MIN_PASSWORD_LENGTH ->
                throwPasswordPolicyException(SecurityConstants.PASSWORD_REASON_TOO_SHORT)
            !password.any { it.isDigit() } ->
                throwPasswordPolicyException(SecurityConstants.PASSWORD_REASON_MISSING_DIGIT)
            PASSWORD_BLACKLIST.contains(password.lowercase()) ->
                throwPasswordPolicyException(SecurityConstants.PASSWORD_REASON_BLACKLISTED)
            else -> return
        }
    }
    private fun throwPasswordPolicyException(reason: String): Nothing {
        throw ValidationException(
            errorCode = ErrorCodes.PASSWORD_POLICY,
            messageKey = MessageKeys.PASSWORD_POLICY,
            messageArgs = arrayOf(MIN_PASSWORD_LENGTH, reason),
            fieldName = "password"
        )
    }
}