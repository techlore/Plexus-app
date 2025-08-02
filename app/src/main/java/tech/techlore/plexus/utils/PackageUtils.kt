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

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tech.techlore.plexus.models.main.MainData
import java.io.ByteArrayInputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

class PackageUtils {
    
    companion object {
        
        suspend fun scannedInstalledAppsList(context: Context): List<MainData> {
            return withContext(Dispatchers.IO) {
                val packageManager = context.packageManager
                val installedAppsList = arrayListOf<MainData>()
                
                packageManager.getInstalledApplications(0)
                    // Only scan for user installed apps,
                    // OR
                    // system apps updated by user
                    .filter {
                        it.flags and ApplicationInfo.FLAG_SYSTEM != 1
                        || it.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0
                    }
                    .map {
                        // Add scanned apps to list
                        if (it.packageName !in setOf("tech.techlore.plexus",
                                                     "com.google.android.settings.intelligence",
                                                     "com.android.vending",
                                                     "com.google.android.gms",
                                                     "com.google.android.gsf",
                                                     "org.microg.gms.droidguard",
                                                     "org.microg.unifiednlp")) {
                            
                            val packageInfo = getPackageInfoWithSigCerts(packageManager, it.packageName)
                            
                            installedAppsList
                                .add(MainData(name = it.loadLabel(packageManager).toString(),
                                              packageName = it.packageName,
                                              installedVersion = packageInfo.versionName.orEmpty(),
                                              installedBuild =
                                              if (Build.VERSION.SDK_INT >= 28) packageInfo.longVersionCode
                                              else packageInfo.versionCode.toLong(),
                                              installedFrom =
                                              when {
                                                  isAppFromFdroid(packageManager, it.packageName)
                                                  || isAppSignedByFdroid(packageInfo) -> "fdroid"
                                                  isAppFromApk(packageManager, it.packageName) -> "apk"
                                                  else -> "google_play_alternative"
                                              },
                                              isInstalled = true))
                        }
                    }
                
                installedAppsList
            }
        }
        
        private fun getPackageInfoWithSigCerts(packageManager: PackageManager,
                                               packageName: String): PackageInfo {
            return packageManager.getPackageInfo(
                packageName,
                if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES
                else PackageManager.GET_SIGNATURES
            )
        }
        
        private fun getAppCertificate(packageInfo: PackageInfo): String? {
            return try {
                val signature =
                    if (Build.VERSION.SDK_INT >= 28) {
                        val signingInfo = packageInfo.signingInfo
                        when {
                            signingInfo?.hasMultipleSigners() == true -> signingInfo.apkContentsSigners.first()
                            else -> signingInfo?.signingCertificateHistory?.first()
                        }
                    }
                    else packageInfo.signatures?.first()
                
                val inputStream = ByteArrayInputStream(signature?.toByteArray())
                (CertificateFactory.getInstance("X.509").generateCertificate(inputStream) as X509Certificate)
                    .subjectX500Principal.name
            }
            catch (_: Exception) {
                null
            }
        }
        
        private fun isAppFromFdroid(packageManager: PackageManager, packageName: String): Boolean {
            val fdroidPackages = setOf("org.fdroid.fdroid",
                                       "org.fdroid.basic",
                                       "org.fdroid.fdroid.privileged")
            
            return (
                    if (Build.VERSION.SDK_INT >= 30)
                        packageManager.getInstallSourceInfo(packageName).installingPackageName
                    else
                        packageManager.getInstallerPackageName(packageName)
                   ) in fdroidPackages
        }
        
        private fun isAppSignedByFdroid(packageInfo: PackageInfo): Boolean {
            return getAppCertificate(packageInfo) == "CN=FDroid,OU=FDroid,O=fdroid.org,L=ORG,ST=ORG,C=UK"
        }
        
        private fun isAppFromApk(packageManager: PackageManager, packageName: String): Boolean {
            val packageInstallers =
                setOf("com.android.packageinstaller",
                      "com.google.android.packageinstaller",
                      "com.samsung.android.packageinstaller",
                      "com.miui.packageinstaller",
                      "com.miui.global.packageinstaller",
                      "com.asus.packageinstaller")
            
            return (
                    if (Build.VERSION.SDK_INT >= 30)
                        packageManager.getInstallSourceInfo(packageName).installingPackageName
                    else
                        packageManager.getInstallerPackageName(packageName)
                   ) in packageInstallers
        }
        
    }
    
}