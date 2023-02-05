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

import static tech.techlore.plexus.utils.IntentUtils.OpenURL;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tech.techlore.plexus.R;
import tech.techlore.plexus.activities.SettingsActivity;
import tech.techlore.plexus.databinding.FragmentLicensesBinding;

public class LicensesFragment extends Fragment {
    
    private FragmentLicensesBinding fragmentBinding;
    private SettingsActivity settingsActivity;
    
    public LicensesFragment() {
        // Required empty public constructor
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentBinding = FragmentLicensesBinding.inflate(inflater, container, false);
        return fragmentBinding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    
        settingsActivity = (SettingsActivity) requireActivity();
    
        settingsActivity.activityBinding.toolbarBottom.setTitle(getString(R.string.licenses));
        
        // Plexus
        fragmentBinding.plexus.setOnClickListener(v ->
                  OpenURL(requireActivity(), "https://github.com/techlore/Plexus-app/blob/main/LICENSE",
                          settingsActivity.activityBinding.settingsCoordLayout, settingsActivity.activityBinding.toolbarBottom));
    
        // Jackson
        fragmentBinding.jackson.setOnClickListener(v ->
                  OpenURL(requireActivity(), "https://github.com/FasterXML/jackson-core/blob/2.15/LICENSE",
                          settingsActivity.activityBinding.settingsCoordLayout, settingsActivity.activityBinding.toolbarBottom));
        // OkHttp
        fragmentBinding.okhttp.setOnClickListener(v ->
                  OpenURL(requireActivity(), "https://github.com/square/okhttp/blob/master/LICENSE.txt",
                          settingsActivity.activityBinding.settingsCoordLayout, settingsActivity.activityBinding.toolbarBottom));
        // Plexus
        fragmentBinding.fastscroll.setOnClickListener(v ->
                  OpenURL(requireActivity(), "https://github.com/zhanghai/AndroidFastScroll/blob/master/LICENSE",
                          settingsActivity.activityBinding.settingsCoordLayout, settingsActivity.activityBinding.toolbarBottom));
    
        // Material Design Icons
        fragmentBinding.mdIcons.setOnClickListener(v ->
                  OpenURL(requireActivity(), "https://github.com/Templarian/MaterialDesign/blob/master/LICENSE",
                          settingsActivity.activityBinding.settingsCoordLayout, settingsActivity.activityBinding.toolbarBottom));

    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        settingsActivity.activityBinding.toolbarBottom.setTitle(getString(R.string.about));
        fragmentBinding = null;
    }
    
}