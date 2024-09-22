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

package tech.techlore.plexus.fragments.settings

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import tech.techlore.plexus.BuildConfig
import tech.techlore.plexus.R
import tech.techlore.plexus.activities.SettingsActivity
import tech.techlore.plexus.appmanager.ApplicationManager
import tech.techlore.plexus.databinding.FragmentSettingsBinding
import tech.techlore.plexus.fragments.bottomsheets.settings.DefaultViewBottomSheet
import tech.techlore.plexus.fragments.bottomsheets.settings.LicensesBottomSheet
import tech.techlore.plexus.fragments.bottomsheets.settings.SupportUsBottomSheet
import tech.techlore.plexus.fragments.bottomsheets.settings.ThemeBottomSheet
import tech.techlore.plexus.preferences.PreferenceManager.Companion.CONF_BEFORE_SUBMIT
import tech.techlore.plexus.preferences.PreferenceManager.Companion.MATERIAL_YOU
import tech.techlore.plexus.utils.IntentUtils.Companion.openURL
import tech.techlore.plexus.utils.UiUtils.Companion.convertDpToPx

class SettingsFragment : Fragment() {
    
    private var _binding: FragmentSettingsBinding? = null
    private val fragmentBinding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val settingsActivity = requireActivity() as SettingsActivity
        settingsActivity.activityBinding.settingsBottomAppBarTitle.text = getString(R.string.settings)
        val preferenceManager = (requireContext().applicationContext as ApplicationManager).preferenceManager
        
        // Adjust scrollview for edge to edge
        ViewCompat.setOnApplyWindowInsetsListener(fragmentBinding.settingsScrollView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                                                        or WindowInsetsCompat.Type.displayCutout())
            v.updatePadding(left = insets.left,
                            top = insets.top,
                            right = insets.right)
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = insets.bottom + convertDpToPx(requireContext(), 80f)
            }
            WindowInsetsCompat.CONSUMED
        }
        
        // Version
        @SuppressLint("SetTextI18n")
        fragmentBinding.aboutVersion.text = "${getString(R.string.version)}: ${BuildConfig.VERSION_NAME}"
        
        // Theme
        fragmentBinding.theme.setOnClickListener {
            ThemeBottomSheet().show(parentFragmentManager, "ThemeBottomSheet")
        }
        
        // Material You
        fragmentBinding.materialYouSwitch.apply {
            if (Build.VERSION.SDK_INT >= 31) {
                isVisible = true
                isChecked = preferenceManager.getBoolean(MATERIAL_YOU, defValue = false)
                setOnCheckedChangeListener { _, isChecked ->
                    preferenceManager.setBoolean(MATERIAL_YOU, isChecked)
                }
            }
        }
        
        // Default view
        fragmentBinding.defaultView.setOnClickListener {
            DefaultViewBottomSheet().show(parentFragmentManager, "DefaultViewBottomSheet")
        }
        
        // Confirm before submitting
        fragmentBinding.confBeforeSubmitSwitch.apply {
            isChecked = preferenceManager.getBoolean(CONF_BEFORE_SUBMIT, defValue = false)
            setOnCheckedChangeListener { _, isChecked ->
                preferenceManager.setBoolean(CONF_BEFORE_SUBMIT, isChecked)
            }
        }
        
        // Privacy policy
        fragmentBinding.privacyPolicy.setOnClickListener {
            openURL(settingsActivity,
                    getString(R.string.plexus_privacy_policy_url),
                    settingsActivity.activityBinding.settingsCoordLayout,
                    settingsActivity.activityBinding.settingsBottomAppBar)
        }
        
        // Report an issue
        fragmentBinding.reportIssue.setOnClickListener {
            openURL(settingsActivity,
                    getString(R.string.plexus_report_issue_url),
                    settingsActivity.activityBinding.settingsCoordLayout,
                    settingsActivity.activityBinding.settingsBottomAppBar)
        }
        
        // Support us
        fragmentBinding.supportUs.setOnClickListener {
            SupportUsBottomSheet().show(parentFragmentManager, "SupportUsBottomSheet")
        }
        
        // View on GitHub
        fragmentBinding.viewOnGitHub.setOnClickListener {
            openURL(settingsActivity,
                    getString(R.string.plexus_github_url),
                    settingsActivity.activityBinding.settingsCoordLayout,
                    settingsActivity.activityBinding.settingsBottomAppBar)
        }
        
        // Visit Techlore
        fragmentBinding.visitTechlore.setOnClickListener {
            openURL(settingsActivity,
                    getString(R.string.techlore_website_url),
                    settingsActivity.activityBinding.settingsCoordLayout,
                    settingsActivity.activityBinding.settingsBottomAppBar)
        }
        
        // Third party licenses
        fragmentBinding.licenses.setOnClickListener {
            LicensesBottomSheet().show(parentFragmentManager, "LicensesBottomSheet")
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}