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

package tech.techlore.plexus.fragments.main;

import static tech.techlore.plexus.utils.IntentUtils.OpenURL;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import tech.techlore.plexus.R;
import tech.techlore.plexus.activities.LicensesActivity;
import tech.techlore.plexus.activities.MainActivity;
import tech.techlore.plexus.databinding.FragmentSettingsAboutBinding;

public class AboutFragment extends Fragment {

    private FragmentSettingsAboutBinding fragmentBinding;
    private MainActivity mainActivity;
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
        setHasOptionsMenu(true);
        fragmentBinding = FragmentSettingsAboutBinding.inflate(inflater, container, false);
        return fragmentBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mainActivity = (MainActivity) requireActivity();

        mainActivity.activityBinding.toolbarTop.setTitle(R.string.about_title);

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
                            mainActivity.activityBinding.mainCoordinatorLayout, mainActivity.activityBinding.bottomNavContainer));

        // Licenses
        fragmentBinding.licenses
                .setOnClickListener(v ->
                        mainActivity.startActivity(new Intent(mainActivity, LicensesActivity.class)));

        // View on GitHub
        fragmentBinding.viewOnGit
                .setOnClickListener(v ->
                        OpenURL(requireActivity(), "https://github.com/techlore/Plexus-app",
                                mainActivity.activityBinding.mainCoordinatorLayout, mainActivity.activityBinding.bottomNavContainer));

        // Visit Techlore
        fragmentBinding.visitTechlore
                .setOnClickListener(v ->
                        OpenURL(requireActivity(), "https://techlore.tech",
                                mainActivity.activityBinding.mainCoordinatorLayout, mainActivity.activityBinding.bottomNavContainer));

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentBinding = null;
    }

}
