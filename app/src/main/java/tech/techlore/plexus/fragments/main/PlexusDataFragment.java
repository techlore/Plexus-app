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
import static tech.techlore.plexus.preferences.PreferenceManager.DG_RATING_SORT_PREF;
import static tech.techlore.plexus.preferences.PreferenceManager.MG_RATING_SORT_PREF;
import static tech.techlore.plexus.preferences.PreferenceManager.RATING_RADIO_PREF;
import static tech.techlore.plexus.utils.IntentUtils.AppDetails;
import static tech.techlore.plexus.utils.NetworkUtils.HasInternet;
import static tech.techlore.plexus.utils.NetworkUtils.HasNetwork;
import static tech.techlore.plexus.utils.NetworkUtils.URLResponse;
import static tech.techlore.plexus.utils.UiUtils.InflateViewStub;
import static tech.techlore.plexus.utils.ListUtils.PlexusDataRatingSort;
import static tech.techlore.plexus.utils.ListUtils.PopulateDataList;
import static tech.techlore.plexus.utils.UiUtils.LongClickBottomSheet;
import static tech.techlore.plexus.utils.UiUtils.ReloadViewPagerFragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

import me.zhanghai.android.fastscroll.FastScrollerBuilder;
import tech.techlore.plexus.R;
import tech.techlore.plexus.activities.MainActivity;
import tech.techlore.plexus.adapters.PlexusDataItemAdapter;
import tech.techlore.plexus.databinding.DialogFooterBinding;
import tech.techlore.plexus.databinding.DialogNoNetworkBinding;
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

        // RATING SORT
        for (PlexusData plexusData : mainActivity.dataList) {

            if (preferenceManager.getInt(RATING_RADIO_PREF) == 0
                || preferenceManager.getInt(RATING_RADIO_PREF) == R.id.radio_any_rating) {

                plexusDataList.add(plexusData);
            }

            else if (preferenceManager.getInt(RATING_RADIO_PREF) == R.id.radio_dg_rating) {

                PlexusDataRatingSort(preferenceManager.getInt(DG_RATING_SORT_PREF), plexusData,
                        plexusData.dgRating, plexusDataList);
            }

            else if (preferenceManager.getInt(RATING_RADIO_PREF) == R.id.radio_mg_rating) {

                PlexusDataRatingSort(preferenceManager.getInt(MG_RATING_SORT_PREF), plexusData,
                        plexusData.mgRating, plexusDataList);
            }
        }


        // ALPHABETICAL SORT
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

        if (plexusDataList.size() == 0){
            InflateViewStub(fragmentBinding.emptyListViewStub);
            fragmentBinding.swipeRefreshLayout.setEnabled(false);
        }
        else {
            fragmentBinding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            fragmentBinding.recyclerView.setAdapter(plexusDataItemAdapter);
        }

        // FAST SCROLL
        new FastScrollerBuilder(fragmentBinding.recyclerView).useMd2Style().build();

        // ON CLICK
        plexusDataItemAdapter.setOnItemClickListener(position -> {

            PlexusData plexusData = plexusDataList.get(position);
            AppDetails(mainActivity, plexusData.name, plexusData.packageName,
                       plexusData.version, null,
                       plexusData.dgNotes, plexusData.mgNotes,
                       plexusData.dgRating, plexusData.mgRating);

        });

        // ON LONG CLICK
        plexusDataItemAdapter.setOnItemLongClickListener(position -> {

            PlexusData plexusData = plexusDataList.get(position);
            LongClickBottomSheet(mainActivity, plexusData.name, plexusData.packageName, plexusData.version,
                                 plexusData.dgRating, plexusData.mgRating,
                                 plexusData.dgNotes, plexusData.mgNotes);

        });

        // SWIPE REFRESH LAYOUT
        fragmentBinding.swipeRefreshLayout.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.backgroundColor, requireContext().getTheme()));
        fragmentBinding.swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary, requireContext().getTheme()));
        fragmentBinding.swipeRefreshLayout.setOnRefreshListener(this::RefreshData);

    }

    // NO NETWORK DIALOG
    private void NoNetworkDialog() {
        final Dialog dialog = new Dialog(requireContext(), R.style.DialogTheme);
        dialog.setCancelable(false);

        dialog.getWindow().setBackgroundDrawable(ContextCompat
                .getDrawable(requireContext(), R.drawable.shape_rounded_corners));
        dialog.getWindow().getDecorView().setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.bottomSheetColor));

        final DialogNoNetworkBinding dialogBinding = DialogNoNetworkBinding.inflate(getLayoutInflater());
        final DialogFooterBinding footerBinding = DialogFooterBinding.bind(dialogBinding.getRoot());
        dialog.setContentView(dialogBinding.getRoot());

        // POSITIVE BUTTON
        footerBinding.positiveButton
                .setOnClickListener(view1 -> {
                    RefreshData();
                    dialog.dismiss();
                });

        // NEGATIVE BUTTON
        footerBinding.negativeButton
                .setOnClickListener(view12 -> {
                    dialog.cancel();
                    fragmentBinding.swipeRefreshLayout.setRefreshing(false);
                });

        // SHOW DIALOG WITH CUSTOM ANIMATION
        Objects.requireNonNull(dialog.getWindow()).getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void RefreshData(){

        Handler handler = new Handler(Looper.getMainLooper());

        if (HasNetwork(requireContext())) {

            Executors.newSingleThreadExecutor().execute(() -> {

                // BACKGROUND THREAD WORK
                if (HasInternet()) {

                    try {
                        jsonData = URLResponse();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // UI THREAD WORK
                    handler.post(() -> {
                        try {
                            mainActivity.dataList.clear();
                            mainActivity.dataList = PopulateDataList(jsonData);
                            fragmentBinding.swipeRefreshLayout.setRefreshing(false);
                            ReloadViewPagerFragment(mainActivity.activityBinding.viewPager, mainActivity.viewPagerAdapter, 0);
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
