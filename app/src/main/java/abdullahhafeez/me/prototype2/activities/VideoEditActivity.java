package abdullahhafeez.me.prototype2.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

import abdullahhafeez.me.prototype2.adapters.MainOverlayRecyclerViewAdapter;
import abdullahhafeez.me.prototype2.R;
import abdullahhafeez.me.prototype2.others.RecyclerTouchListner;
import abdullahhafeez.me.prototype2.Services.ImageEditService;

public class VideoEditActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "VideoEditActivity";

    private String currentDirectory;

    private Bitmap thumbnailBitmap;
    private ImageView thumbnailImage;
    private int selectedOverlay;

    private Button tickeditbutt;
    private Button canceleditbutt;


    private RecyclerView mainOverlayRecyclerView;
    private ArrayList<Integer> overlayPreviewList;
    private ArrayList<Integer> overlayList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_edit);
        android.support.v7.app.ActionBar mActionBar = getSupportActionBar();
        mActionBar.show();
        currentDirectory = getIntent().getStringExtra("currentDirectory");

        thumbnailBitmap = BitmapFactory.decodeFile(currentDirectory + "/" + MainActivity.verize_pics_original + "/" + "picture0.jpg");

        thumbnailImage = (ImageView) findViewById(R.id.editthumnailImage);

        tickeditbutt = (Button) findViewById(R.id.tickeditbutt);
        canceleditbutt = (Button) findViewById(R.id.canceleditbutt);

        tickeditbutt.setOnClickListener(this);
        canceleditbutt.setOnClickListener(this);


        thumbnailImage.setImageBitmap(thumbnailBitmap);

        //Setting main overlay recyclerview
        mainOverlayRecyclerView = (RecyclerView) findViewById(R.id.mainOverlayRecyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mainOverlayRecyclerView.setLayoutManager(mLayoutManager);

        fetchPreviewOverlays();
        fetchOverlays();

        MainOverlayRecyclerViewAdapter mainOverlayRecyclerViewAdapter = new MainOverlayRecyclerViewAdapter(this, overlayPreviewList);
        mainOverlayRecyclerView.setAdapter(mainOverlayRecyclerViewAdapter);

        mainOverlayRecyclerView.addOnItemTouchListener(new RecyclerTouchListner(getApplicationContext(), mainOverlayRecyclerView, new RecyclerTouchListner.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                selectedOverlay = overlayList.get(position);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        /////////////




    }

    private void fetchPreviewOverlays() {
        overlayPreviewList = new ArrayList<>();
        overlayPreviewList.add(R.drawable.blue_mustache_sample);
        overlayPreviewList.add(R.drawable.curly_mustache_sample);
        overlayPreviewList.add(R.drawable.simple_mustache_sample);
        overlayPreviewList.add(R.drawable.blue_mustache_sample);
        overlayPreviewList.add(R.drawable.curly_mustache_sample);
        overlayPreviewList.add(R.drawable.simple_mustache_sample);
        overlayPreviewList.add(R.drawable.blue_mustache_sample);
        overlayPreviewList.add(R.drawable.curly_mustache_sample);
        overlayPreviewList.add(R.drawable.simple_mustache_sample);
    }
    private void fetchOverlays() {
        overlayList = new ArrayList<>();
        overlayList.add(R.drawable.blue_mustache);
        overlayList.add(R.drawable.curly_mustache);
        overlayList.add(R.drawable.simple_moustache);
        overlayList.add(R.drawable.blue_mustache);
        overlayList.add(R.drawable.curly_mustache);
        overlayList.add(R.drawable.simple_moustache);
        overlayList.add(R.drawable.blue_mustache);
        overlayList.add(R.drawable.curly_mustache);
        overlayList.add(R.drawable.simple_moustache);
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.tickeditbutt){

            Toast.makeText(VideoEditActivity.this, "Video is Editing", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, ImageEditService.class);
            intent.putExtra("currentdirectory", currentDirectory);
            intent.putExtra("selectedoverlay", selectedOverlay);
            startService(intent);
        }
        else if(v.getId() == R.id.canceleditbutt){

            selectedOverlay =-1;
            finish();
        }



    }
}
