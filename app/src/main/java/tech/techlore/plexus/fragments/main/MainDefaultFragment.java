package tech.techlore.plexus.fragments.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.tabs.TabLayout;

import tech.techlore.plexus.R;

public class MainDefaultFragment extends Fragment {

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
        return inflater.inflate(R.layout.fragment_main_default,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        final TabLayout mainTabLayout = view.findViewById(R.id.main_tab_layout);

    /*###########################################################################################*/

        // DISPLAY PLEXUS DATA FRAGMENT BY DEFAULT
        DisplayFragment("Plexus Data");

        // TAB LAYOUT
        mainTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if (tab.getPosition()==0){
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

    }

    // SETUP CHILD FRAGMENTS
    private void DisplayFragment(String fragmentName) {

        Fragment fragment;
        FragmentTransaction transaction=getChildFragmentManager().beginTransaction();

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

}
