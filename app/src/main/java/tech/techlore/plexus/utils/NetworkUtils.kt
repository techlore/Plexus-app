/*
 * Copyright (c) 2022 Techlore
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
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*

class NetworkUtils {
    
    companion object {
    
        private val okHttpClient = OkHttpClient()
    
        // Check network availability
        fun hasNetwork(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
    
        // Check if network has internet connection
        // Must be called in background thread
        fun hasInternet(): Boolean {
            return try {
                val socket = Socket()
                socket.connect(InetSocketAddress("plexus.fly.dev", 443), 5000)
                socket.close()
                true
            }
            catch (e: IOException) {
                false
            }
        }
    
        // GET request
        @Throws(IOException::class)
        fun getReq(): String {
            
            val request = Request.Builder()
                .url("https://plexus.fly.dev/api/v1/applications")
                .build()
            
            okHttpClient.newCall(request)
                .execute()
                .use { response ->
                    return response.body.string()
                }
        }
    }
}