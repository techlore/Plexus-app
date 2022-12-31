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

import static tech.techlore.plexus.preferences.PreferenceManager.A_Z_SORT_PREF;
import static tech.techlore.plexus.preferences.PreferenceManager.DG_STATUS_SORT_PREF;
import static tech.techlore.plexus.preferences.PreferenceManager.FILTER_PREF;
import static tech.techlore.plexus.preferences.PreferenceManager.MG_STATUS_SORT_PREF;
import static tech.techlore.plexus.preferences.PreferenceManager.STATUS_RADIO_PREF;
import static tech.techlore.plexus.utils.IntentUtils.AppDetails;
import static tech.techlore.plexus.utils.IntentUtils.ReloadFragment;
import static tech.techlore.plexus.utils.ListUtils.InstalledAppsStatusSort;
import static tech.techlore.plexus.utils.ListUtils.ScanInstalledApps;
import static tech.techlore.plexus.utils.UiUtils.LongClickBottomSheet;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import me.zhanghai.android.fastscroll.FastScrollerBuilder;
import tech.techlore.plexus.R;
import tech.techlore.plexus.activities.MainActivity;
import tech.techlore.plexus.adapters.InstalledAppItemAdapter;
import tech.techlore.plexus.databinding.RecyclerViewBinding;
import tech.techlore.plexus.models.InstalledApp;
import tech.techlore.plexus.preferences.PreferenceManager;

public class InstalledAppsFragment extends Fragment {

    private RecyclerViewBinding fragmentBinding;
    private List<InstalledApp> installedAppsFinalList;

    public InstalledAppsFragment() {
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
        fragmentBinding = RecyclerViewBinding.inflate(inflater, container, false);
        return fragmentBinding.getRoot();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        final PreferenceManager preferenceManager = new PreferenceManager(requireContext());
        final MainActivity mainActivity = ((MainActivity) requireActivity());
        List<InstalledApp> installedAppsTempList = new ArrayList<>();
        installedAppsFinalList = new ArrayList<>();
        List<String> playStoreInstallers = new ArrayList<>(Arrays.asList("com.android.vending", "com.aurora.store"));
        final InstalledAppItemAdapter installedAppItemAdapter = new InstalledAppItemAdapter(installedAppsFinalList);

        /*###########################################################################################*/

        ((MainActivity) requireActivity()).activityBinding.toolbarTop.setTitle(R.string.installed_apps);

        // Filter based on installers (play store, aurora etc.)
        if (preferenceManager.getInt(FILTER_PREF) == 0
            || preferenceManager.getInt(FILTER_PREF) == R.id.menu_all_apps) {

            installedAppsTempList = mainActivity.installedList;

        }
        else if (preferenceManager.getInt(FILTER_PREF) == R.id.menu_play_apps) {

            for (InstalledApp installedApp : mainActivity.installedList) {

                String installerName = requireContext().getPackageManager()
                        .getInstallerPackageName(installedApp.getPackageName());

                if (playStoreInstallers.contains(installerName)) {
                    installedAppsTempList.add(installedApp);
                }

            }

        }
        else {

            for (InstalledApp installedApp : mainActivity.installedList) {

                String installerName = requireContext().getPackageManager()
                        .getInstallerPackageName(installedApp.getPackageName());

                if (!playStoreInstallers.contains(installerName)) {
                    installedAppsTempList.add(installedApp);
                }

            }
        }

        // Status sort
        for (InstalledApp installedApp : installedAppsTempList) {

            if (preferenceManager.getInt(STATUS_RADIO_PREF) == 0
                    || preferenceManager.getInt(STATUS_RADIO_PREF) == R.id.radio_any_status) {

                installedAppsFinalList.add(installedApp);
            }
            else if (preferenceManager.getInt(STATUS_RADIO_PREF) == R.id.radio_dg_status) {

                InstalledAppsStatusSort(preferenceManager.getInt(DG_STATUS_SORT_PREF), installedApp,
                        installedApp.dgRating, installedAppsFinalList);
            }
            else if (preferenceManager.getInt(STATUS_RADIO_PREF) == R.id.radio_mg_status) {

                InstalledAppsStatusSort(preferenceManager.getInt(MG_STATUS_SORT_PREF), installedApp,
                        installedApp.mgRating, installedAppsFinalList);
            }
        }

        // Alphabetical sort
        if (preferenceManager.getInt(A_Z_SORT_PREF) == 0
            || preferenceManager.getInt(A_Z_SORT_PREF) == R.id.sort_a_z) {

            //noinspection ComparatorCombinators
            Collections.sort(installedAppsFinalList, (ai1, ai2) ->
                    ai1.getName().compareTo(ai2.getName())); // A-Z

        }
        else {

            Collections.sort(installedAppsFinalList, (ai1, ai2) ->
                    ai2.getName().compareTo(ai1.getName())); // Z-A
        }

        if (installedAppsFinalList.size() == 0){
            fragmentBinding.emptyListViewStub.inflate();
        }
        else {
            fragmentBinding.recyclerView.setAdapter(installedAppItemAdapter);
            new FastScrollerBuilder(fragmentBinding.recyclerView).useMd2Style().build(); // Fast scroll
        }

        // On click
        installedAppItemAdapter.setOnItemClickListener(position -> {

            InstalledApp installedApp = installedAppsFinalList.get(position);
            AppDetails(mainActivity, installedApp.getName(), installedApp.getPackageName()
                       /*installedApp.getPlexusVersion(), installedApp.getInstalledVersion(),
                       installedApp.getDgNotes(), installedApp.getMgNotes(),
                       installedApp.getDgRating(), installedApp.getMgRating()*/);

        });

        // On long click
        installedAppItemAdapter.setOnItemLongClickListener(position -> {

            InstalledApp installedApp = installedAppsFinalList.get(position);
            LongClickBottomSheet(mainActivity,
                                 installedApp.getName(), installedApp.getPackageName(), /*installedApp.getPlexusVersion(),
                                 installedApp.getDgRating(), installedApp.getMgRating(),
                                 installedApp.getDgNotes(), installedApp.getMgNotes(),*/
                                 mainActivity.activityBinding.mainCoordinatorLayout,
                                 mainActivity.activityBinding.bottomNavContainer);

        });

        // Swipe refresh layout
        fragmentBinding.swipeRefreshLayout.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.backgroundColor, requireContext().getTheme()));
        fragmentBinding.swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent, requireContext().getTheme()));
        fragmentBinding.swipeRefreshLayout.setOnRefreshListener(() -> {
            mainActivity.installedList.clear();
            ScanInstalledApps(requireContext(), mainActivity.dataList, mainActivity.installedList);
            fragmentBinding.swipeRefreshLayout.setRefreshing(false);
            ReloadFragment(getParentFragmentManager(), this);
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentBinding = null;
    }

}
