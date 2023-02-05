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

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import tech.techlore.plexus.R
import tech.techlore.plexus.databinding.ActivitySettingsBinding
import tech.techlore.plexus.fragments.settings.AboutFragment
import tech.techlore.plexus.fragments.settings.HelpFragment

class SettingsActivity : AppCompatActivity() {

    lateinit var activityBinding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityBinding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)

        /*####################################################################################*/
        setSupportActionBar(activityBinding.toolbarBottom)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        activityBinding.toolbarBottom.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        
        intent.extras?.let {
            displayFragment(it.getInt("frag"))
        }
    }

    // Setup fragments
    private fun displayFragment(intentValue: Int) {

        val fragment: Fragment
        val toolbarTitle: String

        if (intentValue == R.id.menu_help) {
            fragment = HelpFragment()
            toolbarTitle = getString(R.string.menu_help)
        }
        else {
            fragment = AboutFragment()
            toolbarTitle = getString(R.string.about)
        }

        supportFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .replace(R.id.activity_host_fragment, fragment)
            .commitNow()
        activityBinding.toolbarBottom.title = toolbarTitle
    }
}