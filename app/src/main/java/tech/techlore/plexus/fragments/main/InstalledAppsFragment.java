package tech.techlore.plexus.fragments.main;

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

public class InstalledAppsFragment extends Fragment {

    private RecyclerView recyclerView;
    private PackageManager packageManager;
    private List<InstalledApp> installedAppList;
    private InstalledAppItemAdapter installedAppItemAdapter;
    private InstalledApp installedApp;
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
        installedAppList = new ArrayList<>();
        installedAppItemAdapter = new InstalledAppItemAdapter(installedAppList);

        for (ApplicationInfo appInfo : packageManager.getInstalledApplications(PackageManager.GET_META_DATA)){

            InstalledApp installedApp = new InstalledApp();

            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) !=1
                /*|| (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) !=0*/ ) {
                // NO SYSTEM APPS

                installedApp.setName(String.valueOf(appInfo.loadLabel(packageManager)));
                installedApp.setPackageName(appInfo.packageName);
                try {
                    PackageInfo packageInfo = packageManager.getPackageInfo(appInfo.packageName, 0);
                    installedApp.setVersion(packageInfo.versionName);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                installedApp.setDgRating("X");
                installedApp.setMgRating("X");
                installedApp.setDgNotes("X");
                installedApp.setMgNotes("X");
                installedAppList.add(installedApp);
            }

        }

        // SORT ALPHABETICALLY
        //noinspection ComparatorCombinators
        Collections.sort(installedAppList, (ai1, ai2) ->
                ai1.getName().compareTo(ai2.getName()));

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(installedAppItemAdapter);

        // HANDLE CLICK EVENTS OF ITEMS
        installedAppItemAdapter.setOnItemClickListener(position -> {

            installedApp = installedAppList.get(position);
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
                    mainActivity.extFab.shrink();

                    if (delayTimer != null) {
                        delayTimer.cancel();
                    }

                    // EXTEND FAB WHEN SCROLLING STOPPED
                    // WITH A SUBTLE DELAY
                    delayTimer = new CountDownTimer(400, 100) {

                        public void onTick(long millisUntilFinished) {}

                        // ON TIMER FINISH, EXTEND FAB
                        public void onFinish() {
                            mainActivity.extFab.extend();
                        }
                    }.start();
                }

            }
        });

    }

}
