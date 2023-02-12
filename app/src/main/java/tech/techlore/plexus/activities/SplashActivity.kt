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
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import tech.techlore.plexus.R
import tech.techlore.plexus.models.MainData
import tech.techlore.plexus.utils.DbUtils.Companion.favListFromDB
import tech.techlore.plexus.utils.DbUtils.Companion.getDatabase
import tech.techlore.plexus.utils.DbUtils.Companion.installedAppsIntoDB
import tech.techlore.plexus.utils.DbUtils.Companion.installedAppsListFromDB
import tech.techlore.plexus.utils.DbUtils.Companion.plexusDataIntoDB
import tech.techlore.plexus.utils.DbUtils.Companion.plexusDataListFromDB
import tech.techlore.plexus.utils.IntentUtils.Companion.sendListsIntent
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasInternet
import tech.techlore.plexus.utils.NetworkUtils.Companion.hasNetwork
import kotlin.coroutines.CoroutineContext

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity(), CoroutineScope {
    
    private val job = Job()
    override val coroutineContext: CoroutineContext get() = Dispatchers.Main + job
    private lateinit var plexusDataList: ArrayList<MainData>
    private lateinit var installedAppsList: ArrayList<MainData>
    private lateinit var favList: ArrayList<MainData>
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        /*########################################################################################*/
        
        retrieveData()
    }
    
    private fun noNetworkDialog() {
        MaterialAlertDialogBuilder(this, R.style.DialogTheme)
            
            .setTitle(R.string.dialog_title)
            
            .setMessage(R.string.dialog_subtitle)
            
            .setPositiveButton(R.string.retry) { _: DialogInterface?, _: Int ->
                retrieveData() }
                
            .setNegativeButton(R.string.exit) { _: DialogInterface?, _: Int ->
                finishAndRemoveTask() }
                
            .setCancelable(false)
            
            .show()
    }
    
    private fun retrieveData() {
    
        launch {
            val context = this@SplashActivity
            if (hasNetwork(context) && hasInternet()) {
                val db = getDatabase(context)
                plexusDataIntoDB(db.mainDataDao())
                installedAppsIntoDB(context, db.mainDataDao())
                plexusDataList = plexusDataListFromDB(db.mainDataDao())
                installedAppsList = installedAppsListFromDB(db.mainDataDao())
                favList = favListFromDB(db.mainDataDao())
                sendListsIntent(context, MainActivity::class.java,
                                plexusDataList, installedAppsList, favList)
                // Lists are sent through intent, because if they are not,
                // we have to get lists from db in recycler view fragment or main activity
                // which causes slight delay for lists to show up.
                finish()
            }
            else {
                noNetworkDialog()
            }
        }
    }
    
    override fun finish() {
        super.finish()
        job.cancel()
        overridePendingTransition(R.anim.slide_from_end, R.anim.slide_to_start)
    }
}