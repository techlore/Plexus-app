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

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Objects;

import tech.techlore.plexus.R;
import tech.techlore.plexus.activities.SettingsActivity;
import tech.techlore.plexus.databinding.BottomSheetHeaderBinding;
import tech.techlore.plexus.databinding.BottomSheetLicensesBinding;
import tech.techlore.plexus.databinding.DialogFooterBinding;
import tech.techlore.plexus.databinding.FragmentSettingsAboutBinding;

public class AboutFragment extends Fragment {

    private FragmentSettingsAboutBinding fragmentBinding;
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
        fragmentBinding = FragmentSettingsAboutBinding.inflate(inflater, container, false);
        Objects.requireNonNull(((SettingsActivity) requireActivity()).getSupportActionBar()).setTitle(R.string.about_title);
        return fragmentBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        // VERSION
        try {
            version = getResources().getString(R.string.version)
                    + ": "
                    + requireContext().getPackageManager()
                    .getPackageInfo(requireContext()
                            .getPackageName(), 0)
                    .versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        fragmentBinding.versionAbout.setText(version);

        // PRIVACY POLICY
        fragmentBinding.privacyPolicy
                .setOnClickListener(v ->
                    OpenURL(requireActivity(), "https://github.com/techlore/Plexus-app/blob/main/PRIVACY.md"));

        // LICENSES
        fragmentBinding.licenses
                .setOnClickListener(v ->
                                LicensesBottomSheet());

        // VIEW ON GITHUB
        fragmentBinding.viewOnGit
                .setOnClickListener(v ->
                        OpenURL(requireActivity(), "https://github.com/techlore/Plexus-app"));

        // VISIT TECHLORE
        fragmentBinding.visitTechlore
                .setOnClickListener(v ->
                        OpenURL(requireActivity(), "https://techlore.tech"));

    }

    // THEME BOTTOM SHEET
    private void LicensesBottomSheet(){
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetTheme);
        bottomSheetDialog.setCancelable(true);

        final BottomSheetLicensesBinding bottomSheetBinding = BottomSheetLicensesBinding.inflate(getLayoutInflater());
        final BottomSheetHeaderBinding headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.getRoot());
        final DialogFooterBinding footerBinding = DialogFooterBinding.bind(bottomSheetBinding.getRoot());
        bottomSheetDialog.setContentView(bottomSheetBinding.getRoot());

        // TITLE
        headerBinding.bottomSheetTitle.setText(R.string.licenses);

        // PLEXUS
        bottomSheetBinding.plexusLicense.setOnClickListener(v ->
                OpenURL(requireActivity(), "https://github.com/techlore/Plexus-app/blob/main/LICENSE"));

        // JACKSON
        bottomSheetBinding.jacksonLicense.setOnClickListener(v ->
                OpenURL(requireActivity(), "https://github.com/FasterXML/jackson-core/blob/2.14/LICENSE"));

        // OKHTTP
        bottomSheetBinding.okhttpLicense.setOnClickListener(v ->
                OpenURL(requireActivity(), "https://github.com/square/okhttp/blob/master/LICENSE.txt"));

        // ANDROID FAST SCROLL
        bottomSheetBinding.fastscrollLicense.setOnClickListener(v ->
                OpenURL(requireActivity(), "https://github.com/zhanghai/AndroidFastScroll/blob/master/LICENSE"));

        // MATERIAL DESIGN ICONS
        bottomSheetBinding.materialIconsLicense.setOnClickListener(v ->
                OpenURL(requireActivity(), "https://github.com/Templarian/MaterialDesign/blob/master/LICENSE"));

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
