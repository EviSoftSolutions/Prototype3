package abdullahhafeez.me.prototype2.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
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

import abdullahhafeez.me.prototype2.R;

/**
 * Created by Abdullah on 6/19/2017.
 */

public class VideosAdapter extends BaseAdapter {

    private Context mContext;
    private File[] listVerizeFiles;
    private String videoPath;
    private Bitmap bmThumbnail;

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
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).
                    inflate(R.layout.gallery_item_layout, parent, false);;
        }

        videoPath = listVerizeFiles[position].toString() + "/play.mp4";

        bmThumbnail = ThumbnailUtils.createVideoThumbnail(videoPath, Thumbnails.MICRO_KIND);
        ImageView galleryVideo = (ImageView) convertView.findViewById(R.id.galleryVideo);
        galleryVideo.setImageBitmap(bmThumbnail);

        return convertView;
    }


}
