package tech.techlore.plexus.fragments.main;

import static tech.techlore.plexus.preferences.PreferenceManager.A_Z_SORT_PREF;
import static tech.techlore.plexus.preferences.PreferenceManager.DG_RATING_SORT_PREF;
import static tech.techlore.plexus.preferences.PreferenceManager.MG_RATING_SORT_PREF;
import static tech.techlore.plexus.preferences.PreferenceManager.RATING_RADIO_PREF;
import static tech.techlore.plexus.utils.IntentUtils.AppDetails;
import static tech.techlore.plexus.utils.NetworkUtils.HasInternet;
import static tech.techlore.plexus.utils.NetworkUtils.HasNetwork;
import static tech.techlore.plexus.utils.NetworkUtils.URLResponse;
import static tech.techlore.plexus.utils.UiUtils.InflateViewStub;
import static tech.techlore.plexus.utils.ListUtils.PlexusDataRatingSort;
import static tech.techlore.plexus.utils.ListUtils.PopulateDataList;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

import me.zhanghai.android.fastscroll.FastScrollerBuilder;
import tech.techlore.plexus.R;
import tech.techlore.plexus.activities.MainActivity;
import tech.techlore.plexus.adapters.PlexusDataItemAdapter;
import tech.techlore.plexus.models.PlexusData;
import tech.techlore.plexus.preferences.PreferenceManager;

public class PlexusDataFragment extends Fragment {

    private MainActivity mainActivity;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private PlexusDataItemAdapter plexusDataItemAdapter;
    private List<PlexusData> plexusDataList;
    private static String jsonData;

    public PlexusDataFragment() {
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
        return inflater.inflate(R.layout.recycler_view,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        final PreferenceManager preferenceManager = new PreferenceManager(requireContext());
        mainActivity = ((MainActivity) requireActivity());
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        recyclerView = view.findViewById(R.id.recycler_view);
        plexusDataList = new ArrayList<>();
        plexusDataItemAdapter = new PlexusDataItemAdapter(plexusDataList);

    /*###########################################################################################*/

        // RATING SORT
        for (PlexusData plexusData : mainActivity.dataList) {

            if (preferenceManager.getInt(RATING_RADIO_PREF) == 0
                || preferenceManager.getInt(RATING_RADIO_PREF) == R.id.radio_any_rating) {

                plexusDataList.add(plexusData);
            }

            else if (preferenceManager.getInt(RATING_RADIO_PREF) == R.id.radio_dg_rating) {

                PlexusDataRatingSort(preferenceManager.getInt(DG_RATING_SORT_PREF), plexusData,
                        plexusData.dgRating, plexusDataList);
            }

            else if (preferenceManager.getInt(RATING_RADIO_PREF) == R.id.radio_mg_rating) {

                PlexusDataRatingSort(preferenceManager.getInt(MG_RATING_SORT_PREF), plexusData,
                        plexusData.mgRating, plexusDataList);
            }
        }


        // ALPHABETICAL SORT
        if (preferenceManager.getInt(A_Z_SORT_PREF) == 0
            || preferenceManager.getInt(A_Z_SORT_PREF) == R.id.sort_a_z) {

            //noinspection ComparatorCombinators
            Collections.sort(plexusDataList, (ai1, ai2) ->
                    ai1.name.compareTo(ai2.name)); // A-Z

        }

        else {

            Collections.sort(plexusDataList, (ai1, ai2) ->
                    ai2.name.compareTo(ai1.name)); // Z-A
        }

        if (plexusDataList.size() == 0){
            InflateViewStub(view.findViewById(R.id.empty_list_view_stub));
            swipeRefreshLayout.setEnabled(false);
        }
        else {
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            recyclerView.setAdapter(plexusDataItemAdapter);
        }

        // FAST SCROLL
        new FastScrollerBuilder(recyclerView).useMd2Style().build();

        // HANDLE CLICK EVENTS OF ITEMS
        plexusDataItemAdapter.setOnItemClickListener(position -> {

            PlexusData plexusData = plexusDataList.get(position);
            AppDetails(mainActivity, plexusData.name, plexusData.packageName,
                       plexusData.version, null,
                       plexusData.dgNotes, plexusData.mgNotes,
                       plexusData.dgRating, plexusData.mgRating);

        });

        // SWIPE REFRESH LAYOUT
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.backgroundColor, requireContext().getTheme()));
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary, requireContext().getTheme()));
        swipeRefreshLayout.setOnRefreshListener(this::RefreshData);

    }

    // NO NETWORK DIALOG
    private void NoNetworkDialog() {
        final Dialog dialog = new Dialog(requireContext(), R.style.DialogTheme);
        dialog.setCancelable(false);

        @SuppressLint("InflateParams") View view  = getLayoutInflater().inflate(R.layout.dialog_no_network, null);
        dialog.getWindow().setBackgroundDrawable(ContextCompat
                .getDrawable(requireContext(), R.drawable.shape_rounded_corners));
        dialog.getWindow().getDecorView().setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.bottomSheetColor));
        dialog.setContentView(view);

        // POSITIVE BUTTON
        view.findViewById(R.id.dialog_positive_button)
                .setOnClickListener(view1 -> {
                    RefreshData();
                    dialog.dismiss();
                });

        // NEGATIVE BUTTON
        view.findViewById(R.id.dialog_negative_button)
                .setOnClickListener(view12 -> {
                    dialog.cancel();
                    swipeRefreshLayout.setRefreshing(false);
                });

        // SHOW DIALOG WITH CUSTOM ANIMATION
        Objects.requireNonNull(dialog.getWindow()).getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void RefreshData(){

        Handler handler = new Handler(Looper.getMainLooper());

        if (HasNetwork(requireContext())) {

            Executors.newSingleThreadExecutor().execute(() -> {

                // BACKGROUND THREAD WORK
                if (HasInternet()) {

                    try {
                        jsonData = URLResponse();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // UI THREAD WORK
                    handler.post(() -> {
                        try {
                            mainActivity.dataList.clear();
                            mainActivity.dataList = PopulateDataList(jsonData);
                            plexusDataList = mainActivity.dataList;
                            plexusDataItemAdapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false);
                            getParentFragmentManager().beginTransaction().detach(mainActivity.fragment).commit();
                            getParentFragmentManager().beginTransaction().attach(mainActivity.fragment).commit();
                        }

                        catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    });
                }
                else {
                    handler.post(this::NoNetworkDialog);
                }
            });
        }

        else {
            NoNetworkDialog();
        }

    }

}
