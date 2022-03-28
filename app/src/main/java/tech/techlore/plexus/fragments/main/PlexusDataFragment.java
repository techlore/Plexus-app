package tech.techlore.plexus.fragments.main;

import static tech.techlore.plexus.preferences.PreferenceManager.A_Z_SORT_PREF;
import static tech.techlore.plexus.preferences.PreferenceManager.RATING_SORT_PREF;
import static tech.techlore.plexus.utils.Utility.AppDetails;

import android.os.Bundle;
import android.os.CountDownTimer;
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
    private CountDownTimer delayTimer;

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

            if (preferenceManager.getInt(RATING_SORT_PREF) == 0
                || preferenceManager.getInt(RATING_SORT_PREF) == R.id.sort_any) {

                plexusDataList.add(plexusData);
            }

            else if (preferenceManager.getInt(RATING_SORT_PREF) == R.id.sort_not_tested) {

                if (plexusData.dgRating.equals("X") || plexusData.mgRating.equals("X")) {

                    plexusDataList.add(plexusData);
                }
            }

            else if (preferenceManager.getInt(RATING_SORT_PREF) == R.id.sort_unusable) {

                if (plexusData.dgRating.equals("1") || plexusData.mgRating.equals("1")) {

                    plexusDataList.add(plexusData);
                }
            }

            else if (preferenceManager.getInt(RATING_SORT_PREF) == R.id.sort_acceptable) {

                if (plexusData.dgRating.equals("2") || plexusData.mgRating.equals("2")) {

                    plexusDataList.add(plexusData);
                }
            }

            else if (preferenceManager.getInt(RATING_SORT_PREF) == R.id.sort_good) {

                if (plexusData.dgRating.equals("3") || plexusData.mgRating.equals("3")) {

                    plexusDataList.add(plexusData);
                }
            }

            else if (preferenceManager.getInt(RATING_SORT_PREF) == R.id.sort_perfect) {

                if (plexusData.dgRating.equals("4") || plexusData.mgRating.equals("4")) {

                    plexusDataList.add(plexusData);
                }
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

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(plexusDataItemAdapter);

        // FAST SCROLL
        new FastScrollerBuilder(recyclerView).useMd2Style().build();

        // HANDLE CLICK EVENTS OF ITEMS
        plexusDataItemAdapter.setOnItemClickListener(position -> {

            PlexusData plexusData = plexusDataList.get(position);
            AppDetails(mainActivity, plexusData.name, plexusData.packageName, plexusData.version,
                    plexusData.dgNotes, plexusData.mgNotes, plexusData.dgRating, plexusData.mgRating);

        });

        // SHRINK FAB ON SCROLL
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // CHECK IF SCROLLING OR NOT
                //   0 = NO SCROLL
                // > 0 = SCROLL UP
                // < 0 = SCROLL DOWN
                if (dy != 0) {

                    // SHRINK FAB WHEN SCROLLING
                    mainActivity.searchFab.shrink();

                    if (delayTimer != null) {
                        delayTimer.cancel();
                    }

                    // EXTEND FAB WHEN SCROLLING STOPPED
                    // WITH A SUBTLE DELAY
                    delayTimer = new CountDownTimer(400, 100) {

                        public void onTick(long millisUntilFinished) {}

                        // ON TIMER FINISH, EXTEND FAB
                        public void onFinish() {
                            mainActivity.searchFab.extend();
                        }
                    }.start();
                }

            }
        });

    }

}
