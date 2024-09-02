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
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import tech.techlore.plexus.appmanager.ApplicationManager

class SystemUtils {
    
    companion object {
        
        fun isDeviceDeGoogledOrMicroG(context: Context) {
            
            val appManager = (context.applicationContext as ApplicationManager)
            
            val gappsPackages = listOf("com.google.android.gms",
                                       "com.google.android.gsf",
                                       "com.android.vending")
            
            val packageManager = context.packageManager
            
            val installedGappsPackagesList =
                gappsPackages.filter { packageName ->
                    getAppInfo(packageManager, packageName) != null
                }
            
            val microGCount =
                if (installedGappsPackagesList.isNotEmpty()) {
                    installedGappsPackagesList.count { packageName ->
                        val appLabel =
                            getAppInfo(packageManager, packageName)?.let {
                                packageManager.getApplicationLabel(it).toString()
                            }
                        appLabel?.let { !it.startsWith("Google", ignoreCase = true) } ?: false
                    }
                }
                else -1
            
            appManager.isDeviceMicroG = installedGappsPackagesList.size == microGCount
            appManager.isDeviceDeGoogled = installedGappsPackagesList.isEmpty()
        }
        
        private fun getAppInfo(packageManager: PackageManager, packageName: String): ApplicationInfo? {
            return try {
                packageManager.getApplicationInfo(packageName, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
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