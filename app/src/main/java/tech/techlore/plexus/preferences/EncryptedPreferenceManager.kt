/*
 * Copyright (c) 2022-present Techlore
 *
 *  This file is part of Plexus.
 *
 *  Plexus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plexus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Plexus.  If not, see <https://www.gnu.org/licenses/>.
 */

package tech.techlore.plexus.preferences

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.IOException
import java.security.GeneralSecurityException

class EncryptedPreferenceManager(context: Context) {
    
    companion object {
        const val DEVICE_ROM = "device_rom"
        const val DEVICE_ID = "device_id"
        const val DEVICE_TOKEN = "device_token"
        const val IS_REGISTERED = "is_registered"
    }
    
    private val encryptedSharedPreferences =
        try {
            EncryptedSharedPreferences.create(context,
                                              "tech.techlore.plexus_enc_prefs",
                                              MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
                                              EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                                              EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
        }
        catch (genSecException: GeneralSecurityException) {
            genSecException.printStackTrace()
            throw RuntimeException("Encrypted Preferences General Security Exception", genSecException)
        }
        catch (ioException: IOException) {
            ioException.printStackTrace()
            throw RuntimeException("Encrypted Preferences IO Exception", ioException)
        }
    
    fun getBoolean(key: String): Boolean {
        return encryptedSharedPreferences.getBoolean(key, false)
    }
    
    fun setBoolean(key: String, value: Boolean) {
        encryptedSharedPreferences.edit().apply {
            putBoolean(key, value)
            apply()
        }
    }
    
    fun getString(key: String): String? {
        return encryptedSharedPreferences.getString(key, null)
    }
    
    fun setString(key: String, value: String) {
        encryptedSharedPreferences.edit().apply {
            putString(key, value)
            apply()
        }
    }
    
    fun deleteString(key: String) {
        encryptedSharedPreferences.edit().apply{
            remove(key)
            apply()
        }
    }
    
}