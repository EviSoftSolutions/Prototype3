package abdullahhafeez.me.prototype3.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.provider.MediaStore.Video.Thumbnails;

import java.io.File;

import abdullahhafeez.me.prototype3.R;

/**
 * Created by Abdullah on 6/19/2017.
 */

public class VideosAdapter extends BaseAdapter {

    private Context mContext;
    private File[] listVerizeFiles;
    private String videoPath;
    private Bitmap bmThumbnail;
    private ImageView playicon;
    private ImageView shareicon;
    private ImageView videoThumbnail;

    // Constructor
    public VideosAdapter(Context c, File[] listVerizeFiles) {
        mContext = c;
        this.listVerizeFiles = listVerizeFiles;
    }

    public int getCount() {
        return listVerizeFiles.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).
                    inflate(R.layout.gallery_item_layout, parent, false);;
        }

        videoPath = listVerizeFiles[position].toString() + "/play.mp4";

        bmThumbnail = ThumbnailUtils.createVideoThumbnail(videoPath, Thumbnails.MICRO_KIND);
         videoThumbnail = convertView.findViewById(R.id.galleryVideo);
        videoThumbnail.setImageBitmap(bmThumbnail);
         shareicon = convertView.findViewById(R.id.share_icon);
         playicon = convertView.findViewById(R.id.play_icon);
         shareicon.setOnClickListener(new View.OnClickListener() {
             int clickedPosition =position;
             @Override
             public void onClick(View view) {
                 Intent sendIntent = new Intent();
                 sendIntent.setAction(Intent.ACTION_SEND);
                 sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(listVerizeFiles[clickedPosition].toString() + "/play.mp4")));
                 sendIntent.setType("video/*");
                 mContext.startActivity(Intent.createChooser(sendIntent, "Share Your Video Using"));
             }
         });

         playicon.setOnClickListener(new View.OnClickListener() {
             int clickedPosition = position;
             @Override
             public void onClick(View view) {
                 Intent intent = new Intent();
                 intent.setAction(Intent.ACTION_VIEW);
                 intent.setDataAndType(Uri.fromFile(new File(listVerizeFiles[clickedPosition].toString() + "/play.mp4")), "video/*");
                 mContext.startActivity(intent);
             }
         });

        return convertView;
    }


}
