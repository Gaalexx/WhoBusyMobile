package com.project.whobusy.data.session.storage

/** Blocking encryption boundary. Callers perform these operations on an IO dispatcher. */
interface TokenCipher {
    fun encrypt(plaintext: ByteArray): ByteArray

    /** Rejects an invalid envelope, unavailable key, or failed authentication with a security exception. */
    fun decrypt(payload: ByteArray): ByteArray
}
