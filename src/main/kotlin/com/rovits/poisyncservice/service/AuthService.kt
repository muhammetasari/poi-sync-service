package com.rovits.poisyncservice.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import com.rovits.poisyncservice.domain.document.UserDocument
import com.rovits.poisyncservice.domain.dto.*
import com.rovits.poisyncservice.exception.*
import com.rovits.poisyncservice.repository.UserRepository
import com.rovits.poisyncservice.util.MessageKeys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val tokenBlacklistService: TokenBlacklistService,
    private val rateLimitService: RateLimitService,
    @Value("\${app.initial-admin-email:}") private val initialAdminEmail: String
) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    companion object {
        private const val ROLE_USER = "user"
        private const val ROLE_ADMIN = "admin"
        private const val PROVIDER_PASSWORD = "password"
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

    /**
     * Unified login for both email/password and social providers
     * Client must authenticate with Firebase first and send the ID token
     */
    @Transactional
    fun login(request: LoginRequest): AuthResponse {
        logger.info("Processing login with Firebase token")

        val decodedToken = verifyFirebaseToken(request.firebaseToken)
        val email = decodedToken.email
            ?: throw ValidationException(
                errorCode = ErrorCodes.FIELD_REQUIRED,
                messageKey = MessageKeys.VALIDATION_REQUIRED,
                messageArgs = arrayOf("email"),
                fieldName = "email"
            )

        // Check email verification for password provider only
        // Firebase SDK doesn't expose provider directly, check if email verified is required
        // For social providers, email is automatically verified
        val isEmailAuth = decodedToken.claims["firebase"]?.let {
            (it as? Map<*, *>)?.get("sign_in_provider") as? String
        } == PROVIDER_PASSWORD

        if (isEmailAuth && !decodedToken.isEmailVerified) {
            throw AuthenticationException(
                errorCode = ErrorCodes.AUTH_EMAIL_NOT_VERIFIED,
                messageKey = MessageKeys.AUTH_EMAIL_NOT_VERIFIED,
                messageArgs = arrayOf(email)
            )
        }

        rateLimitService.checkAndIncrease(email)

        val user = getOrCreateUser(decodedToken)
        logger.info("Login successful: email={}, provider={}", email, user.authProvider)
        return generateAuthResponse(user)
    }

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        logger.info("Processing registration with Firebase token")

        val decodedToken = verifyFirebaseToken(request.firebaseToken)
        val email = decodedToken.email
            ?: throw ValidationException(
                errorCode = ErrorCodes.FIELD_REQUIRED,
                messageKey = MessageKeys.VALIDATION_REQUIRED,
                messageArgs = arrayOf("email"),
                fieldName = "email"
            )

        rateLimitService.checkAndIncrease(email)

        // Check if user already exists
        if (userRepository.findByFirebaseUid(decodedToken.uid).isPresent) {
            throw BusinessException(
                errorCode = ErrorCodes.USER_ALREADY_EXISTS,
                messageKey = MessageKeys.USER_ALREADY_EXISTS,
                messageArgs = arrayOf(email)
            )
        }

        val user = createUserFromToken(decodedToken)

        // Set default role as "user" in Firebase custom claims
        setFirebaseCustomClaims(decodedToken.uid, ROLE_USER)

        logger.info("User registered: email={}, uid={}", email, decodedToken.uid)
        return generateAuthResponse(user)
    }

    /**
     * Send password reset email via Firebase
     */
    fun sendPasswordResetEmail(request: SendPasswordResetRequest) {
        logger.info("Sending password reset email to: {}", request.email)

        try {
            FirebaseAuth.getInstance().generatePasswordResetLink(request.email)
            logger.info("Password reset link generated for: {}", request.email)
            // Firebase automatically sends the email based on template configuration
        } catch (e: Exception) {
            logger.error("Failed to generate password reset link", e)
            throw ExternalServiceException(
                errorCode = ErrorCodes.FIREBASE_FAILED,
                messageKey = MessageKeys.FIREBASE_FAILED,
                messageArgs = arrayOf("Firebase", e.message ?: "Failed to send password reset email"),
                serviceName = "Firebase",
                cause = e
            )
        }
    }

    /**
     * Send email verification via Firebase
     */
    fun sendEmailVerification(request: SendEmailVerificationRequest) {
        logger.info("Sending email verification")

        val decodedToken = verifyFirebaseToken(request.firebaseToken)
        val email = decodedToken.email ?: throw ValidationException(
            errorCode = ErrorCodes.FIELD_REQUIRED,
            messageKey = MessageKeys.VALIDATION_REQUIRED,
            messageArgs = arrayOf("email"),
            fieldName = "email"
        )

        try {
            FirebaseAuth.getInstance().generateEmailVerificationLink(email)
            logger.info("Email verification link generated for: {}", email)
            // Firebase automatically sends the email based on template configuration
        } catch (e: Exception) {
            logger.error("Failed to generate email verification link", e)
            throw ExternalServiceException(
                errorCode = ErrorCodes.FIREBASE_FAILED,
                messageKey = MessageKeys.FIREBASE_FAILED,
                messageArgs = arrayOf("Firebase", e.message ?: "Failed to send verification email"),
                serviceName = "Firebase",
                cause = e
            )
        }
    }

    /**
     * Update user role (admin only operation)
     */
    @Transactional
    fun updateUserRole(userId: String, request: UpdateUserRoleRequest) {
        logger.info("Updating user role: userId={}, newRole={}", userId, request.role)

        val user = userRepository.findById(userId).orElseThrow {
            ResourceNotFoundException(
                ErrorCodes.USER_NOT_FOUND,
                MessageKeys.USER_NOT_FOUND,
                arrayOf(userId)
            )
        }

        // Update Firebase custom claims
        setFirebaseCustomClaims(user.firebaseUid, request.role)

        // Update MongoDB
        val updatedUser = user.copy(
            role = request.role,
            updatedAt = System.currentTimeMillis()
        )
        userRepository.save(updatedUser)

        logger.info("User role updated: userId={}, newRole={}", userId, request.role)
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

    private fun getOrCreateUser(decodedToken: FirebaseToken): UserDocument {
        return userRepository.findByFirebaseUid(decodedToken.uid)
            .orElseGet { createUserFromToken(decodedToken) }
    }

    private fun createUserFromToken(decodedToken: FirebaseToken): UserDocument {
        val email = decodedToken.email!!
        val name = decodedToken.name
        // Extract provider from Firebase claims
        val provider = decodedToken.claims["firebase"]?.let {
            (it as? Map<*, *>)?.get("sign_in_provider") as? String
        } ?: PROVIDER_PASSWORD

        // Check if this should be the initial admin
        val role = if (initialAdminEmail.isNotBlank() && email.equals(initialAdminEmail, ignoreCase = true)) {
            logger.info("Setting initial admin role for: {}", email)
            ROLE_ADMIN
        } else {
            ROLE_USER
        }

        val newUser = UserDocument(
            firebaseUid = decodedToken.uid,
            email = email,
            name = name,
            authProvider = provider,
            role = role
        )

        return try {
            val saved = userRepository.save(newUser)

            // Set custom claims in Firebase if admin
            if (role == ROLE_ADMIN) {
                setFirebaseCustomClaims(decodedToken.uid, role)
            }

            saved
        } catch (e: org.springframework.dao.DuplicateKeyException) {
            userRepository.findByFirebaseUid(decodedToken.uid).orElseThrow {
                BusinessException(
                    errorCode = ErrorCodes.USER_ALREADY_EXISTS,
                    messageKey = MessageKeys.USER_ALREADY_EXISTS,
                    messageArgs = arrayOf(email),
                    cause = e
                )
            }
        }
    }

    private fun setFirebaseCustomClaims(firebaseUid: String, role: String) {
        try {
            val claims = mapOf("role" to role)
            FirebaseAuth.getInstance().setCustomUserClaims(firebaseUid, claims)
            logger.info("Custom claims set for user: uid={}, role={}", firebaseUid, role)
        } catch (e: Exception) {
            logger.error("Failed to set custom claims", e)
            // Don't throw exception, just log the error
        }
    }

    private fun generateAuthResponse(user: UserDocument): AuthResponse {
        return AuthResponse(
            token = jwtService.generateToken(user),
            refreshToken = jwtService.generateRefreshToken(user),
            user = UserDto(
                id = user.id,
                email = user.email,
                name = user.name,
                role = user.role
            )
        )
    }
}