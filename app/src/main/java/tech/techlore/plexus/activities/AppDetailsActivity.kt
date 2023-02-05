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

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import tech.techlore.plexus.R
import tech.techlore.plexus.databinding.ActivityAppDetailsBinding
import tech.techlore.plexus.utils.IntentUtils.Companion.share
import tech.techlore.plexus.utils.IntentUtils.Companion.openURL

class AppDetailsActivity : AppCompatActivity() {

    private lateinit var activityBinding: ActivityAppDetailsBinding
    private lateinit var nameString: String
    private lateinit var packageNameString: String
    /*private lateinit val plexusVersionString: String
    private lateinit val dgStatusString: String
    private lateinit val mgStatusString: String
    private lateinit val dgNotesString: String
    private lateinit val mgNotesString: String*/
    private lateinit var playStoreString: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityBinding = ActivityAppDetailsBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)

        nameString = intent.getStringExtra("name")!!
        packageNameString = intent.getStringExtra("packageName")!!
        //plexusVersionString = intent.getStringExtra("plexusVersion");
        val installedVersionString = intent.getStringExtra("installedVersion")
        /*dgStatusString = intent.getStringExtra("dgStatus");
        mgStatusString = intent.getStringExtra("mgStatus");
        dgNotesString = intent.getStringExtra("dgNotes");
        mgNotesString = intent.getStringExtra("mgNotes");*/
        playStoreString = "https://play.google.com/store/apps/details?id=$packageNameString"
        val requestManager = Glide.with(applicationContext)
        val requestBuilder: RequestBuilder<Drawable>

        /*########################################################################################*/

        setSupportActionBar(activityBinding.bottomAppBar)

        activityBinding.bottomAppBar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        if (installedVersionString != null) {
            activityBinding.detailsVersion.visibility = View.GONE
            activityBinding.plexusText.visibility = View.VISIBLE
            activityBinding.detailsPlexusVersion.visibility = View.VISIBLE
            //activityBinding.detailsPlexusVersion.setText(plexusVersionString)
            activityBinding.installedText.visibility = View.VISIBLE
            activityBinding.detailsInstalledVersion.visibility = View.VISIBLE
            activityBinding.detailsInstalledVersion.text = installedVersionString
            requestBuilder =
                try {
                    requestManager.load(packageManager.getApplicationIcon(packageNameString))
                }
                catch (e: PackageManager.NameNotFoundException) {
                    throw RuntimeException(e)
                }
        }
        else {
            requestBuilder = requestManager
                .load("")
                .placeholder(R.drawable.ic_apk)
                .onlyRetrieveFromCache(true) // Image will always be in cache
                                                 // since it's loaded in Plexus Data fragment

            //activityBinding.detailsVersion.setText(plexusVersionString);
        }

        requestBuilder.into(activityBinding.detailsAppIcon)
        activityBinding.detailsName.text = nameString
        activityBinding.detailsPackageName.text = packageNameString
        /*activityBinding.dgNotes.setText(dgNotesString);
        activityBinding.mgNotes.setText(mgNotesString);*/

        // FAB
        activityBinding.fab.setOnClickListener {
            startActivity(
                Intent(this@AppDetailsActivity, SubmitActivity::class.java)
                    .putExtra("name", nameString)
                    .putExtra("packageName", packageNameString)
            )
            overridePendingTransition(R.anim.fade_in_slide_from_bottom, R.anim.no_movement)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu
        // this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_activity_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {

            // Play store URL
            R.id.menu_play_store -> openURL(this, playStoreString,
                                            activityBinding.appDetailsCoordinatorLayout,
                                            activityBinding.bottomAppBar)

            R.id.menu_share -> share(this, nameString, packageNameString,
                                    /*plexusVersionString,
                                        dgStatusString, mgStatusString,
                                        dgNotesString, mgNotesString,*/
                                    playStoreString)

            R.id.menu_help -> startActivity(Intent(this@AppDetailsActivity, SettingsActivity::class.java)
                                            .putExtra("frag", R.id.menu_help))

        }

        return true
    }

    // Set transition when finishing activity
    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.fade_scale_out)
    }
}