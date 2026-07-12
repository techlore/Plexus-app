/*
 *     Copyright (C) 2022-present Techlore
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package tech.techlore.plexus.keystore

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.io.encoding.Base64

class KeyStoreManager {
    
    private companion object {
        private const val PROVIDER = "AndroidKeyStore"
        private const val KEYSTORE_ALIAS = "tech.techlore.plexus.my_rating_tokens_alias"
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
    }
    
    private val keyStore =
        KeyStore.getInstance(PROVIDER).apply { load(null) }
    
    private fun getOrCreateKey(): SecretKey {
        return getKey() ?: createKey()
    }
    
    private fun getKey(): SecretKey? {
        return keyStore.getKey(KEYSTORE_ALIAS, null)?.let { it as SecretKey }
    }
    
    private fun createKey(): SecretKey {
        return KeyGenerator.getInstance(ALGORITHM, PROVIDER).apply {
            init(
                KeyGenParameterSpec
                    .Builder(
                        KEYSTORE_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                    .setBlockModes(BLOCK_MODE)
                    .setEncryptionPaddings(PADDING)
                    .setKeySize(256)
                    .setRandomizedEncryptionRequired(true)
                    .build()
            )
        }.generateKey()
    }
    
    fun encryptToken(token: String): String {
        val cipher =
            Cipher.getInstance(TRANSFORMATION).apply {
                init(Cipher.ENCRYPT_MODE, getOrCreateKey())
            }
        
        return cipher
            .doFinal(token.toByteArray(Charsets.UTF_8))
            .let {
                // Store IV along with encrypted data,
                // because IV is required later while decrypting
                Base64.encode(cipher.iv + it)
            }
    }
    
    fun decryptToken(encTokenBase64: String): String {
        val encBytes = Base64.decode(encTokenBase64)
        val cipher =
            Cipher.getInstance(TRANSFORMATION).apply {
                init(
                    Cipher.DECRYPT_MODE,
                    getOrCreateKey(),
                    GCMParameterSpec(
                        128, // GCM requires 128 auth tag length by default
                        encBytes,
                        0,
                        12 // First 12 bytes are IV
                    )
                )
            }
        
        return cipher
            .doFinal(
                encBytes,
                12, // Everything after first 12 bytes
                encBytes.size - 12
            )
            .let {
                String(it, Charsets.UTF_8)
            }
    }
}