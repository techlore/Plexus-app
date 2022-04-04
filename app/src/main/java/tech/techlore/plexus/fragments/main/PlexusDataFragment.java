package tech.techlore.plexus.fragments.main;

import static tech.techlore.plexus.preferences.PreferenceManager.A_Z_SORT_PREF;
import static tech.techlore.plexus.preferences.PreferenceManager.DG_RATING_SORT_PREF;
import static tech.techlore.plexus.preferences.PreferenceManager.MG_RATING_SORT_PREF;
import static tech.techlore.plexus.preferences.PreferenceManager.RATING_RADIO_PREF;
import static tech.techlore.plexus.utils.Utility.AppDetails;
import static tech.techlore.plexus.utils.Utility.InflateViewStub;
import static tech.techlore.plexus.utils.Utility.PlexusDataRatingSort;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.zhanghai.android.fastscroll.FastScrollerBuilder;
import tech.techlore.plexus.R;
import tech.techlore.plexus.activities.MainActivity;
import tech.techlore.plexus.adapters.PlexusDataItemAdapter;
import tech.techlore.plexus.models.PlexusData;
import tech.techlore.plexus.preferences.PreferenceManager;

public class PlexusDataFragment extends Fragment {

    private RecyclerView recyclerView;
    private PlexusDataItemAdapter plexusDataItemAdapter;
    private List<PlexusData> plexusDataList;

    public PlexusDataFragment() {
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
        return inflater.inflate(R.layout.recycler_view,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        final PreferenceManager preferenceManager = new PreferenceManager(requireContext());
        recyclerView = view.findViewById(R.id.recycler_view);
        final MainActivity mainActivity = ((MainActivity) requireActivity());
        plexusDataList = new ArrayList<>();
        plexusDataItemAdapter = new PlexusDataItemAdapter(plexusDataList);

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
            InflateViewStub(view.findViewById(R.id.empty_list_view_stub));
        }
        else {
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            recyclerView.setAdapter(plexusDataItemAdapter);
        }

        // FAST SCROLL
        new FastScrollerBuilder(recyclerView).useMd2Style().build();

        // HANDLE CLICK EVENTS OF ITEMS
        plexusDataItemAdapter.setOnItemClickListener(position -> {

            PlexusData plexusData = plexusDataList.get(position);
            AppDetails(mainActivity, plexusData.name, plexusData.packageName,
                       plexusData.version, null,
                       plexusData.dgNotes, plexusData.mgNotes,
                       plexusData.dgRating, plexusData.mgRating);

        });

    }

}
