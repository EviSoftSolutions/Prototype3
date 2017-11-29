package abdullahhafeez.me.prototype2.Services;

import android.app.IntentService;

import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.util.Log;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.DataSource;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AACTrackImpl;
import com.googlecode.mp4parser.authoring.tracks.h264.H264TrackImpl;

import org.jcodec.api.SequenceEncoder;
import org.jcodec.api.transcode.Transcoder;
import org.jcodec.audio.Audio;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Rational;
import org.jcodec.common.tools.MainUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;

import abdullahhafeez.me.prototype2.R;

import static org.jcodec.scale.BitmapUtil.fromBitmap;

/**
 * Created by Abdullah on 9/26/2017.
 */

public class VideoBuildService extends IntentService {

    private String videoPath;
    private String audioPath;
    private String generalPath;
    private String picsPath;
    private int imageCounter;

    private int video_id = 1;

    private NotificationManager notificationManager;
    private Builder notifyBuilder;

    public VideoBuildService(){
        super("VideoBuildService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notifyBuilder = new NotificationCompat.Builder(this);
        notifyBuilder.setContentTitle("Verize-Video")
                .setContentText("Video is gathering")
                .setSmallIcon(R.drawable.download);

        notifyBuilder.setProgress(0, 0, true);
        notifyBuilder.setAutoCancel(true);
        notificationManager.notify(video_id, notifyBuilder.build());

        videoPath = intent.getStringExtra("videoPath");
        audioPath = intent.getStringExtra("audioPath");
        generalPath = intent.getStringExtra("generalPath");

        picsPath = intent.getStringExtra("picsPath");
        imageCounter = intent.getIntExtra("imageCounter", 0);

        try{
            FileChannelWrapper out =   NIOUtils.writableChannel(MainUtils.tildeExpand(videoPath));
            SequenceEncoder encoder = new SequenceEncoder(out, new Rational(4,1));
            Log.e("counter",Integer.toString(imageCounter));
               imageCounter= (int)(imageCounter* 0.9);
            Log.e("counter",Integer.toString(imageCounter));

            for(int i =0; i< imageCounter ; i++){

                File file = new File(picsPath + "/picture" + i + ".jpg");
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                org.jcodec.common.model.Picture picture = fromBitmap(bitmap);
                encoder.encodeNativeFrame(picture);

            }
            encoder.finish();
        }catch (Exception e){}


        try {
            Log.e("path",videoPath);
            Log.e("path",audioPath);

            Track h264Track = null;


            DataSource dataSource = new FileDataSourceImpl(videoPath);

            Movie sor = MovieCreator.build(dataSource);

            List<Track> videoTracks = sor.getTracks();

            for (Track t : videoTracks
                    ) {
                if (t.getHandler().contains("vid")) {
                    h264Track = t;
                }
            }

            AACTrackImpl aacTrack = new AACTrackImpl(new FileDataSourceImpl(audioPath));
            Movie movie = new Movie();
            movie.addTrack(h264Track);
            movie.addTrack(aacTrack);

            Container mp4file = new DefaultMp4Builder().build(movie);
            FileChannel fc = new FileOutputStream(new File(generalPath.toString() + "/play.mp4")).getChannel();
            mp4file.writeContainer(fc);
            fc.close();

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            new File(videoPath).delete();
            new File(audioPath).delete();

        }


        File dir = new File(picsPath);

        if (dir.isDirectory())
        {
            String[] children = dir.list();

            for (int i = 0; i < children.length; i++)
            {
                new File(dir, children[i]).delete();
            }
        }

        dir.delete();

        notifyBuilder.setContentText("Video Saved");
        notifyBuilder.setProgress(0, 0, false);

        notificationManager.notify(video_id, notifyBuilder.build());


    }
}
