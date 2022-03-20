package tech.techlore.plexus.fragments.search;

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
import tech.techlore.plexus.adapters.AppItemAdapter;
import tech.techlore.plexus.models.App;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private AppItemAdapter rAdapter;
    private List<App> appsList;
    private App app;

    public SearchFragment() {
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
        return inflater.inflate(R.layout.fragment_main,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        recyclerView = view.findViewById(R.id.apps_recycler_view);
        final SearchActivity searchActivity = ((SearchActivity) requireActivity());
        appsList = searchActivity.list;

        /*===========================================================================================*/

        rAdapter = new AppItemAdapter(appsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        searchActivity.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchString) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchString) {

                if (!searchString.isEmpty()) {
                    rAdapter.getFilter().filter(searchString);
                    recyclerView.setAdapter(rAdapter);
                }

                return true;
            }
        });



        // HANDLE CLICK EVENTS OF ITEMS
        rAdapter.setOnItemClickListener(position -> {

            app = appsList.get(position);
            searchActivity.AppDetails(app.name, app.packageName, app.version,
                    app.dgNotes, app.mgNotes, app.dgRating, app.mgRating);

        });

    }

}

