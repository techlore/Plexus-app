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

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import tech.techlore.plexus.R;
import tech.techlore.plexus.activities.SettingsActivity;
import tech.techlore.plexus.databinding.FragmentAboutBinding;

public class AboutFragment extends Fragment {

    private FragmentAboutBinding fragmentBinding;
    String version;

    public AboutFragment() {
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
        fragmentBinding = FragmentAboutBinding.inflate(inflater, container, false);
        return fragmentBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    
        SettingsActivity settingsActivity = (SettingsActivity) requireActivity();

        // Version
        try {
            version = getResources().getString(R.string.version)
                    + ": "
                    + requireContext().getPackageManager()
                    .getPackageInfo(requireContext()
                            .getPackageName(), 0)
                    .versionName;
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        fragmentBinding.versionAbout.setText(version);

        // Privacy policy
        fragmentBinding.privacyPolicy
                .setOnClickListener(v ->
                    OpenURL(requireActivity(), "https://github.com/techlore/Plexus-app/blob/main/PRIVACY.md",
                            settingsActivity.activityBinding.settingsCoordLayout, settingsActivity.activityBinding.toolbarBottom));

        // Licenses
        fragmentBinding.licenses
                .setOnClickListener(v ->
                    getParentFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.slide_from_end, R.anim.slide_to_start,
                                                 R.anim.slide_from_start, R.anim.slide_to_end)
                            .replace(R.id.activity_host_fragment, new LicensesFragment())
                            .addToBackStack(null)
                            .commit());

        // View on GitHub
        fragmentBinding.viewOnGit
                .setOnClickListener(v ->
                        OpenURL(requireActivity(), "https://github.com/techlore/Plexus-app",
                                settingsActivity.activityBinding.settingsCoordLayout, settingsActivity.activityBinding.toolbarBottom));

        // Visit Techlore
        fragmentBinding.visitTechlore
                .setOnClickListener(v ->
                        OpenURL(requireActivity(), "https://techlore.tech",
                                settingsActivity.activityBinding.settingsCoordLayout, settingsActivity.activityBinding.toolbarBottom));

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentBinding = null;
    }

}
