package tech.techlore.plexus.fragments.main;

import static tech.techlore.plexus.preferences.PreferenceManager.A_Z_SORT_PREF;
import static tech.techlore.plexus.preferences.PreferenceManager.DG_RATING_SORT_PREF;
import static tech.techlore.plexus.preferences.PreferenceManager.MG_RATING_SORT_PREF;
import static tech.techlore.plexus.preferences.PreferenceManager.RATING_RADIO_PREF;
import static tech.techlore.plexus.utils.Utility.AppDetails;
import static tech.techlore.plexus.utils.Utility.EmptyList;
import static tech.techlore.plexus.utils.Utility.InstalledAppsRatingSort;

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
import tech.techlore.plexus.adapters.InstalledAppItemAdapter;
import tech.techlore.plexus.models.InstalledApp;
import tech.techlore.plexus.preferences.PreferenceManager;

public class InstalledAppsFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<InstalledApp> installedAppsList;
    private InstalledAppItemAdapter installedAppItemAdapter;
    private CountDownTimer delayTimer;

    public InstalledAppsFragment() {
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
        return inflater.inflate(R.layout.recycler_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        final PreferenceManager preferenceManager=new PreferenceManager(requireContext());
        final MainActivity mainActivity = ((MainActivity) requireActivity());
        recyclerView = view.findViewById(R.id.recycler_view);
        installedAppsList = new ArrayList<>();
        installedAppItemAdapter = new InstalledAppItemAdapter(installedAppsList);

    /*###########################################################################################*/

        // RATING SORT
        for (InstalledApp installedApp : mainActivity.installedList) {

            if (preferenceManager.getInt(RATING_RADIO_PREF) == 0
                    || preferenceManager.getInt(RATING_RADIO_PREF) == R.id.radio_any_rating) {

                installedAppsList.add(installedApp);
            }

            else if (preferenceManager.getInt(RATING_RADIO_PREF) == R.id.radio_dg_rating) {

                InstalledAppsRatingSort(preferenceManager.getInt(DG_RATING_SORT_PREF), installedApp,
                        installedApp.dgRating, installedAppsList);
            }

            else if (preferenceManager.getInt(RATING_RADIO_PREF) == R.id.radio_mg_rating) {

                InstalledAppsRatingSort(preferenceManager.getInt(MG_RATING_SORT_PREF), installedApp,
                        installedApp.mgRating, installedAppsList);
            }
        }

        // SORT ALPHABETICALLY
        if (preferenceManager.getInt(A_Z_SORT_PREF) == 0
                || preferenceManager.getInt(A_Z_SORT_PREF) == R.id.sort_a_z) {

            //noinspection ComparatorCombinators
            Collections.sort(installedAppsList, (ai1, ai2) ->
                    ai1.getName().compareTo(ai2.getName())); // A-Z

        }

        else {

            Collections.sort(installedAppsList, (ai1, ai2) ->
                    ai2.getName().compareTo(ai1.getName())); // Z-A
        }

        if (installedAppsList.size() == 0){
            EmptyList(view.findViewById(R.id.empty_db_view_stub));
        }
        else {
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            recyclerView.setAdapter(installedAppItemAdapter);
        }

        // FAST SCROLL
        new FastScrollerBuilder(recyclerView).useMd2Style().build();

        // HANDLE CLICK EVENTS OF ITEMS
        installedAppItemAdapter.setOnItemClickListener(position -> {
            InstalledApp installedApp = installedAppsList.get(position);
            AppDetails(mainActivity, installedApp.getName(), installedApp.getPackageName(),
                        installedApp.getVersion(), installedApp.getDgNotes(),
                        installedApp.getMgNotes(), installedApp.getDgRating(),
                        installedApp.getMgRating());

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
