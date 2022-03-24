package tech.techlore.plexus.fragments.search;

import static tech.techlore.plexus.utils.Utility.AppDetails;

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

import java.util.List;

import tech.techlore.plexus.R;
import tech.techlore.plexus.activities.SearchActivity;
import tech.techlore.plexus.adapters.PlexusDataItemAdapter;
import tech.techlore.plexus.models.PlexusData;

public class SearchDataFragment extends Fragment {

    private RecyclerView recyclerView;
    private PlexusDataItemAdapter plexusDataItemAdapter;
    private List<PlexusData> searchDataList;

    public SearchDataFragment() {
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
        searchDataList = searchActivity.list;
        plexusDataItemAdapter = new PlexusDataItemAdapter(searchDataList);
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

                if (!searchString.isEmpty()) {
                    plexusDataItemAdapter.getFilter().filter(searchString);
                    recyclerView.setAdapter(plexusDataItemAdapter);
                }
                else {
                    recyclerView.setAdapter(null);
                }

                return true;
            }
        });



        // HANDLE CLICK EVENTS OF ITEMS
        plexusDataItemAdapter.setOnItemClickListener(position -> {

            PlexusData plexusData = searchDataList.get(position);
            AppDetails(searchActivity, plexusData.name, plexusData.packageName, plexusData.version,
                    plexusData.dgNotes, plexusData.mgNotes, plexusData.dgRating, plexusData.mgRating);

        });

    }

}

