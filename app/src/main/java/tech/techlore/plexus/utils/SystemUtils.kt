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

package tech.techlore.plexus.utils

import android.annotation.SuppressLint

class SystemUtils {
    
    companion object {
    
        @SuppressLint("PrivateApi")
        fun getSystemProperty(propertyName: String): String? {
            val propertyValue: String? =
                try {
                    val systemProperties = Class.forName("android.os.SystemProperties")
                    val getProperty = systemProperties.getMethod("get", String::class.java)
                    getProperty.invoke(null, propertyName) as? String
                } catch (e: Exception) {
                    null
                }
            return propertyValue
        }
    
        fun mapAndroidVersionIntToString(versionSdkInt: Int): String {
            return when(versionSdkInt) {
                23 -> "6.0"
                24 -> "7.0"
                25 -> "7.1.1"
                26 -> "8.0"
                27 -> "8.1"
                28 -> "9.0"
                29 -> "10.0"
                30 -> "11.0"
                31 -> "12.0"
                32 -> "12.1"
                33 -> "13.0"
                34 -> "14.0"
                else -> "NA"
            }
        }
        
    }
    
}