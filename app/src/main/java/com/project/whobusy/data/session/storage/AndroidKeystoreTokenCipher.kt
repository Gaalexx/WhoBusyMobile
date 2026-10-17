package com.project.whobusy.data.session.storage

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.security.UnrecoverableKeyException
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidKeystoreTokenCipher internal constructor(
    private val keyAlias: String,
) : TokenCipher {
    @Inject
    constructor() : this("com.project.whobusy.refresh-token.aes-gcm.v1")

    private val delegate = AesGcmTokenCipher(::key)

    override fun encrypt(plaintext: ByteArray): ByteArray = delegate.encrypt(plaintext)

    override fun decrypt(payload: ByteArray): ByteArray = delegate.decrypt(payload)

    private fun key(createIfMissing: Boolean): SecretKey? = synchronized(KEY_LOCK) {
        val store = KeyStore.getInstance(PROVIDER).apply { load(null) }
        val existing = store.getKey(keyAlias, null)
        if (existing != null) {
            return@synchronized existing as? SecretKey
                ?: throw UnrecoverableKeyException("Invalid session key type")
        }
        if (!createIfMissing) return@synchronized null
        val parameters = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        ).setKeySize(256)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .setUserAuthenticationRequired(false)
            .build()
        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER).apply { init(parameters) }.generateKey()
    }

    private companion object {
        const val PROVIDER = "AndroidKeyStore"
        val KEY_LOCK = Any()
    }
}
