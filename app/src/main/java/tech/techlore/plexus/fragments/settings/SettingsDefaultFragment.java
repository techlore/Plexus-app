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

package tech.techlore.plexus.fragments.settings;

import static tech.techlore.plexus.preferences.PreferenceManager.THEME_PREF;
import static tech.techlore.plexus.utils.IntentUtils.OpenURL;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Objects;

import tech.techlore.plexus.R;
import tech.techlore.plexus.activities.SettingsActivity;
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding;
import tech.techlore.plexus.databinding.BottomSheetThemeBinding;
import tech.techlore.plexus.databinding.DialogFooterBinding;
import tech.techlore.plexus.databinding.FragmentSettingsDefaultBinding;
import tech.techlore.plexus.preferences.PreferenceManager;

public class SettingsDefaultFragment extends Fragment {

    private FragmentSettingsDefaultBinding fragmentBinding;
    private PreferenceManager preferenceManager;

    public SettingsDefaultFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentBinding = FragmentSettingsDefaultBinding.inflate(inflater, container,  false);
        Objects.requireNonNull(((SettingsActivity) requireActivity()).getSupportActionBar()).setTitle(R.string.menu_settings);
        return fragmentBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        preferenceManager=new PreferenceManager(requireActivity());

    /*############################################################################################*/

        // THEME
        view.findViewById(R.id.settings_theme_holder)
                .setOnClickListener(v1 ->
                        ThemeBottomSheet());

        if (preferenceManager.getInt(THEME_PREF) == 0){
            if (Build.VERSION.SDK_INT >= 29){
                fragmentBinding.settingsThemeSubtitle.setText(R.string.system_default);
            }
            else{
                fragmentBinding.settingsThemeSubtitle.setText(R.string.light);
            }
        }
        else if (preferenceManager.getInt(THEME_PREF) == R.id.sys_default){
            fragmentBinding.settingsThemeSubtitle.setText(R.string.system_default);
        }
        else if (preferenceManager.getInt(THEME_PREF) == R.id.light){
            fragmentBinding.settingsThemeSubtitle.setText(R.string.light);
        }
        else if (preferenceManager.getInt(THEME_PREF) == R.id.dark){
            fragmentBinding.settingsThemeSubtitle.setText(R.string.dark);
        }

        // REPORT AN ISSUE
        fragmentBinding.settingsReportIssueHolder
                .setOnClickListener(v2 ->
                    OpenURL(requireActivity(), "https://github.com/techlore/Plexus-app/issues"));

        // PULL REQUEST
        fragmentBinding.settingsPullReqHolder
                .setOnClickListener(v3 ->
                        OpenURL(requireActivity(), "https://github.com/techlore/Plexus-app/pulls"));


        // ABOUT
        fragmentBinding.settingsAboutHolder
                .setOnClickListener(v4 ->
                        getParentFragmentManager().beginTransaction()
                                .setCustomAnimations(R.anim.slide_from_end, R.anim.slide_to_start,
                                        R.anim.slide_from_start, R.anim.slide_to_end)
                                .replace(R.id.activity_host_fragment, new AboutFragment())
                                .addToBackStack(null)
                                .commit());
    }

    // THEME BOTTOM SHEET
    private void ThemeBottomSheet(){
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetTheme);
        bottomSheetDialog.setCancelable(true);

        final BottomSheetThemeBinding bottomSheetBinding = BottomSheetThemeBinding.inflate(getLayoutInflater());
        final BottomSheetHeaderBinding headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.getRoot());
        final DialogFooterBinding footerBinding = DialogFooterBinding.bind(bottomSheetBinding.getRoot());
        bottomSheetDialog.setContentView(bottomSheetBinding.getRoot());

        // TITLE
        headerBinding.bottomSheetTitle.setText(R.string.theme_title);

        // DEFAULT CHECKED RADIO
        if (preferenceManager.getInt(THEME_PREF) == 0){
            if (Build.VERSION.SDK_INT >= 29){
                preferenceManager.setInt(THEME_PREF, R.id.sys_default);
            }
            else{
                preferenceManager.setInt(THEME_PREF, R.id.light);
            }
        }
        bottomSheetBinding.themeRadiogroup.check(preferenceManager.getInt(THEME_PREF));

        // SHOW SYSTEM DEFAULT OPTION ONLY ON SDK 29 AND ABOVE
        if (Build.VERSION.SDK_INT >= 29){
            bottomSheetBinding.sysDefault.setVisibility(View.VISIBLE);
        }
        else{
            bottomSheetBinding.sysDefault.setVisibility(View.GONE);
        }

        // ON SELECTING OPTION
        bottomSheetBinding.themeRadiogroup
                .setOnCheckedChangeListener((radioGroup, checkedId) -> {

                    if (checkedId == R.id.sys_default) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    }
                    else if (checkedId == R.id.light) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }

                    else if (checkedId == R.id.dark) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    }

                    preferenceManager.setInt(THEME_PREF, checkedId);
                    bottomSheetDialog.dismiss();
                    requireActivity().recreate();
                });

        // POSITIVE BUTTON
        footerBinding.positiveButton.setVisibility(View.GONE);

        // NEGATIVE BUTTON
        footerBinding.negativeButton.setOnClickListener(view12 ->
                bottomSheetDialog.cancel());

        // SHOW BOTTOM SHEET WITH CUSTOM ANIMATION
        Objects.requireNonNull(bottomSheetDialog.getWindow()).getAttributes().windowAnimations = R.style.BottomSheetAnimation;
        bottomSheetDialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentBinding = null;
    }

}
