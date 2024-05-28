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

package tech.techlore.plexus.utils

import android.annotation.SuppressLint
import android.content.Context
import tech.techlore.plexus.appmanager.ApplicationManager
import tech.techlore.plexus.utils.PackageUtils.Companion.getAppCertificate
import tech.techlore.plexus.utils.PackageUtils.Companion.isAppInstalled

class SystemUtils {
    
    companion object {
        
        fun isDeviceDeGoogledOrMicroG(context: Context) {
            
            val appManager = (context.applicationContext as ApplicationManager)
            
            val gappsPackages = listOf("com.google.android.gms",
                                       "com.google.android.gsf",
                                       "com.android.vending")
            
            val microGCertificate = "O=NOGAPPS Project,C=DE"
            val packageManager = context.packageManager
    
            val gappsCount =
                gappsPackages.count { packageName ->
                    isAppInstalled(packageManager, packageName)
                }
            
            val microGCount =
                gappsPackages.count { packageName ->
                    microGCertificate == getAppCertificate(packageManager, packageName)
                }
    
            appManager.deviceIsMicroG = gappsCount == microGCount
            appManager.deviceIsDeGoogled = gappsCount == 0
        }
        
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
        
    }
    
}