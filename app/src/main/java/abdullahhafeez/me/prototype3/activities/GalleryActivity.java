package abdullahhafeez.me.prototype3.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import abdullahhafeez.me.prototype3.R;
import abdullahhafeez.me.prototype3.adapters.VideosAdapter;

public class GalleryActivity extends AppCompatActivity {

    private File[] listVerizeFiles;
    private TextView no_video_textview;
    private int editItemPosition;

    private GridView gridview;

    private View.OnClickListener editItemSnackbarOnClick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        android.support.v7.app.ActionBar mActionBar = getSupportActionBar();
        mActionBar.show();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        no_video_textview = (TextView) findViewById(R.id.no_video_textview);

//        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent intent = new Intent();
//                intent.setAction(Intent.ACTION_VIEW);
//                intent.setDataAndType(Uri.fromFile(new File(listVerizeFiles[position].toString() + "/play.mp4")), "video/*");
//                startActivity(intent);
//            }
//        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        listVerizeFiles = getAllDirectoriesVerize();

        if (listVerizeFiles.length <= 0) {
            no_video_textview.setVisibility(View.VISIBLE);
        }
        else {
            no_video_textview.setVisibility(View.INVISIBLE);
        }

        gridview = (GridView) findViewById(R.id.videoGridView);
        gridview.setAdapter(new VideosAdapter(this, listVerizeFiles));

        gridview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                editItemPosition = position;

                Snackbar.make(findViewById(android.R.id.content), "Edit this video", Snackbar.LENGTH_LONG)
                        .setAction("Edit", editItemSnackbarOnClick)
                        .show();


                return true;
            }
        });

        editItemSnackbarOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String videoPath = listVerizeFiles[editItemPosition].toString() + "/" + "play.mp4";

                File file = new File(listVerizeFiles[editItemPosition].toString(), MainActivity.verize_pics_original);

                if (file.exists()) {

                    Intent intent = new Intent(GalleryActivity.this, VideoEditActivity.class);
                    intent.putExtra("currentDirectory", listVerizeFiles[editItemPosition].toString());
                    startActivity(intent);

                }
                else {
                    Toast.makeText(GalleryActivity.this, "This item cannot be further edited", Toast.LENGTH_SHORT).show();
                }


            }
        };

    }

    private File[] getAllDirectoriesVerize() {
        File f = new File(Environment.getExternalStorageDirectory(), "Verize");

        return f.listFiles();

    }

}
