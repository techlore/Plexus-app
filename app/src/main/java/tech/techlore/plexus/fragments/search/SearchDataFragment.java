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

        // HANDLE CLICK EVENTS OF ITEMS
        plexusDataItemAdapter.setOnItemClickListener(position -> {

            PlexusData plexusData = searchDataList.get(position);
            AppDetails(searchActivity, plexusData.name, plexusData.packageName,
                       plexusData.version, null,
                       plexusData.dgNotes, plexusData.mgNotes,
                       plexusData.dgRating, plexusData.mgRating);

        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentBinding = null;
    }


}

