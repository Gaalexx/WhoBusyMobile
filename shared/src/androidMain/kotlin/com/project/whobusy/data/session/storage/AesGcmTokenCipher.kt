package com.project.whobusy.data.session.storage

import java.security.GeneralSecurityException
import java.security.UnrecoverableKeyException
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Versioned envelope: magic (4), version (1), random IV (12), ciphertext and authentication tag (16). */
internal class AesGcmTokenCipher(
    private val keyProvider: (createIfMissing: Boolean) -> SecretKey?,
) : TokenCipher {
    override fun encrypt(plaintext: ByteArray): ByteArray {
        require(plaintext.size in 1..MAX_PLAINTEXT_SIZE) { "Invalid refresh payload size" }
        val key = keyProvider(true) ?: throw UnrecoverableKeyException("Session key is unavailable")
        val cipher = Cipher.getInstance(TRANSFORMATION)
        // The provider chooses the IV; AndroidKeyStore also enforces randomized encryption.
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        if (iv.size != IV_SIZE) throw GeneralSecurityException("Unexpected session IV size")
        cipher.updateAAD(PURPOSE)
        cipher.updateAAD(HEADER)
        return HEADER + iv + cipher.doFinal(plaintext)
    }

    override fun decrypt(payload: ByteArray): ByteArray {
        val minimumSize = HEADER_SIZE + IV_SIZE + TAG_SIZE + 1
        if (payload.size !in minimumSize..(HEADER_SIZE + IV_SIZE + TAG_SIZE + MAX_PLAINTEXT_SIZE) ||
            !payload.copyOfRange(0, HEADER_SIZE).contentEquals(HEADER)
        ) {
            throw GeneralSecurityException("Invalid encrypted session envelope")
        }
        // Reading must not silently create a replacement for a lost Keystore key.
        val key = keyProvider(false) ?: throw UnrecoverableKeyException("Session key is unavailable")
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            key,
            GCMParameterSpec(TAG_SIZE * 8, payload.copyOfRange(HEADER_SIZE, HEADER_SIZE + IV_SIZE)),
        )
        cipher.updateAAD(PURPOSE)
        cipher.updateAAD(payload, 0, HEADER_SIZE)
        return cipher.doFinal(payload, HEADER_SIZE + IV_SIZE, payload.size - HEADER_SIZE - IV_SIZE)
    }

    internal companion object {
        const val HEADER_SIZE = 5
        const val IV_SIZE = 12
        const val TAG_SIZE = 16
        const val MAX_PLAINTEXT_SIZE = 65_536
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private val HEADER = byteArrayOf(0x57, 0x42, 0x52, 0x54, 0x01)
        private val PURPOSE = "com.project.whobusy.refresh-token".toByteArray(Charsets.UTF_8)
    }
}
