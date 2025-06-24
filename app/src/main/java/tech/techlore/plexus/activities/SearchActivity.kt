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

package tech.techlore.plexus.activities

import android.os.Bundle
import android.view.Window
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.transition.platform.MaterialSharedAxis
import tech.techlore.plexus.R
import tech.techlore.plexus.bottomsheets.search.SearchSortBottomSheet
import tech.techlore.plexus.databinding.ActivitySearchBinding
import tech.techlore.plexus.utils.UiUtils.Companion.setButtonTooltipText
import tech.techlore.plexus.utils.UiUtils.Companion.setNavBarContrastEnforced

class SearchActivity : AppCompatActivity() {
    
    lateinit var activityBinding: ActivitySearchBinding
    var orderChipId = R.id.sortAZ
    
    public override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        window.apply {
            setNavBarContrastEnforced()
            requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
            returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
        }
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        activityBinding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.searchNavHost) as NavHostFragment
        val navController = navHostFragment.navController
        
        // Back
        activityBinding.searchBackBtn.apply {
            setButtonTooltipText(getString(R.string.menu_back))
            setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
        
        // Sort
        activityBinding.searchSortBtn.apply {
            setButtonTooltipText(getString(R.string.menu_back))
            setOnClickListener {
                SearchSortBottomSheet(navController).show(supportFragmentManager, "SearchSortBottomSheet")
            }
        }
        
    }
    
    // On back pressed
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
            currentFocus?.takeIf { imm?.isAcceptingText == true }?.let {
                imm?.hideSoftInputFromWindow(it.windowToken, 0)
                it.clearFocus()
            }
            finishAfterTransition()
        }
    }
}