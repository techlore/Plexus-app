package tech.techlore.plexus.fragments.main;

import static tech.techlore.plexus.fragments.main.MainDefaultFragment.searchFab;
import static tech.techlore.plexus.utils.Utility.AppDetails;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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

import tech.techlore.plexus.R;
import tech.techlore.plexus.activities.MainActivity;
import tech.techlore.plexus.adapters.InstalledAppItemAdapter;
import tech.techlore.plexus.models.InstalledApp;
import tech.techlore.plexus.models.PlexusData;

public class InstalledAppsFragment extends Fragment {

    private RecyclerView recyclerView;
    private PackageManager packageManager;
    private List<PlexusData> plexusDataList;
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

        final MainActivity mainActivity = ((MainActivity) requireActivity());
        recyclerView = view.findViewById(R.id.recycler_view);
        packageManager = requireContext().getPackageManager();
        plexusDataList = mainActivity.dataList;
        installedAppsList = new ArrayList<>();
        installedAppItemAdapter = new InstalledAppItemAdapter(installedAppsList);

    /*###########################################################################################*/

        // SCAN INSTALLED APPS
        for (ApplicationInfo appInfo : packageManager.getInstalledApplications(PackageManager.GET_META_DATA)){

            InstalledApp installedApp = new InstalledApp();
            String dgRating="X", mgRating="X", dgNotes="X", mgNotes="X";

            // NO SYSTEM APPS
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) !=1) {

                installedApp.setName(String.valueOf(appInfo.loadLabel(packageManager)));
                installedApp.setPackageName(appInfo.packageName);

                try {
                    PackageInfo packageInfo = packageManager.getPackageInfo(appInfo.packageName, 0);
                    installedApp.setVersion(packageInfo.versionName);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                // SEARCH FOR THE PACKAGE NAME IN PLEXUS DATA
                // TO SET RATINGS AND NOTES
                for (PlexusData plexusData : plexusDataList) {

                    if (plexusData.packageName.contains(appInfo.packageName)) {
                        dgRating = plexusData.dgRating;
                        mgRating = plexusData.mgRating;
                        dgNotes = plexusData.dgNotes;
                        mgNotes = plexusData.mgNotes;
                    }

                }

                installedApp.setDgRating(dgRating);
                installedApp.setMgRating(mgRating);
                installedApp.setDgNotes(dgNotes);
                installedApp.setMgNotes(mgNotes);
                installedAppsList.add(installedApp);
            }

        }

        // SORT ALPHABETICALLY
        //noinspection ComparatorCombinators
        Collections.sort(installedAppsList, (ai1, ai2) ->
                ai1.getName().compareTo(ai2.getName()));

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(installedAppItemAdapter);

        // GIVE THE LIST TO MAIN ACTIVITY
        // TO FURTHER GIVE IT TO SEARCH ACTIVITY WHEN REQUIRED
        mainActivity.installedList = installedAppsList;

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
                    searchFab.shrink();

                    if (delayTimer != null) {
                        delayTimer.cancel();
                    }

                    // EXTEND FAB WHEN SCROLLING STOPPED
                    // WITH A SUBTLE DELAY
                    delayTimer = new CountDownTimer(400, 100) {

                        public void onTick(long millisUntilFinished) {}

                        // ON TIMER FINISH, EXTEND FAB
                        public void onFinish() {
                            searchFab.extend();
                        }
                    }.start();
                }

            }
        });

    }

}
