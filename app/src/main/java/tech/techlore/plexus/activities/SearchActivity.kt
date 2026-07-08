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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.transition.platform.MaterialSharedAxis
import tech.techlore.plexus.R
import tech.techlore.plexus.bottomsheets.search.SearchSortBottomSheet
import tech.techlore.plexus.databinding.ActivitySearchBinding
import tech.techlore.plexus.interfaces.main.SortPrefsChangedListener
import tech.techlore.plexus.utils.UiUtils.Companion.convertDpToPx
import tech.techlore.plexus.utils.UiUtils.Companion.refreshFragment
import tech.techlore.plexus.utils.UiUtils.Companion.setNavBarContrastEnforced

class SearchActivity : AppCompatActivity(), SortPrefsChangedListener {
    
    lateinit var activityBinding: ActivitySearchBinding
    private lateinit var navController: NavController
    var orderChipId = R.id.sortAZ
    private val sixteenDpToPx by lazy {
        convertDpToPx(this, 16f)
    }
    private var isKeyboardVisible = true
    
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
        
        // Views will be properly adjusted for edge to edge even without this code,
        // but this is done to get keyboard visibility status.
        // Root view could also theoretically be used instead of docked toolbar,
        // but when I tried that, there were UI imperfections.
        ViewCompat.setOnApplyWindowInsetsListener(activityBinding.searchDockedToolbar) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                                                        or WindowInsetsCompat.Type.displayCutout()
                                                        or WindowInsetsCompat.Type.ime())
            v.updatePadding(
                left = insets.left + sixteenDpToPx,
                right = insets.right + sixteenDpToPx,
                bottom = insets.bottom
            )
            
            isKeyboardVisible = windowInsets.isVisible(WindowInsetsCompat.Type.ime())
            
            WindowInsetsCompat.CONSUMED
        }
        
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.searchNavHost) as NavHostFragment
        navController = navHostFragment.navController
        
        // Back
        activityBinding.searchBackBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        
        // Sort
        activityBinding.searchSortBtn.setOnClickListener {
            SearchSortBottomSheet(this@SearchActivity).show(supportFragmentManager, "SearchSortBottomSheet")
        }
        
    }
    
    override fun onSortPrefsChanged() {
        activityBinding.searchAppBar.setExpanded(true, true)
        navController.refreshFragment()
    }
    
    override fun onPause() {
        super.onPause()
        
        // This will prevent the following scenario:
        // keyboard hidden by user > user taps on an item > comes back > keyboard visible
        if (!isKeyboardVisible && activityBinding.searchView.hasFocus())
            activityBinding.searchView.clearFocus()
    }
    
    // On back pressed
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (isKeyboardVisible) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
                currentFocus?.let {
                    imm?.hideSoftInputFromWindow(it.windowToken, 0)
                }
            }
            
            finishAfterTransition()
        }
    }
}