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

import java.util.Objects;

import tech.techlore.plexus.R;
import tech.techlore.plexus.activities.SettingsActivity;
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
                    OpenURL(requireActivity(), "https://github.com/techlore/Plexus-app/blob/main/LICENSE"));

        // VIEW ON GITHUB
        fragmentBinding.viewOnGit
                .setOnClickListener(v ->
                        OpenURL(requireActivity(), "https://github.com/techlore/Plexus-app"));

        // VISIT TECHLORE
        fragmentBinding.visitTechlore
                .setOnClickListener(v ->
                        OpenURL(requireActivity(), "https://techlore.tech"));

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentBinding = null;
    }

}
