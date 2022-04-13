package tech.techlore.plexus.fragments.settings;

import static tech.techlore.plexus.utils.IntentUtils.OpenURL;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Objects;

import tech.techlore.plexus.R;
import tech.techlore.plexus.activities.SettingsActivity;

public class AboutFragment extends Fragment {

    String version;

    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_settings_about, container,  false);
        Objects.requireNonNull(((SettingsActivity) requireActivity()).getSupportActionBar()).setTitle(R.string.about_title);
        return v;
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
        ((TextView)view.findViewById(R.id.version_about)).setText(version);

        // PRIVACY POLICY
        view.findViewById(R.id.privacy_policy)
                .setOnClickListener(v ->
                    OpenURL(requireActivity(), "https://github.com/techlore/Plexus-app/blob/main/PRIVACY.md"));

        // LICENSES
        view.findViewById(R.id.licenses)
                .setOnClickListener(v ->
                    OpenURL(requireActivity(), "https://github.com/techlore/Plexus-app/blob/main/LICENSE"));

        // VIEW ON GITHUB
        view.findViewById(R.id.view_on_git)
                .setOnClickListener(v ->
                        OpenURL(requireActivity(), "https://github.com/techlore/Plexus-app"));

        // VISIT TECHLORE
        view.findViewById(R.id.visit_techlore)
                .setOnClickListener(v ->
                        OpenURL(requireActivity(), "https://techlore.tech"));

    }

}
