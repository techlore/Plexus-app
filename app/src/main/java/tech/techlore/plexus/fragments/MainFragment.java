package tech.techlore.plexus.fragments;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import tech.techlore.plexus.R;
import tech.techlore.plexus.activities.MainActivity;
import tech.techlore.plexus.adapters.AppItemAdapter;
import tech.techlore.plexus.models.App;

public class MainFragment extends Fragment {

    private OkHttpClient okHttpClient;
    private String jsonData;
    private RecyclerView recyclerView;
    private AppItemAdapter rAdapter;
    private List<App> appsList;
    private App app;
    private CountDownTimer delayTimer;

    public MainFragment() {
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
        return inflater.inflate(R.layout.fragment_main,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        recyclerView = view.findViewById(R.id.apps_recycler_view);
        appsList = new ArrayList<>();
        final MainActivity mainActivity = ((MainActivity) requireActivity());
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Handler handler = new Handler(Looper.getMainLooper());

        /*===========================================================================================*/

        executor.execute(() -> {

            // BACKGROUND THREAD WORK
            try {
                doInBackground();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // UI THREAD WORK
            handler.post(() -> {
                try {
                    populateList();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                rAdapter = new AppItemAdapter(appsList);
                recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
                recyclerView.setAdapter(rAdapter);

                // HANDLE CLICK EVENTS OF ITEMS
                rAdapter.setOnItemClickListener(position -> {

                    app = appsList.get(position);
                    mainActivity.AppDetails(app.name, app.packageName, app.version,
                                            app.dgNotes, app.mgNotes, app.dgRating, app.mgRating);

                });

            });
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

    private String URLRequest() throws IOException {
        Request request = new Request.Builder()
                .url("https://raw.githubusercontent.com/parveshnarwal/Plexus-Demo/main/test.json")
                .build();

        try (Response response = okHttpClient.newCall(request).execute())
        {
            return Objects.requireNonNull(response.body()).string();
        }

    }

    private void doInBackground() throws IOException {
        okHttpClient = new OkHttpClient();
        jsonData = URLRequest();
    }

    private void populateList() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        appsList = objectMapper.readValue(jsonData, new TypeReference<List<App>>(){});
    }

}
