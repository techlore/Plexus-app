package tech.techlore.plexus.fragments.main;

import static tech.techlore.plexus.preferences.PreferenceManager.A_Z_SORT_PREF;
import static tech.techlore.plexus.preferences.PreferenceManager.DG_RATING_SORT_PREF;
import static tech.techlore.plexus.preferences.PreferenceManager.MG_RATING_SORT_PREF;
import static tech.techlore.plexus.preferences.PreferenceManager.RATING_RADIO_PREF;
import static tech.techlore.plexus.utils.IntentUtils.AppDetails;
import static tech.techlore.plexus.utils.UiUtils.InflateViewStub;
import static tech.techlore.plexus.utils.ListUtils.InstalledAppsRatingSort;
import static tech.techlore.plexus.utils.ListUtils.ScanInstalledApps;
import static tech.techlore.plexus.utils.UiUtils.LongClickBottomSheet;
import static tech.techlore.plexus.utils.UiUtils.ReloadFragment;

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
    private List<InstalledApp> installedAppsList;

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
        installedAppsList = new ArrayList<>();
        final InstalledAppItemAdapter installedAppItemAdapter = new InstalledAppItemAdapter(installedAppsList);

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
            InflateViewStub(fragmentBinding.emptyListViewStub);
            fragmentBinding.swipeRefreshLayout.setEnabled(false);
        }
        else {
            fragmentBinding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            fragmentBinding.recyclerView.setAdapter(installedAppItemAdapter);
        }

        // FAST SCROLL
        new FastScrollerBuilder(fragmentBinding.recyclerView).useMd2Style().build();

        // ON CLICK
        installedAppItemAdapter.setOnItemClickListener(position -> {
            InstalledApp installedApp = installedAppsList.get(position);
            AppDetails(mainActivity, installedApp.getName(), installedApp.getPackageName(),
                       installedApp.getPlexusVersion(), installedApp.getInstalledVersion(),
                       installedApp.getDgNotes(), installedApp.getMgNotes(),
                       installedApp.getDgRating(), installedApp.getMgRating());

        });

        // ON LONG CLICK
        installedAppItemAdapter.setOnItemLongClickListener(position -> {

            InstalledApp installedApp = installedAppsList.get(position);
            LongClickBottomSheet(mainActivity,
                                 installedApp.getName(), installedApp.getPackageName(), installedApp.getPlexusVersion(),
                                 installedApp.getDgRating(), installedApp.getMgRating(),
                                 installedApp.getDgNotes(), installedApp.getMgNotes());

        });

        // SWIPE REFRESH LAYOUT
        fragmentBinding.swipeRefreshLayout.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.backgroundColor, requireContext().getTheme()));
        fragmentBinding.swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary, requireContext().getTheme()));
        fragmentBinding.swipeRefreshLayout.setOnRefreshListener(() -> {
            mainActivity.installedList.clear();
            ScanInstalledApps(requireContext(), mainActivity.dataList, mainActivity.installedList);
            fragmentBinding.swipeRefreshLayout.setRefreshing(false);
            ReloadFragment(mainActivity.activityBinding.viewPager, mainActivity.viewPagerAdapter, 1);
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentBinding = null;
    }

}
