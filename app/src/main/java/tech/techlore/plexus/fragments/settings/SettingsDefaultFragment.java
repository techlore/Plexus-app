package tech.techlore.plexus.fragments.settings;

import static tech.techlore.plexus.preferences.PreferenceManager.THEME_PREF;
import static tech.techlore.plexus.utils.IntentUtils.OpenURL;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Objects;

import tech.techlore.plexus.R;
import tech.techlore.plexus.activities.SettingsActivity;
import tech.techlore.plexus.preferences.PreferenceManager;

public class SettingsDefaultFragment extends Fragment {

    private PreferenceManager preferenceManager;

    public SettingsDefaultFragment() {
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
        View v = inflater.inflate(R.layout.fragment_settings_default, container,  false);
        Objects.requireNonNull(((SettingsActivity) requireActivity()).getSupportActionBar()).setTitle(R.string.menu_settings);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        preferenceManager=new PreferenceManager(requireActivity());
        TextView chooseThemeSubtitle = view.findViewById(R.id.settings_theme_subtitle);

    /*############################################################################################*/

        // THEME
        view.findViewById(R.id.settings_theme_holder)
                .setOnClickListener(v1 ->
                        ThemeBottomSheet());

        if (preferenceManager.getInt(THEME_PREF) == 0){
            if (Build.VERSION.SDK_INT >= 29){
                chooseThemeSubtitle.setText(R.string.system_default);
            }
            else{
                chooseThemeSubtitle.setText(R.string.light);
            }
        }
        else if (preferenceManager.getInt(THEME_PREF) == R.id.sys_default){
            chooseThemeSubtitle.setText(R.string.system_default);
        }
        else if (preferenceManager.getInt(THEME_PREF) == R.id.light){
            chooseThemeSubtitle.setText(R.string.light);
        }
        else if (preferenceManager.getInt(THEME_PREF) == R.id.dark){
            chooseThemeSubtitle.setText(R.string.dark);
        }

        // REPORT AN ISSUE
        view.findViewById(R.id.settings_report_issue_holder)
                .setOnClickListener(v2 ->
                    OpenURL(requireActivity(), "https://github.com/techlore/Plexus-app/issues"));


        // ABOUT
        view.findViewById(R.id.settings_about_holder)
                .setOnClickListener(v3 ->
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

        @SuppressLint("InflateParams") final View view  = getLayoutInflater().inflate(R.layout.bottom_sheet_theme, null);
        bottomSheetDialog.setContentView(view);

        final RadioGroup themeRadioGroup = view.findViewById(R.id.theme_radiogroup);

        // TITLE
        ((TextView)view.findViewById(R.id.bottom_sheet_title)).setText(R.string.theme_title);

        // DEFAULT CHECKED RADIO
        if (preferenceManager.getInt(THEME_PREF) == 0){
            if (Build.VERSION.SDK_INT >= 29){
                preferenceManager.setInt(THEME_PREF, R.id.sys_default);
            }
            else{
                preferenceManager.setInt(THEME_PREF, R.id.light);
            }
        }
        themeRadioGroup.check(preferenceManager.getInt(THEME_PREF));

        // SHOW SYSTEM DEFAULT OPTION ONLY ON SDK 29 AND ABOVE
        if (Build.VERSION.SDK_INT >= 29){
            view.findViewById(R.id.sys_default).setVisibility(View.VISIBLE);
        }
        else{
            view.findViewById(R.id.sys_default).setVisibility(View.GONE);
        }

        // ON SELECTING OPTION
        ((RadioGroup)view.findViewById(R.id.theme_radiogroup))
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

        // CANCEL BUTTON
        view.findViewById(R.id.cancel_button).setOnClickListener(view12 ->
                bottomSheetDialog.cancel());

        // SHOW BOTTOM SHEET WITH CUSTOM ANIMATION
        Objects.requireNonNull(bottomSheetDialog.getWindow()).getAttributes().windowAnimations = R.style.BottomSheetAnimation;
        bottomSheetDialog.show();
    }

}
