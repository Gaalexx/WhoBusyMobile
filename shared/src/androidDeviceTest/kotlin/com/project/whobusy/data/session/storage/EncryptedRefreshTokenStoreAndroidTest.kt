package com.project.whobusy.data.session.storage

import android.content.Context
import android.content.ContextWrapper
import android.os.Looper
import android.security.keystore.KeyInfo
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.security.KeyStore
import java.util.UUID
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory

@RunWith(AndroidJUnit4::class)
class EncryptedRefreshTokenStoreAndroidTest {
    private lateinit var context: Context
    private lateinit var keyAlias: String
    private val jobs = mutableListOf<Job>()

    @Before
    fun setUp() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(base.noBackupFilesDir, "refresh-storage-tests/${UUID.randomUUID()}")
        context = object : ContextWrapper(base) {
            override fun getNoBackupFilesDir(): File = directory
        }
        keyAlias = "com.project.whobusy.test.refresh.${UUID.randomUUID()}"
    }

    @After
    fun tearDown() = runBlocking {
        jobs.forEach { it.cancelAndJoin() }
        // Remove only the disposable key created by this test; no application files are deleted.
        withContext(Dispatchers.IO) { keyStore().deleteEntry(keyAlias) }
    }

    @Test
    fun encryptedDataStoreRoundtripReopensWithNonExportableAes256KeyAndRunsOffMain() = runBlocking {
        val keystoreCipher = AndroidKeystoreTokenCipher(keyAlias)
        val checkingCipher = object : TokenCipher {
            override fun encrypt(plaintext: ByteArray): ByteArray {
                assertTrue("Encryption must run off the main thread", Looper.myLooper() != Looper.getMainLooper())
                return keystoreCipher.encrypt(plaintext)
            }

            override fun decrypt(payload: ByteArray): ByteArray {
                assertTrue("Decryption must run off the main thread", Looper.myLooper() != Looper.getMainLooper())
                return keystoreCipher.decrypt(payload)
            }
        }
        val first = openStore(checkingCipher)
        val token = fixtureToken()
        withContext(Dispatchers.Main) {
            first.store.write(token)
            assertTrue("The stored refresh value must roundtrip", first.store.read() == token)
        }
        val key = withContext(Dispatchers.IO) { keyStore().getKey(keyAlias, null) as SecretKey }
        assertNull("AndroidKeyStore must not expose key material", key.encoded)
        val keyInfo =
            SecretKeyFactory.getInstance("AES", "AndroidKeyStore").getKeySpec(key, KeyInfo::class.java) as KeyInfo
        assertTrue("Refresh encryption requires an AES-256 key", keyInfo.keySize == 256)

        first.job.cancelAndJoin()
        val reopened = openStore()
        assertTrue("The encrypted value must survive a DataStore reopen", reopened.store.read() == token)
    }

    @Test
    fun plaintextIsAbsentFromTheNoBackupPreferencesFile() = runBlocking {
        val fixture = openStore()
        val token = fixtureToken()
        fixture.store.write(token)
        val file = EncryptedRefreshTokenStore.storageFile(context)

        assertTrue(
            "Session storage must stay inside noBackupFilesDir",
            file.canonicalPath.startsWith(context.noBackupFilesDir.canonicalPath + File.separator)
        )
        assertTrue("The DataStore write must be durable", file.isFile)
        val bytes = withContext(Dispatchers.IO) { file.readBytes() }
        assertFalse(
            "The persisted file must not contain the refresh plaintext",
            bytes.containsBytes(token.toByteArray())
        )
        assertTrue(
            "The preferences file contains only one encrypted value",
            fixture.dataStore.data.first().asMap().values.single() is ByteArray
        )
    }

    @Test
    fun identicalWritesProduceDifferentPersistedNonces() = runBlocking {
        val fixture = openStore()
        val token = fixtureToken()
        fixture.store.write(token)
        val first = requireNotNull(fixture.dataStore.data.first()[EncryptedRefreshTokenStore.REFRESH_TOKEN])
        fixture.store.write(token)
        val second = requireNotNull(fixture.dataStore.data.first()[EncryptedRefreshTokenStore.REFRESH_TOKEN])

        assertFalse("Refresh writes must never reuse an IV", first.iv().contentEquals(second.iv()))
        assertTrue("The most recent value remains readable", fixture.store.read() == token)
    }

    @Test
    fun tamperedCiphertextIsInvalidatedAndRemainsClearedAfterReopen() = runBlocking {
        val fixture = openStore()
        fixture.store.write(fixtureToken())
        fixture.dataStore.edit { preferences ->
            val payload = requireNotNull(preferences[EncryptedRefreshTokenStore.REFRESH_TOKEN]).copyOf()
            payload[payload.lastIndex] = (payload.last().toInt() xor 1).toByte()
            preferences[EncryptedRefreshTokenStore.REFRESH_TOKEN] = payload
        }

        assertNull(fixture.store.read())
        assertNull(fixture.dataStore.data.first()[EncryptedRefreshTokenStore.REFRESH_TOKEN])
        fixture.job.cancelAndJoin()
        assertNull(openStore().store.read())
    }

    @Test
    fun missingKeystoreKeyClearsRefreshWithoutRecreatingTheKeyDuringRead() = runBlocking {
        val fixture = openStore()
        fixture.store.write(fixtureToken())
        withContext(Dispatchers.IO) { keyStore().deleteEntry(keyAlias) }

        assertNull(fixture.store.read())
        assertNull(fixture.dataStore.data.first()[EncryptedRefreshTokenStore.REFRESH_TOKEN])
        assertFalse(
            "A read must not replace a lost key",
            withContext(Dispatchers.IO) { keyStore().containsAlias(keyAlias) }
        )
        val replacement = fixtureToken()
        fixture.store.write(replacement)
        assertTrue("A new login can create a fresh key", fixture.store.read() == replacement)
    }

    @Test
    fun clearRemovesCiphertextButRetainsTheKeystoreKey() = runBlocking {
        val fixture = openStore()
        fixture.store.write(fixtureToken())

        fixture.store.clear()

        assertNull(fixture.store.read())
        assertTrue(fixture.dataStore.data.first().asMap().isEmpty())
        assertTrue(
            "Logout must retain the reusable Keystore alias",
            withContext(Dispatchers.IO) { keyStore().containsAlias(keyAlias) }
        )
    }

    @Test
    fun corruptPreferencesFileIsReplacedWithAnEmptyStore() = runBlocking {
        val fixture = openStore()
        fixture.store.write(fixtureToken())
        fixture.job.cancelAndJoin()
        withContext(Dispatchers.IO) {
            EncryptedRefreshTokenStore.storageFile(context).writeBytes(byteArrayOf(0xff.toByte(), 0xff.toByte()))
        }

        val reopened = openStore()
        assertNull(reopened.store.read())
        assertTrue(reopened.dataStore.data.first().asMap().isEmpty())
    }

    private fun openStore(cipher: TokenCipher = AndroidKeystoreTokenCipher(keyAlias)): StoreFixture {
        val job = SupervisorJob().also(jobs::add)
        val dataStore = EncryptedRefreshTokenStore.createDataStore(context, CoroutineScope(job + Dispatchers.IO))
        return StoreFixture(dataStore, EncryptedRefreshTokenStore(dataStore, cipher), job)
    }

    private fun keyStore(): KeyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    private fun fixtureToken(): String = "test-only-refresh-${UUID.randomUUID()}"

    private fun ByteArray.iv(): ByteArray =
        copyOfRange(AesGcmTokenCipher.HEADER_SIZE, AesGcmTokenCipher.HEADER_SIZE + AesGcmTokenCipher.IV_SIZE)

    private fun ByteArray.containsBytes(value: ByteArray): Boolean =
        (0..(size - value.size)).any { start -> value.indices.all { this[start + it] == value[it] } }

    private data class StoreFixture(
        val dataStore: DataStore<Preferences>,
        val store: EncryptedRefreshTokenStore,
        val job: Job,
    )
}
