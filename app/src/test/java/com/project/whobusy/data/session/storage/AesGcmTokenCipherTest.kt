package com.project.whobusy.data.session.storage

import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.GeneralSecurityException
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class AesGcmTokenCipherTest {
    @Test
    fun roundtripPreservesOpaqueUtf8PayloadWithoutStoringPlaintext() {
        val key = newKey()
        val cipher = AesGcmTokenCipher { key }
        val plaintext = "test-only-refresh-payload-данные".toByteArray(Charsets.UTF_8)

        val encrypted = cipher.encrypt(plaintext)
        val decrypted = cipher.decrypt(encrypted)

        assertTrue("Authenticated payload roundtrip", plaintext.contentEquals(decrypted))
        assertFalse("The encrypted envelope must not contain the plaintext", encrypted.containsBytes(plaintext))
    }

    @Test
    fun repeatedEncryptionUsesDifferentNoncesAndCiphertexts() {
        val key = newKey()
        val cipher = AesGcmTokenCipher { key }
        val plaintext = "test-only-refresh-payload".toByteArray()

        val first = cipher.encrypt(plaintext)
        val second = cipher.encrypt(plaintext)

        assertFalse("Every write requires a new IV", first.iv().contentEquals(second.iv()))
        assertFalse("Repeated plaintext must produce distinct ciphertexts", first.contentEquals(second))
        assertTrue("First ciphertext remains readable", cipher.decrypt(first).contentEquals(plaintext))
        assertTrue("Second ciphertext remains readable", cipher.decrypt(second).contentEquals(plaintext))
    }

    @Test
    fun tamperingWithHeaderVersionIvCiphertextOrTagIsRejected() {
        val key = newKey()
        val cipher = AesGcmTokenCipher { key }
        val encrypted = cipher.encrypt("test-only-refresh-payload".toByteArray())
        val offsets = listOf(
            0, AesGcmTokenCipher.HEADER_SIZE - 1, AesGcmTokenCipher.HEADER_SIZE,
            AesGcmTokenCipher.HEADER_SIZE + AesGcmTokenCipher.IV_SIZE, encrypted.lastIndex
        )

        offsets.forEach { offset ->
            val tampered = encrypted.copyOf().apply { this[offset] = (this[offset].toInt() xor 1).toByte() }
            assertThrows(GeneralSecurityException::class.java) { cipher.decrypt(tampered) }
        }
    }

    @Test
    fun truncatedAndOversizedEnvelopesAreRejectedBeforeKeyLookup() {
        var keyRequested = false
        val cipher = AesGcmTokenCipher {
            keyRequested = true
            newKey()
        }

        listOf(byteArrayOf(), ByteArray(5), ByteArray(33), ByteArray(AesGcmTokenCipher.MAX_PLAINTEXT_SIZE + 100))
            .forEach { invalid ->
                assertThrows(GeneralSecurityException::class.java) { cipher.decrypt(invalid) }
            }

        assertFalse("Invalid envelopes must not access or create a key", keyRequested)
    }

    @Test
    fun missingKeyInvalidatesCiphertextWithoutGeneratingAReplacementDuringRead() {
        var key: SecretKey? = newKey()
        var createRequests = 0
        val cipher = AesGcmTokenCipher { createIfMissing ->
            if (createIfMissing) createRequests++
            key
        }
        val encrypted = cipher.encrypt("test-only-refresh-payload".toByteArray())
        key = null

        assertThrows(GeneralSecurityException::class.java) { cipher.decrypt(encrypted) }
        assertTrue("Only encryption may request key creation", createRequests == 1)
    }

    @Test
    fun replacedKeyCannotAuthenticatePreviouslyWrittenCiphertext() {
        var key = newKey()
        val cipher = AesGcmTokenCipher { key }
        val plaintext = "test-only-refresh-payload".toByteArray()
        val encrypted = cipher.encrypt(plaintext)
        key = newKey()

        assertThrows(GeneralSecurityException::class.java) { cipher.decrypt(encrypted) }
        assertTrue(
            "New sessions can use the replacement key",
            cipher.decrypt(cipher.encrypt(plaintext)).contentEquals(plaintext)
        )
    }

    private fun newKey(): SecretKey = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()

    private fun ByteArray.iv(): ByteArray =
        copyOfRange(AesGcmTokenCipher.HEADER_SIZE, AesGcmTokenCipher.HEADER_SIZE + AesGcmTokenCipher.IV_SIZE)

    private fun ByteArray.containsBytes(value: ByteArray): Boolean =
        (0..(size - value.size)).any { start -> value.indices.all { this[start + it] == value[it] } }
}
