package tech.techlore.plexus.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import tech.techlore.plexus.R;

public class SearchActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ((MaterialToolbar)findViewById(R.id.toolbar_search)).setNavigationOnClickListener(view ->
                onBackPressed());

        //onNewIntent(getIntent());

        // SEARCH BAR
        /*SearchView searchView = findViewById(R.id.searchView);
        searchView.requestFocus();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                rAdapter.getFilter().filter(s);
                return false;
            }
        });*/
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search your data somehow
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SearchActivity.this, MainActivity.class));
        finish();
        overridePendingTransition(0, R.anim.fade_out_slide_to_bottom);
    }
}
