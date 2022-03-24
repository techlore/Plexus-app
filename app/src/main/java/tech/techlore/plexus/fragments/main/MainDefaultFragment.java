package tech.techlore.plexus.fragments.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.io.Serializable;
import java.util.Objects;

import tech.techlore.plexus.R;
import tech.techlore.plexus.activities.MainActivity;
import tech.techlore.plexus.activities.SearchActivity;

public class MainDefaultFragment extends Fragment {

    public static ExtendedFloatingActionButton searchFab;

    public MainDefaultFragment() {
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
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_tab,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        final TabLayout mainTabLayout = view.findViewById(R.id.tab_layout);
        searchFab = view.findViewById(R.id.search_fab);

    /*###########################################################################################*/

        // DISPLAY PLEXUS DATA FRAGMENT BY DEFAULT
        DisplayFragment("Plexus Data");

        // TAB LAYOUT
        mainTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if (tab.getPosition() == 0){
                    DisplayFragment("Plexus Data");
                }
                else {
                    DisplayFragment("Installed Apps");
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // SEARCH FAB
        // DON'T FINISH MAIN ACTIVITY,
        // OR ELSE ISSUES WHEN GETTING LIST BACK FROM SEARCH ACTIVITY
        searchFab.setOnClickListener(v -> {

            if (Objects.requireNonNull(mainTabLayout.getTabAt(0)).isSelected()) {
                Search("plexusData");
            }
            else {
                Search("installedApps");
            }

            requireActivity().overridePendingTransition(R.anim.fade_in_slide_from_bottom, R.anim.no_movement);
        });

    }

    // SETUP CHILD FRAGMENTS
    private void DisplayFragment(String fragmentName) {

        Fragment fragment;
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

        if (fragmentName.equals("Plexus Data")) {
            fragment = new PlexusDataFragment();
            transaction.setCustomAnimations(R.anim.slide_from_start, R.anim.slide_to_end,
                    R.anim.slide_from_end, R.anim.slide_to_start);
        }

        else {
            fragment = new InstalledAppsFragment();
            transaction.setCustomAnimations(R.anim.slide_from_end, R.anim.slide_to_start,
                    R.anim.slide_from_start, R.anim.slide_to_end);
        }

        transaction.replace(R.id.tab_host_fragment, fragment)
                .commit();

    }

    // SEARCH ACTIVITY INTENT
    public void Search(String fromValue) {

        startActivity(new Intent(requireActivity(), SearchActivity.class)
                .putExtra("plexusDataList", (Serializable) ((MainActivity)requireActivity()).list)
                .putExtra("from", fromValue));

    }

}
