package abdullahhafeez.me.prototype3.activities;

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

import abdullahhafeez.me.prototype3.R;
import abdullahhafeez.me.prototype3.data.StoreOverlay;
import abdullahhafeez.me.prototype3.adapters.StoreOverlayAdapter;

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

        toolbar = findViewById(R.id.toolbar);
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

      //  overlayPreviewList.add(R.drawable.blue_mustache_sample);
//        overlayPreviewList.add(R.drawable.rabbit_sample);
//        overlayPreviewList.add(R.drawable.cat_sample);
//        overlayPreviewList.add(R.drawable.curly_mustache_sample);
//        overlayPreviewList.add(R.drawable.flower_crown_sample);
//        overlayPreviewList.add(R.drawable.dog_sample);
//        overlayPreviewList.add(R.drawable.glass_joker_sample);
//        overlayPreviewList.add(R.drawable.simple_mustache_sample);
//        overlayPreviewList.add(R.drawable.joker_sample);
//        overlayPreviewList.add(R.drawable.anonymous_sample);
        StoreOverlay storeOverlay = new StoreOverlay(R.drawable.blue_mustache, "Blue Mustache");
        storeOverlayList.add(storeOverlay);
        storeOverlay = new StoreOverlay(R.drawable.rabbit_ears, "Bunny Rabbit");
        storeOverlayList.add(storeOverlay);
        storeOverlay = new StoreOverlay(R.drawable.cat_ears, "Shrieking Cat");
        storeOverlayList.add(storeOverlay);
        storeOverlay = new StoreOverlay(R.drawable.curly_mustache, "Curly Mustache");
        storeOverlayList.add(storeOverlay);
        storeOverlay = new StoreOverlay(R.drawable.flower_crown, "Flower Crown");
        storeOverlayList.add(storeOverlay);
        storeOverlay = new StoreOverlay(R.drawable.dog_ear, "Fun Dog");
        storeOverlayList.add(storeOverlay);
        storeOverlay = new StoreOverlay(R.drawable.glasses, "Groucho Glasses");
        storeOverlayList.add(storeOverlay);
        storeOverlay = new StoreOverlay(R.drawable.simple_moustache, "Simple Mustache");
        storeOverlayList.add(storeOverlay);
        storeOverlay = new StoreOverlay(R.drawable.joker_hat, "Joker's Attire");
        storeOverlayList.add(storeOverlay);
        storeOverlay = new StoreOverlay(R.drawable.anonymous, "Pakistani Anonymous");
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
