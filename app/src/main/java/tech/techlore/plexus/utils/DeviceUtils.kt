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

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tech.techlore.plexus.objects.DeviceState

class DeviceUtils {
    
    companion object {
        
        suspend fun isDeviceDeGoogledOrMicroG(packageManager: PackageManager) {
            val gappsPackages = arrayOf("com.google.android.gms", // Google Play Services
                                        "com.google.android.gsf", // Google Services Framework
                                        "com.android.vending") // Google Play Store
            
            var microGCount = 0
            var installedGappsCount = 0
            
            withContext(Dispatchers.IO) {
                gappsPackages.forEach {
                    getAppInfo(packageManager, it)?.let { appInfo ->
                        installedGappsCount ++
                        if (!packageManager.getApplicationLabel(appInfo).startsWith("Google", ignoreCase = true)) {
                            when {
                                // CalyxOS + disabled microG = deGoogled
                                DeviceState.rom == "CalyxOS" && !appInfo.enabled -> installedGappsCount --
                                else -> microGCount ++
                            }
                        }
                    }
                }
                
                DeviceState.apply {
                    isDeviceMicroG = installedGappsCount > 0 && installedGappsCount == microGCount
                    isDeviceDeGoogled = installedGappsCount == 0
                }
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
        
    }
    
}