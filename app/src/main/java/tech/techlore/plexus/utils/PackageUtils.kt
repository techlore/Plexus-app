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

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
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
                val installedAppsList = ArrayList<MainData>()
            
                packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                    // Only scan for user installed apps
                    // OR system apps updated by user
                    .filter {
                        it.flags and ApplicationInfo.FLAG_SYSTEM != 1
                        || it.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0
                    }
                    .mapNotNull {
                        // Add scanned apps to list
                        if (it.packageName !in setOf("tech.techlore.plexus",
                                                     "com.android.vending",
                                                     "com.google.android.gms",
                                                     "com.google.android.gsf",
                                                     "org.microg.gms.droidguard",
                                                     "org.microg.unifiednlp")){
                        
                            val installedFrom =
                                when(packageManager.getInstallerPackageName(it.packageName)) {
                                    "com.android.vending", "com.aurora.store" -> "google_play"
                                    else ->
                                        if (isAppFromFdroid(packageManager, it.packageName)) "fdroid"
                                        else "other"
                                }
                        
                            installedAppsList
                                .add(MainData(name = it.loadLabel(packageManager).toString(),
                                              packageName = it.packageName,
                                              installedVersion =
                                              packageManager.getPackageInfo(it.packageName, 0).versionName,
                                              installedBuild = packageManager.getPackageInfo(it.packageName, 0).versionCode,
                                              installedFrom = installedFrom,
                                              isInstalled = true))
                        }
                    }
                
                installedAppsList
            }
        }
    
        fun checkAppCertificate(packageManager: PackageManager,
                                packageName: String,
                                certificate: String): Boolean {
            return try {
                val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                val certificateFactory = CertificateFactory.getInstance("X.509")
                packageInfo.signatures.any { signature ->
                    val inputStream = ByteArrayInputStream(signature.toByteArray())
                    val x509Certificate = certificateFactory.generateCertificate(inputStream) as X509Certificate
                    x509Certificate.subjectX500Principal.name == certificate
                }
            }
            catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    
        private fun isAppFromFdroid(packageManager: PackageManager, packageName: String): Boolean {
            val fdroidCertificate = "CN=FDroid,OU=FDroid,O=fdroid.org,L=ORG,ST=ORG,C=UK"
            return checkAppCertificate(packageManager, packageName, fdroidCertificate)
        }
    
        fun isAppInstalled(packageManager: PackageManager, packageName: String): Boolean {
            return try {
                packageManager.getApplicationInfo(packageName, 0)
                true
            }
            catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
        
    }
    
}