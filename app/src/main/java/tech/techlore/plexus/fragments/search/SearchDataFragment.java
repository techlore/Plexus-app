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

package tech.techlore.plexus.fragments.search;

import static tech.techlore.plexus.utils.IntentUtils.AppDetails;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import tech.techlore.plexus.activities.SearchActivity;
import tech.techlore.plexus.adapters.PlexusDataItemAdapter;
import tech.techlore.plexus.databinding.RecyclerViewBinding;
import tech.techlore.plexus.models.PlexusData;

public class SearchDataFragment extends Fragment {

    private RecyclerViewBinding fragmentBinding;
    private PlexusDataItemAdapter plexusDataItemAdapter;
    private List<PlexusData> searchDataList;
    private CountDownTimer delayTimer;

    public SearchDataFragment() {
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
        fragmentBinding = RecyclerViewBinding.inflate(getLayoutInflater());
        return fragmentBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        final SearchActivity searchActivity = ((SearchActivity) requireActivity());
        searchDataList = searchActivity.dataList;
        plexusDataItemAdapter = new PlexusDataItemAdapter(searchDataList);

    /*###########################################################################################*/

        fragmentBinding.swipeRefreshLayout.setEnabled(false);
        fragmentBinding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Perform search
        searchActivity.searchViewBinding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchString) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String searchString) {

                if (delayTimer != null) {
                    delayTimer.cancel();
                }

                // Search with a subtle delay
                delayTimer = new CountDownTimer(350, 150) {

                    public void onTick(long millisUntilFinished) {}

                    public void onFinish() {

                        if (!searchString.isEmpty()) {
                            plexusDataItemAdapter.getFilter().filter(searchString);
                            fragmentBinding.recyclerView.setAdapter(plexusDataItemAdapter);
                        }
                        else {
                            fragmentBinding.recyclerView.setAdapter(null);
                        }

                    }
                }.start();

                return true;
            }
        });

        // On click
        plexusDataItemAdapter.setOnItemClickListener(position -> {

            PlexusData plexusData = searchDataList.get(position);
            AppDetails(searchActivity, plexusData.name, plexusData.packageName, null);
//                       plexusData.version, null,
//                       plexusData.dgNotes, plexusData.mgNotes,
//                       plexusData.dgStatus, plexusData.mgStatus);

        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentBinding = null;
    }


}

