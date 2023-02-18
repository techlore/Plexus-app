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

package tech.techlore.plexus.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import tech.techlore.plexus.R
import tech.techlore.plexus.appmanager.ApplicationManager
import tech.techlore.plexus.fragments.dialogs.NoNetworkDialog
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasInternet
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasNetwork
import kotlin.coroutines.CoroutineContext

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity(), CoroutineScope {
    
    private val job = Job()
    override val coroutineContext: CoroutineContext get() = Dispatchers.Main + job
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        /*########################################################################################*/
        
        retrieveData()
    }
    
    private fun retrieveData() {
    
        launch {
            val context = this@SplashActivity
            if (hasNetwork(context) && hasInternet()) {
                val mainRepository = (applicationContext as ApplicationManager).mainRepository
                mainRepository.plexusDataIntoDB(context)
                mainRepository.installedAppsIntoDB(context)
                startActivity(Intent(context, MainActivity::class.java))
                finish()
            }
            else {
                NoNetworkDialog(negativeButtonText = getString(R.string.exit),
                                positiveButtonClickListener = {
                                    retrieveData()
                                },
                                negativeButtonClickListener = {
                                    finishAndRemoveTask()
                                })
                    .show(supportFragmentManager, "NoNetworkDialog")
            }
        }
    }
    
    override fun finish() {
        super.finish()
        job.cancel()
        overridePendingTransition(R.anim.slide_from_end, R.anim.slide_to_start)
    }
}