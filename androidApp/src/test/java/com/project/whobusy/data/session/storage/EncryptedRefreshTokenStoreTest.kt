package com.project.whobusy.data.session.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.GeneralSecurityException
import javax.crypto.KeyGenerator

@OptIn(ExperimentalCoroutinesApi::class)
class EncryptedRefreshTokenStoreTest {
    @Test
    fun storesOnlyEncryptedRefreshPayloadAndClearRemovesIt() = runTest {
        val preferences = MemoryPreferencesStore()
        val key = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
        val store =
            EncryptedRefreshTokenStore(preferences, AesGcmTokenCipher { key }, StandardTestDispatcher(testScheduler))
        val token = "test-only-refresh-payload"

        store.write(token)

        assertTrue("A refresh value must be encrypted", preferences.data.value.asMap().values.single() is ByteArray)
        assertTrue("Read decrypts the refresh payload", token == store.read())
        store.clear()
        assertNull(store.read())
        assertTrue(preferences.data.value.asMap().isEmpty())
    }

    @Test
    fun securityFailureClearsCorruptCiphertext() = runTest {
        val preferences =
            MemoryPreferencesStore(mutablePreferencesOf(EncryptedRefreshTokenStore.REFRESH_TOKEN to byteArrayOf(1)))
        val cipher = failingCipher { throw GeneralSecurityException("Invalid authentication tag") }
        val store = EncryptedRefreshTokenStore(preferences, cipher, StandardTestDispatcher(testScheduler))

        assertNull(store.read())
        assertTrue(preferences.data.value.asMap().isEmpty())
    }

    @Test
    fun malformedPreferenceTypeIsInvalidated() = runTest {
        val preferences = MemoryPreferencesStore(
            mutablePreferencesOf(stringPreferencesKey(EncryptedRefreshTokenStore.REFRESH_TOKEN.name) to "invalid-format"),
        )
        val store = EncryptedRefreshTokenStore(preferences, failingCipher(), StandardTestDispatcher(testScheduler))

        assertNull(store.read())
        assertTrue(preferences.data.value.asMap().isEmpty())
    }

    @Test
    fun invalidUtf8PlaintextIsInvalidatedAndTemporaryBytesAreWiped() = runTest {
        val preferences =
            MemoryPreferencesStore(mutablePreferencesOf(EncryptedRefreshTokenStore.REFRESH_TOKEN to byteArrayOf(1)))
        val decrypted = byteArrayOf(0xc3.toByte(), 0x28)
        val store =
            EncryptedRefreshTokenStore(preferences, failingCipher { decrypted }, StandardTestDispatcher(testScheduler))

        assertNull(store.read())
        assertTrue(preferences.data.value.asMap().isEmpty())
        assertTrue("Temporary plaintext must be wiped after decoding", decrypted.all { it == 0.toByte() })
    }

    @Test
    fun readCancellationPropagatesWithoutInvalidatingPersistedCiphertext() = runTest {
        val preferences =
            MemoryPreferencesStore(mutablePreferencesOf(EncryptedRefreshTokenStore.REFRESH_TOKEN to byteArrayOf(1)))
        val store = EncryptedRefreshTokenStore(
            preferences,
            failingCipher { throw CancellationException("Cancelled") },
            StandardTestDispatcher(testScheduler),
        )
        var cancelled = false
        try {
            store.read()
        } catch (_: CancellationException) {
            cancelled = true
        }

        assertTrue("Cancellation must escape the storage boundary", cancelled)
        assertFalse("Cancellation must not clear valid persisted state", preferences.data.value.asMap().isEmpty())
    }

    @Test
    fun writeCancellationPropagatesAndDoesNotReplacePersistedCiphertext() = runTest {
        val existing = byteArrayOf(1, 2, 3)
        val preferences =
            MemoryPreferencesStore(mutablePreferencesOf(EncryptedRefreshTokenStore.REFRESH_TOKEN to existing))
        val cipher = object : TokenCipher {
            override fun encrypt(plaintext: ByteArray): ByteArray = throw CancellationException("Cancelled")
            override fun decrypt(payload: ByteArray): ByteArray = error("Unexpected decryption")
        }
        val store = EncryptedRefreshTokenStore(preferences, cipher, StandardTestDispatcher(testScheduler))
        var cancelled = false
        try {
            store.write("test-only-new-refresh")
        } catch (_: CancellationException) {
            cancelled = true
        }

        assertTrue("Cancellation must escape the storage boundary", cancelled)
        assertTrue(
            "The previous atomic value must remain intact",
            existing.contentEquals(preferences.data.value[EncryptedRefreshTokenStore.REFRESH_TOKEN])
        )
    }

    private fun failingCipher(decrypt: (ByteArray) -> ByteArray = { error("Unexpected decryption") }): TokenCipher =
        object : TokenCipher {
            override fun encrypt(plaintext: ByteArray): ByteArray = error("Unexpected encryption")
            override fun decrypt(payload: ByteArray): ByteArray = decrypt.invoke(payload)
        }

    private class MemoryPreferencesStore(initial: Preferences = emptyPreferences()) : DataStore<Preferences> {
        override val data = MutableStateFlow(initial)
        private val mutex = Mutex()

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
            mutex.withLock {
                transform(data.value).also { data.value = it }
            }
    }
}
