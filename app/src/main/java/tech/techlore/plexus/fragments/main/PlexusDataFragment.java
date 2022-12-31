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
import static tech.techlore.plexus.preferences.PreferenceManager.MG_STATUS_SORT_PREF;
import static tech.techlore.plexus.preferences.PreferenceManager.STATUS_RADIO_PREF;
import static tech.techlore.plexus.utils.IntentUtils.AppDetails;
import static tech.techlore.plexus.utils.IntentUtils.ReloadFragment;
import static tech.techlore.plexus.utils.NetworkUtils.HasInternet;
import static tech.techlore.plexus.utils.NetworkUtils.HasNetwork;
import static tech.techlore.plexus.utils.NetworkUtils.URLResponse;
import static tech.techlore.plexus.utils.ListUtils.PlexusDataStatusSort;
import static tech.techlore.plexus.utils.ListUtils.PopulateDataList;
import static tech.techlore.plexus.utils.UiUtils.LongClickBottomSheet;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

import me.zhanghai.android.fastscroll.FastScrollerBuilder;
import tech.techlore.plexus.R;
import tech.techlore.plexus.activities.MainActivity;
import tech.techlore.plexus.adapters.PlexusDataItemAdapter;
import tech.techlore.plexus.databinding.RecyclerViewBinding;
import tech.techlore.plexus.models.PlexusData;
import tech.techlore.plexus.preferences.PreferenceManager;

public class PlexusDataFragment extends Fragment {

    private RecyclerViewBinding fragmentBinding;
    private MainActivity mainActivity;
    private List<PlexusData> plexusDataList;
    private static String jsonData;

    public PlexusDataFragment() {
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        final PreferenceManager preferenceManager = new PreferenceManager(requireContext());
        mainActivity = ((MainActivity) requireActivity());
        plexusDataList = new ArrayList<>();
        final PlexusDataItemAdapter plexusDataItemAdapter = new PlexusDataItemAdapter(plexusDataList);

    /*###########################################################################################*/

        ((MainActivity) requireActivity()).activityBinding.toolbarTop.setTitle(R.string.plexus_data);

        // Status sort
        for (PlexusData plexusData : mainActivity.dataList) {

            if (preferenceManager.getInt(STATUS_RADIO_PREF) == 0
                || preferenceManager.getInt(STATUS_RADIO_PREF) == R.id.radio_any_status) {

                plexusDataList.add(plexusData);
            }
//            else if (preferenceManager.getInt(STATUS_RADIO_PREF) == R.id.radio_dg_status) {
//
//                PlexusDataStatusSort(preferenceManager.getInt(DG_STATUS_SORT_PREF), plexusData,
//                        plexusData.dgStatus, plexusDataList);
//            }
//            else if (preferenceManager.getInt(STATUS_RADIO_PREF) == R.id.radio_mg_status) {
//
//                PlexusDataStatusSort(preferenceManager.getInt(MG_STATUS_SORT_PREF), plexusData,
//                        plexusData.mgStatus, plexusDataList);
//            }
        }


        // Alphabetical sort
        if (preferenceManager.getInt(A_Z_SORT_PREF) == 0
            || preferenceManager.getInt(A_Z_SORT_PREF) == R.id.sort_a_z) {

            //noinspection ComparatorCombinators
            Collections.sort(plexusDataList, (ai1, ai2) ->
                    ai1.name.compareTo(ai2.name)); // A-Z

        }
        else {

            Collections.sort(plexusDataList, (ai1, ai2) ->
                    ai2.name.compareTo(ai1.name)); // Z-A
        }

        if (mainActivity.dataList.size() == 0){
            fragmentBinding.emptyListViewStub.inflate();
        }
        else {
            fragmentBinding.recyclerView.setAdapter(plexusDataItemAdapter);
            new FastScrollerBuilder(fragmentBinding.recyclerView).useMd2Style().build(); // Fast scroll
        }

        // On click
        plexusDataItemAdapter.setOnItemClickListener(position -> {

            PlexusData plexusData = plexusDataList.get(position);
            AppDetails(mainActivity, plexusData.name, plexusData.packageName
                       /*plexusData.version, null,
                       plexusData.dgNotes, plexusData.mgNotes,
                       plexusData.dgStatus, plexusData.mgStatus*/);

        });

        // On long click
        plexusDataItemAdapter.setOnItemLongClickListener(position -> {

            PlexusData plexusData = plexusDataList.get(position);
            LongClickBottomSheet(mainActivity, plexusData.name, plexusData.packageName, /*plexusData.version,
                                 plexusData.dgStatus, plexusData.mgStatus,
                                 plexusData.dgNotes, plexusData.mgNotes,*/
                                 mainActivity.activityBinding.mainCoordinatorLayout,
                                 mainActivity.activityBinding.bottomNavContainer);

        });

        // Swipe refresh layout
        fragmentBinding.swipeRefreshLayout.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.backgroundColor, requireContext().getTheme()));
        fragmentBinding.swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent, requireContext().getTheme()));
        fragmentBinding.swipeRefreshLayout.setOnRefreshListener(this::RefreshData);

    }

    private void NoNetworkDialog() {

        new MaterialAlertDialogBuilder(requireContext(), R.style.DialogTheme)

                .setTitle(R.string.dialog_title)
                .setMessage(R.string.dialog_subtitle)

                .setPositiveButton(R.string.retry, (dialog, which) ->
                        RefreshData())

                .setNegativeButton(R.string.cancel, (dialog, which) ->
                        fragmentBinding.swipeRefreshLayout.setRefreshing(false))

                .show();

    }

    @SuppressLint("NotifyDataSetChanged")
    private void RefreshData(){

        Handler handler = new Handler(Looper.getMainLooper());

        if (HasNetwork(requireContext())) {

            Executors.newSingleThreadExecutor().execute(() -> {

                // Background thread work
                if (HasInternet()) {

                    try {
                        jsonData = URLResponse();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }

                    // UI Thread work
                    handler.post(() -> {
                        try {
                            mainActivity.dataList = PopulateDataList(jsonData);
                            fragmentBinding.swipeRefreshLayout.setRefreshing(false);
                            ReloadFragment(getParentFragmentManager(), this);
                        }
                        catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    });
                }
                else {
                    handler.post(this::NoNetworkDialog);
                }
            });
        }
        else {
            NoNetworkDialog();
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentBinding = null;
    }

}
