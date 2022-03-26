package tech.techlore.plexus.fragments.search;

import static tech.techlore.plexus.utils.Utility.AppDetails;

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
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import tech.techlore.plexus.R;
import tech.techlore.plexus.activities.SearchActivity;
import tech.techlore.plexus.adapters.InstalledAppItemAdapter;
import tech.techlore.plexus.models.InstalledApp;

public class SearchInstalledFragment extends Fragment {

    private RecyclerView recyclerView;
    private InstalledAppItemAdapter installedAppItemAdapter;
    private List<InstalledApp> searchInstalledList;
    private CountDownTimer delayTimer;

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
        searchInstalledList = searchActivity.installedList;
        installedAppItemAdapter = new InstalledAppItemAdapter(searchInstalledList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        /*###########################################################################################*/

        // PERFORM SEARCH
        searchActivity.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchString) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String searchString) {

                if (delayTimer != null) {
                    delayTimer.cancel();
                }

                // SEARCH WITH A SUBTLE DELAY
                delayTimer = new CountDownTimer(350, 150) {

                    public void onTick(long millisUntilFinished) {}

                    // ON TIMER FINISH, EXTEND FAB
                    public void onFinish() {

                        if (!searchString.isEmpty()) {
                            installedAppItemAdapter.getFilter().filter(searchString);
                            recyclerView.setAdapter(installedAppItemAdapter);
                        }
                        else {
                            recyclerView.setAdapter(null);
                        }

                    }
                }.start();

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
