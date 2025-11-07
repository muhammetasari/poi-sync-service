package com.rovits.poisyncservice.service

import com.google.firebase.auth.FirebaseAuth
import com.rovits.poisyncservice.domain.document.UserDocument
import com.rovits.poisyncservice.domain.dto.* // RegisterRequest import'u
import com.rovits.poisyncservice.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.BadCredentialsException // Yeni import
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
        // ... (Bu metot Adım 5'te yazıldığı gibi kalıyor) ...
        logger.info("Sosyal giriş isteği alınıyor: provider=${request.provider}")

        val decodedToken = try {
            FirebaseAuth.getInstance().verifyIdToken(request.firebaseToken)
        } catch (e: Exception) {
            logger.error("❌ Geçersiz Firebase token: ${e.message}")
            throw IllegalArgumentException("Geçersiz Firebase token")
        }

        val email = decodedToken.email ?: throw IllegalArgumentException("Token'dan email alınamadı")
        val name = decodedToken.name

        val user = userRepository.findByEmail(email).orElseGet {
            logger.info("Yeni kullanıcı oluşturuluyor: $email")
            val newUser = UserDocument(
                email = email,
                name = name,
                password = null,
                provider = request.provider
            )
            userRepository.save(newUser)
        }

        logger.info("Kullanıcı girişi başarılı: $email")
        return generateAuthResponse(user)
    }

    // YENİ EKLENEN METOT: KAYIT OLMA
    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        logger.info("Yeni kullanıcı kayıt isteği: ${request.email}")

        // 1. Kullanıcı zaten var mı?
        if (userRepository.findByEmail(request.email).isPresent) {
            logger.warn("Kayıt hatası: Email zaten kullanılıyor - ${request.email}")
            throw IllegalArgumentException("Bu e-posta adresi zaten kullanılıyor.")
        }

        // 2. Şifreyi hash'le
        val hashedPassword = passwordEncoder.encode(request.password)

        // 3. Yeni kullanıcıyı oluştur
        val newUser = UserDocument(
            email = request.email,
            name = request.name,
            password = hashedPassword, // Hash'lenmiş şifre
            provider = "email" // Sağlayıcı
        )

        // 4. Kullanıcıyı kaydet
        val savedUser = userRepository.save(newUser)
        logger.info("Yeni kullanıcı başarıyla kaydedildi: ${savedUser.email} (ID: ${savedUser.id})")

        // 5. Token üretip cevap dön (kayıt sonrası otomatik giriş)
        return generateAuthResponse(savedUser)
    }

    // YENİ EKLENEN METOT: GİRİŞ YAPMA
    @Transactional(readOnly = true) // Sadece okuma işlemi
    fun login(request: LoginRequest): AuthResponse {
        logger.info("Giriş isteği: ${request.email}")

        // 1. Kullanıcıyı bul
        val user = userRepository.findByEmail(request.email)
            .orElseThrow {
                logger.warn("Giriş hatası: Kullanıcı bulunamadı - ${request.email}")
                BadCredentialsException("Geçersiz e-posta veya şifre.")
            }

        // 2. Sağlayıcıyı kontrol et (Google ile giren şifreyle giremez)
        if (user.provider != "email" || user.password == null) {
            logger.warn("Giriş hatası: Yanlış sağlayıcı (${user.provider}) - ${request.email}")
            throw BadCredentialsException("Bu hesap ile ${user.provider} üzerinden giriş yapılmıştır.")
        }

        // 3. Şifreyi kontrol et
        if (!passwordEncoder.matches(request.password, user.password)) {
            logger.warn("Giriş hatası: Yanlış şifre - ${request.email}")
            throw BadCredentialsException("Geçersiz e-posta veya şifre.")
        }

        logger.info("Kullanıcı girişi başarılı: ${user.email}")

        // 4. Token üretip cevap dön
        return generateAuthResponse(user)
    }

    private fun generateAuthResponse(user: UserDocument): AuthResponse {
        // ... (Bu metot Adım 5'te yazıldığı gibi kalıyor) ...
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