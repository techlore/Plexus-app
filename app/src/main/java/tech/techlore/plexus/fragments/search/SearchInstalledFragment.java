package tech.techlore.plexus.fragments.search;

import static tech.techlore.plexus.utils.Utility.AppDetails;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import tech.techlore.plexus.R;
import tech.techlore.plexus.activities.SearchActivity;
import tech.techlore.plexus.adapters.InstalledAppItemAdapter;
import tech.techlore.plexus.models.InstalledApp;
import tech.techlore.plexus.models.PlexusData;

public class SearchInstalledFragment extends Fragment {

    private RecyclerView recyclerView;
    private PackageManager packageManager;
    private InstalledAppItemAdapter installedAppItemAdapter;
    private List<InstalledApp> searchInstalledList;
    private List<ApplicationInfo> scannedApps;

    public SearchInstalledFragment() {
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
        return inflater.inflate(R.layout.recycler_view,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        recyclerView = view.findViewById(R.id.recycler_view);
        final SearchActivity searchActivity = ((SearchActivity) requireActivity());
        packageManager = requireContext().getPackageManager();
        searchInstalledList = new ArrayList<>();
        scannedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        installedAppItemAdapter = new InstalledAppItemAdapter(searchInstalledList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        /*###########################################################################################*/

        // SCAN INSTALLED APPS
        for (ApplicationInfo appInfo : scannedApps){

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
                for (PlexusData plexusData : searchActivity.list) {

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
                searchInstalledList.add(installedApp);
            }

        }

        // PERFORM SEARCH
        searchActivity.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchString) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String searchString) {

                if (!searchString.isEmpty()) {
                    installedAppItemAdapter.getFilter().filter(searchString);
                    recyclerView.setAdapter(installedAppItemAdapter);
                }
                else {
                    recyclerView.setAdapter(null);
                }

                return true;
            }
        });

        // HANDLE CLICK EVENTS OF ITEMS
        installedAppItemAdapter.setOnItemClickListener(position -> {

            InstalledApp installedApp = searchInstalledList.get(position);
            AppDetails(searchActivity, installedApp.getName(), installedApp.getPackageName(),
                        installedApp.getVersion(), installedApp.getDgNotes(),
                        installedApp.getMgNotes(), installedApp.getDgRating(),
                        installedApp.getMgRating());

        });

    }

}
