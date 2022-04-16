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
import tech.techlore.plexus.adapters.InstalledAppItemAdapter;
import tech.techlore.plexus.databinding.RecyclerViewBinding;
import tech.techlore.plexus.models.InstalledApp;

public class SearchInstalledFragment extends Fragment {

    private RecyclerViewBinding fragmentBinding;
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentBinding = RecyclerViewBinding.inflate(getLayoutInflater());
        return fragmentBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        final SearchActivity searchActivity = ((SearchActivity) requireActivity());
        searchInstalledList = searchActivity.installedList;
        installedAppItemAdapter = new InstalledAppItemAdapter(searchInstalledList);

        /*###########################################################################################*/

        fragmentBinding.swipeRefreshLayout.setEnabled(false);
        fragmentBinding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // PERFORM SEARCH
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

                // SEARCH WITH A SUBTLE DELAY
                delayTimer = new CountDownTimer(350, 150) {

                    public void onTick(long millisUntilFinished) {}

                    // ON TIMER FINISH, EXTEND FAB
                    public void onFinish() {

                        if (!searchString.isEmpty()) {
                            installedAppItemAdapter.getFilter().filter(searchString);
                            fragmentBinding.recyclerView.setAdapter(installedAppItemAdapter);
                        }
                        else {
                            fragmentBinding.recyclerView.setAdapter(null);
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
                        installedApp.getPlexusVersion(), installedApp.getInstalledVersion(),
                        installedApp.getDgNotes(), installedApp.getMgNotes(),
                        installedApp.getDgRating(), installedApp.getMgRating());

        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentBinding = null;
    }

}
