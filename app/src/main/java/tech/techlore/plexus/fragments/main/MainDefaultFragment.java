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

import tech.techlore.plexus.R;
import tech.techlore.plexus.activities.MainActivity;
import tech.techlore.plexus.activities.SearchActivity;

public class MainDefaultFragment extends Fragment {

    public static ExtendedFloatingActionButton searchFab;
    private MainActivity mainActivity;

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

        final TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        searchFab = view.findViewById(R.id.search_fab);
        mainActivity = ((MainActivity) requireActivity());

    /*###########################################################################################*/

        // DISPLAY FRAGMENT WHEN NEWLY CREATED OR REATTACHED
        tabLayout.selectTab(tabLayout.getTabAt(mainActivity.selectedTab));
        DisplayFragment(tabLayout.getSelectedTabPosition());

        // TAB LAYOUT
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                DisplayFragment(tab.getPosition());

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
        searchFab.setOnClickListener(v ->
                StartSearch(tabLayout.getSelectedTabPosition()));

    }

    // SETUP CHILD FRAGMENTS
    private void DisplayFragment(int selectedTab) {

        Fragment fragment;
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

        if (selectedTab == 0) {
            fragment = new PlexusDataFragment();
            transaction.setCustomAnimations(R.anim.slide_from_start, R.anim.slide_to_end,
                    R.anim.slide_from_end, R.anim.slide_to_start);
        }

        else {
            fragment = new InstalledAppsFragment();
            transaction.setCustomAnimations(R.anim.slide_from_end, R.anim.slide_to_start,
                    R.anim.slide_from_start, R.anim.slide_to_end);
        }

        mainActivity.selectedTab = selectedTab;
        transaction.replace(R.id.tab_host_fragment, fragment)
                .commit();

    }

    // SEARCH ACTIVITY INTENT
    public void StartSearch(int selectedTab) {

        Intent searchIntent = new Intent(requireActivity(), SearchActivity.class);
        searchIntent.putExtra("from", selectedTab);

        // IF FROM PLEXUS DATA TAB,
        // GIVE PLEXUS DATA LIST TO SEARCH ACTIVITY
        if (selectedTab == 0) {
            searchIntent.putExtra("plexusDataList", (Serializable) mainActivity.dataList);
        }

        // ELSE GIVE INSTALLED APPS LIST TO SEARCH ACTIVITY
        else {
            searchIntent.putExtra("installedList", (Serializable) mainActivity.installedList);
        }

        startActivity(searchIntent);
        requireActivity().overridePendingTransition(R.anim.fade_in_slide_from_bottom, R.anim.no_movement);

    }

}
