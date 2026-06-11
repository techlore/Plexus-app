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
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NetworkUtils {
    
    companion object {
        
        // Check if network has internet connection
        suspend fun hasInternet(context: Context): Boolean {
            return withContext(Dispatchers.IO) {
                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                    ?.let {
                        it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        && it.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                        && (Build.VERSION.SDK_INT < 28 || it.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED))
                    }
                ?: false
            }
        }
    }
}