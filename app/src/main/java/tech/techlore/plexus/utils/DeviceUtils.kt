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
import tech.techlore.plexus.objects.DeviceState

class DeviceUtils {
    
    companion object {
        
        fun isDeviceDeGoogledOrMicroG(context: Context) {
            
            val gappsPackages = arrayOf("com.google.android.gms",
                                       "com.google.android.gsf",
                                       "com.android.vending")
            
            val packageManager = context.packageManager
            
            val installedGappsPackagesList =
                gappsPackages.filter { packageName ->
                    getAppInfo(packageManager, packageName) != null
                }
            
            val microGCount =
                when {
                    installedGappsPackagesList.isNotEmpty() -> {
                        installedGappsPackagesList.count { packageName ->
                            val appLabel =
                                getAppInfo(packageManager, packageName)?.let {
                                    packageManager.getApplicationLabel(it).toString()
                                }
                            appLabel?.let { !it.startsWith("Google", ignoreCase = true) } == true
                        }
                    }
                    else -> - 1
                }
            
            DeviceState.apply {
                isDeviceMicroG = installedGappsPackagesList.size == microGCount
                isDeviceDeGoogled = installedGappsPackagesList.isEmpty()
            }
        }
        
        private fun getAppInfo(packageManager: PackageManager, packageName: String): ApplicationInfo? {
            return try {
                packageManager.getApplicationInfo(packageName, 0)
            }
            catch (_: PackageManager.NameNotFoundException) {
                null
            }
        }
        
        @SuppressLint("PrivateApi")
        fun getSystemProperty(propertyName: String): String? {
            return try {
                val systemProperties = Class.forName("android.os.SystemProperties")
                val getProperty = systemProperties.getMethod("get", String::class.java)
                getProperty.invoke(null, propertyName) as? String
            }
            catch (_: Exception) {
                null
            }
        }
        
    }
    
}