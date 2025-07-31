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

package tech.techlore.plexus.api

import android.app.Application
import android.util.Base64
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.CertificatePinner
import okhttp3.ConnectionSpec
import okhttp3.Dispatcher
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import tech.techlore.plexus.R
import java.io.ByteArrayInputStream
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import kotlin.collections.map

class ApiManager {
    
    companion object : KoinComponent {
        
        private const val CERT_BEGIN = "-----BEGIN CERTIFICATE-----"
        private const val CERT_END = "-----END CERTIFICATE-----"
        
        // Google needs special handling
        // Check https://pki.goog/faq/#faq-27
        private val googleRootCerts = getGoogleRootCerts().toTypedArray()
        
        private val okHttpClient =
            HttpClient(OkHttp) {
                engine {
                    config {
                        dispatcher(
                            Dispatcher().apply {
                                maxRequests = 8 // Max parallel network requests (default is 64)
                            }
                        )
                        connectTimeout(30, TimeUnit.SECONDS) // Default is 10 seconds
                        readTimeout(30, TimeUnit.SECONDS) // Default is 10 seconds
                        writeTimeout(30, TimeUnit.SECONDS) // Default is 10 seconds
                        followRedirects(true)
                        connectionSpecs(listOf(ConnectionSpec.RESTRICTED_TLS, ConnectionSpec.MODERN_TLS))
                        certificatePinner(
                            CertificatePinner.Builder()
                                .add("plexus.techlore.tech", "sha256/C5+lpZ7tcVwmwQIMcRtPbsQtWLABXhQzejna0wHFr8M=") // ISRG Root X1
                                .add("f-droid.org", "sha256/C5+lpZ7tcVwmwQIMcRtPbsQtWLABXhQzejna0wHFr8M=") // ISRG Root X1
                                .add("play.google.com", *googleRootCerts)
                                .add("play-lh.googleusercontent.com", *googleRootCerts)
                                .add("translate.fedilab.app", "sha256/C5+lpZ7tcVwmwQIMcRtPbsQtWLABXhQzejna0wHFr8M=") // ISRG Root X1
                                .build()
                        )
                    }
                }
                install(ContentNegotiation) {
                    json(get<Json>())
                }
            }
        
        private fun getGoogleRootCerts(): List<String> {
            return try {
                get<Application>().resources.openRawResource(R.raw.google_roots_ca)
                    .use {
                        parseX509Certificates(it.bufferedReader().readText())
                    }
                    .map {
                        "sha256/${it.publicKey.encoded.sha256Base64()}"
                    }
            }
            catch (e: Exception) {
                Log.e("Certificate Pinning", "Failed to get SHA256 certificate hash for Google", e)
                emptyList()
            }
        }
        
        private fun parseX509Certificates(pemContent: String): List<X509Certificate> {
            val certificateFactory = CertificateFactory.getInstance("X.509")
            return pemContent.split(CERT_END)
                .map { it.substringAfter(CERT_BEGIN).substringBefore(CERT_END).trim() }
                .filter { it.isNotBlank() }
                .map {
                    certificateFactory.generateCertificate(
                        ByteArrayInputStream(Base64.decode(it, Base64.DEFAULT))
                    ) as X509Certificate
                }
        }
        
        private fun ByteArray.sha256Base64(): String {
            return Base64.encodeToString(
                MessageDigest.getInstance("SHA-256").digest(this@sha256Base64),
                Base64.NO_WRAP
            )
        }
        
        fun apiBuilder(): ApiService {
            return ApiService(okHttpClient)
        }
    }
}
