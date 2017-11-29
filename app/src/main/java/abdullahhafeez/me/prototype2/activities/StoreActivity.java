package abdullahhafeez.me.prototype2.activities;

import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import abdullahhafeez.me.prototype2.R;
import abdullahhafeez.me.prototype2.data.StoreOverlay;
import abdullahhafeez.me.prototype2.adapters.StoreOverlayAdapter;

public class StoreActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private StoreOverlayAdapter storeOverlayAdapter;


    private ArrayList<StoreOverlay> storeOverlayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        recyclerView = findViewById(R.id.recycler_view);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        storeOverlayList = new ArrayList<>();

        loadData();

        storeOverlayAdapter = new StoreOverlayAdapter(StoreActivity.this, storeOverlayList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(storeOverlayAdapter);


    }


    private void loadData() {

        StoreOverlay storeOverlay = new StoreOverlay(R.drawable.blue_mustache, "Blue Mustache");
        storeOverlayList.add(storeOverlay);
        storeOverlay = new StoreOverlay(R.drawable.curly_mustache, "Curly Mustache");
        storeOverlayList.add(storeOverlay);
        storeOverlay = new StoreOverlay(R.drawable.blue_mustache, "Simple Mustache");
        storeOverlayList.add(storeOverlay);
        storeOverlay = new StoreOverlay(R.drawable.curly_mustache, "Other Curly Mustache");
        storeOverlayList.add(storeOverlay);
        storeOverlay = new StoreOverlay(R.drawable.simple_moustache, "OtherSimple Mustache");
        storeOverlayList.add(storeOverlay);
        storeOverlay = new StoreOverlay(R.drawable.blue_mustache, "New Curly Mustache");
        storeOverlayList.add(storeOverlay);
        storeOverlay = new StoreOverlay(R.drawable.simple_moustache, "New Simple Mustache");
        storeOverlayList.add(storeOverlay);
        storeOverlay = new StoreOverlay(R.drawable.curly_mustache, "Latest Curly Mustache");
        storeOverlayList.add(storeOverlay);
        storeOverlay = new StoreOverlay(R.drawable.blue_mustache, "Latest Simple Mustache");
        storeOverlayList.add(storeOverlay);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.overlay_search_menu, menu);

        MenuItem search = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);
        search(searchView);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    private void search(SearchView searchView) {

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                storeOverlayAdapter.getFilter().filter(newText);
                return true;
            }
        });
    }


}
