package abdullahhafeez.me.prototype3.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

import abdullahhafeez.me.prototype3.adapters.MainOverlayRecyclerViewAdapter;
import abdullahhafeez.me.prototype3.R;
import abdullahhafeez.me.prototype3.others.MyVideoReceiver;
import abdullahhafeez.me.prototype3.others.RecyclerTouchListener;
import abdullahhafeez.me.prototype3.Services.ImageEditService;
import de.mrapp.android.dialog.ProgressDialog;

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

    public static MyVideoReceiver receiverEdit;

    ProgressDialog.Builder dialogBuilder;
    ProgressDialog dialog;


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

        mainOverlayRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mainOverlayRecyclerView, new RecyclerTouchListener.ClickListener() {
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
        overlayPreviewList.add(R.drawable.rabbit_sample);
        overlayPreviewList.add(R.drawable.cat_sample);
        overlayPreviewList.add(R.drawable.curly_mustache_sample);
        overlayPreviewList.add(R.drawable.flower_crown_sample);
        overlayPreviewList.add(R.drawable.dog_sample);
        overlayPreviewList.add(R.drawable.glass_joker_sample);
        overlayPreviewList.add(R.drawable.simple_mustache_sample);
        overlayPreviewList.add(R.drawable.joker_sample);
        overlayPreviewList.add(R.drawable.anonymous_sample);
    }
    private void fetchOverlays() {
        overlayList = new ArrayList<>();
        overlayList.add(R.drawable.blue_mustache);
        overlayList.add(R.drawable.rabbit_ears);
        overlayList.add(R.drawable.cat_ears);
        overlayList.add(R.drawable.curly_mustache);
        overlayList.add(R.drawable.flower_crown);
        overlayList.add(R.drawable.dog_ear);
        overlayList.add(R.drawable.glasses);
        overlayList.add(R.drawable.simple_moustache);
        overlayList.add(R.drawable.joker_hat);
        overlayList.add(R.drawable.anonymous);
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.tickeditbutt){
            setupServiceReceiver();

            Intent intent = new Intent(this, ImageEditService.class);
            intent.putExtra("currentdirectory", currentDirectory);
            intent.putExtra("selectedoverlay", selectedOverlay);
            intent.putExtra("receiver", receiverEdit);

            dialogBuilder = new ProgressDialog.Builder(this);
        //dialogBuilder.setTitle("Uploading Files");
            dialogBuilder.setMessage("Verize Video is Editing");
            dialogBuilder.setProgressBarPosition(ProgressDialog.ProgressBarPosition.LEFT);
            dialog = dialogBuilder.create();
            dialog.show();
            startService(intent);
        }
        else if(v.getId() == R.id.canceleditbutt){

            selectedOverlay =-1;
            finish();
        }

    }

    public void setupServiceReceiver() {
        receiverEdit = new MyVideoReceiver(new Handler());
        // This is where we specify what happens when data is received from the service
        receiverEdit.setReceiver(new MyVideoReceiver.Receiver() {
            @Override
            public void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == RESULT_OK) {
                    Boolean resultValue = resultData.getBoolean("resultValue");

                    if(resultValue){
                        dialog.hide();
                        Toast.makeText(VideoEditActivity.this, "Verize Video is Saved", Toast.LENGTH_SHORT).show();

                    }
                    else{
                        dialog.hide();
                        Toast.makeText(VideoEditActivity.this, "Video Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

}
