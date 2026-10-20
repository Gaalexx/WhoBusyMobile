package com.project.whobusy.data.session.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.project.whobusy.di.ApplicationContext
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.security.GeneralSecurityException

@SingleIn(AppScope::class)
class EncryptedRefreshTokenStore internal constructor(
    private val dataStore: DataStore<Preferences>,
    private val cipher: TokenCipher,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : RefreshTokenStore {
    @Inject
    constructor(
        @ApplicationContext context: Context,
        cipher: AndroidKeystoreTokenCipher,
    ) : this(createDataStore(context), cipher)

    override suspend fun read(): String? = withContext(ioDispatcher) {
        var token: String? = null
        // Decrypt and invalidate in one transaction, so a concurrent write cannot be removed by a stale read.
        dataStore.updateData { preferences ->
            var plaintext: ByteArray? = null
            try {
                val payload = preferences[REFRESH_TOKEN] ?: return@updateData preferences
                val decrypted = cipher.decrypt(payload)
                plaintext = decrypted
                token = Charsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(decrypted)).toString()
                if (token.isNullOrBlank()) throw GeneralSecurityException("Empty session payload")
                preferences
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: GeneralSecurityException) {
                token = null
                preferences.toMutablePreferences().apply { remove(REFRESH_TOKEN) }
            } catch (_: CharacterCodingException) {
                token = null
                preferences.toMutablePreferences().apply { remove(REFRESH_TOKEN) }
            } catch (_: ClassCastException) {
                token = null
                preferences.toMutablePreferences().apply { remove(REFRESH_TOKEN) }
            } finally {
                plaintext?.fill(0)
            }
        }
        token
    }

    override suspend fun write(token: String) {
        require(token.isNotBlank()) { "Refresh payload must not be blank" }
        withContext(ioDispatcher) {
            val plaintext = token.toByteArray(Charsets.UTF_8)
            try {
                dataStore.edit { preferences -> preferences[REFRESH_TOKEN] = cipher.encrypt(plaintext) }
            } finally {
                plaintext.fill(0)
            }
        }
    }

    override suspend fun clear() {
        withContext(ioDispatcher) { dataStore.edit { it.remove(REFRESH_TOKEN) } }
    }

    internal companion object {
        val REFRESH_TOKEN = byteArrayPreferencesKey("encrypted_refresh_token")

        fun storageFile(context: Context): File =
            File(context.noBackupFilesDir, "datastore/auth_session.preferences_pb")

        fun createDataStore(
            context: Context,
            scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
        ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
            scope = scope,
            produceFile = { storageFile(context) },
        )
    }
}
