package abdullahhafeez.me.prototype2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import org.jcodec.api.SequenceEncoder;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Rational;
import org.jcodec.common.tools.MainUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission.CAMERA;
import static org.jcodec.scale.BitmapUtil.fromBitmap;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "FaceTracker";

    private CameraSource mCameraSource = null;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;
    int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    int KEEP_ALIVE_TIME = 1;
    TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    private int facing = 1;

    private static final float FACE_POSITION_RADIUS = 5.0f;

    private static boolean makeVideoFlag = false;
    private static boolean videoStatus = false;
    private String verize_folder_main = "Verize";
    private String verize_pics_original = "Original Pics";
    private String verize_folder_edited = "Edited Pics";
    String currentVideoFolderName;

    private int frameCounter = 0;
    private static int imageCounter = 0;
    List<Future<?>> futures;
    ExecutorService exec;
    private int video_id = 1;

    private NotificationManager mNotifyManager;
    private Builder notificationBuilder;
    Paint myRectPaint;

    PointF leftEye = null;
    PointF rightEye = null;
    PointF leftMouth = null;
    PointF rightMouth = null;
    PointF noseBase = null;
    private final int REQUEST_PERMISSION_CODE = 84;
    double degree;
    Button captureButton;

    String[] permissions = {CAMERA, WRITE_EXTERNAL_STORAGE};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myRectPaint = new Paint();
        myRectPaint.setStrokeWidth(5);
        myRectPaint.setColor(Color.RED);
        myRectPaint.setStyle(Paint.Style.STROKE);
        captureButton = (Button) findViewById(R.id.camera_capture_but);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);


        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);

        futures = new ArrayList<Future<?>>();
        exec = Executors.newFixedThreadPool(10);


        int cameraPermissionCheck = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int writePermissionCheck = ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);

        if (cameraPermissionCheck == PackageManager.PERMISSION_GRANTED && writePermissionCheck == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(facing);
            makeVerizeFolder();
        }
        else {
            ActivityCompat.requestPermissions(MainActivity.this, permissions, REQUEST_PERMISSION_CODE);
        }

//
//
//        // Check for the camera permission before accessing the camera.  If the
//        // permission is not granted yet, request permission.
//        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
//        if (rc == PackageManager.PERMISSION_GRANTED) {
//            makeVerizeFolder();
//        } else {
//            requestCameraPermission();
//        }
//
//        ActivityCompat.requestPermissions(MainActivity.this, permissions, REQUEST_PERMISSION_CODE);
//
////        int permissionCheck = ContextCompat.checkSelfPermission(this,
////                Manifest.permission.WRITE_EXTERNAL_STORAGE);
////        if(permissionCheck == PackageManager.PERMISSION_GRANTED){
////
////
////
////        }
////        else{
////            Log.e("Asdasdasda", "entering");
////
////            // Should we show an explanation?
////            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
////                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
////
////
////                // Show an explanation to the user *asynchronously* -- don't block
////                // this thread waiting for the user's response! After the user
////                // sees the explanation, try again to request the permission.
////                Toast.makeText(this, "why it is nesssagary permission", Toast.LENGTH_SHORT).show();
////
////                ActivityCompat.requestPermissions(this,
////                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
////                        WRITE_EXTERNAL_REQUEST);
////
////            } else {
////
////                // No explanation needed, we can request the permission.
////                Toast.makeText(this, "REquest permission", Toast.LENGTH_SHORT).show();
////
////                ActivityCompat.requestPermissions(this,
////                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
////                        WRITE_EXTERNAL_REQUEST);
////
////                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
////                // app-defined int constant. The callback method gets the
////                // result of the request.
////            }
////
////
////        }
//        rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        if (rc == PackageManager.PERMISSION_GRANTED) {
//            createCameraSource(facing);
//        } else {
//            ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                        WRITE_EXTERNAL_REQUEST);
//        }
//


    }

    public void makeVerizeFolder() {
        File f = new File(Environment.getExternalStorageDirectory(), verize_folder_main);
        if (!f.exists()) {
            f.mkdirs();
            Log.e("folder","verize created");
        }
    }

    public void logoOnClick(View view) {

        startActivity(new Intent(MainActivity.this, GalleryActivity.class));

    }

    public void cameraCaptureOnClick(View view) {

        if (makeVideoFlag) {
            makeVideoFlag = false;

            Toast.makeText(getApplicationContext(), "status Video stopped", Toast.LENGTH_LONG).show();
            // A) Await all runnables to be done (blocking)

            for (Future<?> future : futures)
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }


            Snackbar.make(findViewById(android.R.id.content), "Video is processing", Snackbar.LENGTH_LONG)
                    .setAction("OK", null)
                    .setActionTextColor(Color.RED)
                    .show();

            mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationBuilder = new NotificationCompat.Builder(MainActivity.this);
            notificationBuilder.setContentTitle("Verize-Video")
                    .setContentText("Video is gathering")
                    .setSmallIcon(R.drawable.download);


            makeVideo();


        } else {
            makeVideoFlag = true;
            imageCounter = -1;
            frameCounter = 0;

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HH_mm_ss");
            String currentTimeStamp = dateFormat.format(new Date());

            currentVideoFolderName = "video_" + currentTimeStamp;

            //making verize pics folder
            File videoFile = new File(Environment.getExternalStorageDirectory() + "/" + verize_folder_main, currentVideoFolderName);
            if (!videoFile.exists()) {
                videoFile.mkdirs();
                Log.e("VideoFolder","Video Folder created");
            }


            CameraSource.PreviewFrameInterface previewFrameInterface = new CameraSource.PreviewFrameInterface(){

                @Override
                public void getPreviewFrame(byte[] imageBytes, Camera camera) {


                    if (makeVideoFlag) {
                        if (frameCounter%5 == 0) {
                            imageCounter++;
                            TaskData data = new TaskData(camera, imageBytes, imageCounter);

                            //executeAsyncTask(new ProcessImageTask(), data);

                            Runnable worker = new ProcessImageTask(data);
                            Future<?> f = exec.submit(worker);
                            futures.add(f);
                        }
                        frameCounter++;

                    }


                }
            };

            mCameraSource.setPreviewInterface(previewFrameInterface);
        }

    }

//    static public <T> void executeAsyncTask(AsyncTask<T, ?, ?> task,T... params) {
//
//        Log.d("MULTITHREADING", "ASYNCTASK EXECUTED");
//            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
//
//
//    }


    private void makeVideo() {

        new VideoTask().execute();

    }

    private class VideoTask extends AsyncTask<Void, Integer, Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            notificationBuilder.setProgress(0, 0, true);
            mNotifyManager.notify(video_id, notificationBuilder.build());
        }

        @Override
        protected Void doInBackground(Void... params) {
            try{

                String videoPath = "/sdcard/" + verize_folder_main + "/" + currentVideoFolderName + "/" + "play.mp4";
                String picsPath = "/" + verize_folder_main + "/" + currentVideoFolderName + "/" + verize_folder_edited;

                FileChannelWrapper out =   NIOUtils.writableChannel(MainUtils.tildeExpand(videoPath));
                SequenceEncoder encoder = new SequenceEncoder(out, new Rational(5,1));
                for(int i =0; i< imageCounter ; i++){

                    Log.e("making video", Integer.toString(i));
                    File file = new File(Environment.getExternalStorageDirectory().getPath() + picsPath + "/picture" + i + ".jpg");
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    org.jcodec.common.model.Picture picture = fromBitmap(bitmap);
                    encoder.encodeNativeFrame(picture);

                }
                encoder.finish();
            }catch (Exception e){}

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            notificationBuilder.setContentText("Video Saved");
            // Removes the progress bar
            notificationBuilder.setProgress(0, 0, false);
            mNotifyManager.notify(video_id, notificationBuilder.build());

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // Update progress
            //notificationBuilder.setProgress(100, values[0], false);
            mNotifyManager.notify(video_id, notificationBuilder.build());
            super.onProgressUpdate(values);
        }

    }

    public void storeOnClick(View view) {

        startActivity(new Intent(this, StoreActivity.class));

    }


    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource(int facing) {

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setTrackingEnabled(true)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }

        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(facing)
                .setRequestedFps(1.0f)
                .build();

    }

//    private class ProcessImageTask extends AsyncTask<TaskData, Void, Void>{
//
//        @Override
//        protected Void doInBackground(TaskData... data) {
//            Camera.Parameters parameters = data[0].getCamera().getParameters();
//            Camera.Size size = parameters.getPreviewSize();
//            YuvImage image = new YuvImage(data[0].getImageData(), ImageFormat.NV21,
//                    size.width, size.height, null);
//            File file = new File(Environment.getExternalStorageDirectory()
//                    .getPath() + "/out " + data[0].getCounterName() + ".jpg");
//
//            FileOutputStream filecon = null;
//            try {
//                filecon = new FileOutputStream(file);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//
//            image.compressToJpeg(
//                    new Rect(0, 0, image.getWidth(), image.getHeight()), 90,
//                    filecon);
//
//            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
//            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), bmOptions);
//
//
//            Matrix matrix = new Matrix();
//            matrix.postRotate(-90);
//            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
//
//            if (file.exists()) file.delete();
//            try {
//                FileOutputStream out = new FileOutputStream(file);
//                rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            Paint myRectPaint = new Paint();
//            myRectPaint.setStrokeWidth(5);
//            myRectPaint.setColor(Color.RED);
//            myRectPaint.setStyle(Paint.Style.STROKE);
//
//            Bitmap tempBitmap = Bitmap.createBitmap(rotatedBitmap.getWidth(), rotatedBitmap.getHeight(), Bitmap.Config.RGB_565);
//            Canvas tempCanvas = new Canvas(tempBitmap);
//            tempCanvas.drawBitmap(rotatedBitmap, 0, 0, null);
//
//
//            FaceDetector faceDetector = new FaceDetector.Builder(getApplicationContext())
//                    .setTrackingEnabled(false)
//                    .setLandmarkType(FaceDetector.ALL_LANDMARKS)
//                    .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
//                    .build();
//
//            if (!faceDetector.isOperational()) {
//                new AlertDialog.Builder(getApplicationContext()).setMessage("Could not set up the face detector!").show();
//
//            }
//
//            Frame frame1 = new Frame.Builder().setBitmap(tempBitmap).build();
//            SparseArray<Face> faces = faceDetector.detect(frame1);
//
//            for (int i = 0; i < faces.size(); i++) {
//
//
//                List<Landmark> allLands = null;
//
//                try {
//                    allLands = faces.valueAt(i).getLandmarks();
//                } catch (Exception e) {
//                    Log.e(TAG, "onPreviewFrame: " + e.getMessage());
//                }
//
//
//                for (Landmark a : allLands) {
//
//                    PointF point = a.getPosition();
//
//                    double viewWidth = tempCanvas.getWidth();
//                    double viewHeight = tempCanvas.getHeight();
//                    double imageWidth = 480;
//                    double imageHeight = 640;
//                    double scale = Math.min(viewWidth / imageWidth, viewHeight / imageHeight);
//
//                    tempCanvas.drawCircle((int) (point.x * scale), (int) (point.y * scale), FACE_POSITION_RADIUS, myRectPaint);
//
//                }
//
//            }
//
//            if (file.exists()) file.delete();
//
//            try {
//                FileOutputStream out = new FileOutputStream(file);
//                tempBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
//                out.flush();
//                out.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//
//
//            return null;
//        }
//    }


    private class ProcessImageTask implements Runnable{

        private  TaskData data;
        ProcessImageTask( TaskData data){
            this.data = data;
        }

        @Override
        public void run() {
            Camera.Parameters parameters = data.getCamera().getParameters();
            Camera.Size size = parameters.getPreviewSize();
            YuvImage image = new YuvImage(data.getImageData(), ImageFormat.NV21,
                    size.width, size.height, null);


            //making verize pics folder
            File verizeFile = new File(Environment.getExternalStorageDirectory() + "/" + verize_folder_main + "/" + currentVideoFolderName, verize_folder_edited);
            if (!verizeFile.exists()) {
                verizeFile.mkdirs();
                Log.e("VideoFolder","Video Folder created");
            }

            String picsPath = "/" + verize_folder_main + "/" + currentVideoFolderName + "/" + verize_folder_edited;

            File file = new File(Environment.getExternalStorageDirectory()
                    .getPath() + picsPath + "/picture" + data.getCounterName() + ".jpg");


            FileOutputStream filecon = null;
            try {
                filecon = new FileOutputStream(file);

            } catch (Exception e) {
                e.printStackTrace();
            }


            image.compressToJpeg(
                    new Rect(0, 0, image.getWidth(), image.getHeight()), 90,
                    filecon);

            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), bmOptions);


            Matrix matrix = new Matrix();
            matrix.postRotate(-90);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);



            Bitmap tempBitmap = Bitmap.createBitmap(rotatedBitmap.getWidth(), rotatedBitmap.getHeight(), Bitmap.Config.RGB_565);
            Canvas tempCanvas = new Canvas(tempBitmap);
            tempCanvas.drawBitmap(rotatedBitmap, 0, 0, null);


            FaceDetector faceDetector = new FaceDetector.Builder(getApplicationContext())
                    .setTrackingEnabled(false)
                    .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                    .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                    .build();

            if (!faceDetector.isOperational()) {
                new AlertDialog.Builder(getApplicationContext()).setMessage("Could not set up the face detector!").show();

            }

            Frame frame1 = new Frame.Builder().setBitmap(tempBitmap).build();
            SparseArray<Face> faces = faceDetector.detect(frame1);

            for (int i = 0; i < faces.size(); i++) {


                List<Landmark> allLands = null;

                try {
                    allLands = faces.valueAt(i).getLandmarks();
                } catch (Exception e) {
                    Log.e(TAG, "onPreviewFrame: " + e.getMessage());
                }

               Bitmap overlay = BitmapFactory.decodeResource(getResources(), R.drawable.moustache);


                for (Landmark a : allLands) {

                    double viewWidth = tempCanvas.getWidth();
                    double viewHeight = tempCanvas.getHeight();
                    double imageWidth = 480;
                    double imageHeight = 640;
                    double scale = Math.min(viewWidth / imageWidth, viewHeight / imageHeight);


                    if (a.getType() == Landmark.LEFT_MOUTH){
                        leftMouth = a.getPosition();
                    }
                    else if (a.getType() ==Landmark.NOSE_BASE){
                        noseBase = a.getPosition();
                    }else if (a.getType() == Landmark.RIGHT_MOUTH){
                        rightMouth = a.getPosition();
                    }

                    if(a.getType() == Landmark.LEFT_EYE)
                        leftEye = a.getPosition();
                    else if(a.getType() == Landmark.RIGHT_EYE)
                        rightEye = a.getPosition();





                }

//                Matrix matrix1 = new Matrix();
//
//                float x = translateX(currentface.getPosition().x + currentface.getWidth() / 2);
//                float y = translateY(currentface.getPosition().y + currentface.getHeight() / 2);
//
//                PointF Fpoints = currentface.getPosition();
//
//                // Draws a bounding box around the face.
//                float xOffset = scaleX(currentface.getWidth() / 2.5f);
//                float yOffset = scaleY(currentface.getHeight() / 2.5f);
//                float left = x - xOffset;
//                float top = y - yOffset;
//                float right = x + xOffset;
//                float bottom = y + yOffset;


                //matrix1.preScale(leftEye.x, leftEye.y, right, leftEye.y+200f);
                //matrix1.mapRect(new RectF((int) 50, (int) leftEye.y + 100, (int) right, (int) leftEye.y + 200));

//                float slope = (rightEye.y - leftEye.y)/ (rightEye.x - rightEye.x);
//
//
//                double degree =  Math.toDegrees(-Math.atan2(leftEye.y - rightEye.y, leftEye.x - rightEye.x));
//
//                matrix.setRotate((float)degree);
//                matrix1 = new Matrix();
//                matrix1.setTranslate(left,leftEye.y);
////
////                if (leftEye.y < rightEye.y) {
////                   matrix1.setTranslate(rightEye.x - left,rightEye.y);
////                }
////                else if (rightEye.y < leftEye.y) {
////                    matrix1.setTranslate(leftEye.x - left,rightEye.y);
////                }
//
//
//                matrix1.postScale((right - left)/420, 1f);
//                //matrix1.setScale(0.5f, 0.5f, 0.5f, 0.5f);
//                matrix.postConcat(matrix1);
//                //canvas.drawBitmap(bitmap, null, new Rect((int) left, (int) leftEye.y + 100, (int) right, (int) leftEye.y + 200), mBoxPaint);
//                tempCanvas.drawBitmap(overlay, matrix,null);


                try {
                    float scaleY;

                    float scaleX = (leftMouth.x - rightMouth.x)/180;
                    if(leftMouth.y < rightMouth.y) {
                        scaleY = (rightMouth.y - noseBase.y) / 90;
                        matrix.setTranslate(rightMouth.x - (rightMouth.y - noseBase.y) / 2, noseBase.y + (rightMouth.y - noseBase.y) / 2);
                    }
                    else {
                        scaleY= (leftMouth.y - noseBase.y)/90;
                        matrix.setTranslate(rightMouth.x - (rightMouth.y - noseBase.y) / 2,noseBase.y + (rightMouth.y - noseBase.y) / 4);

                    }
                    matrix.preScale(scaleX, scaleY);
                    degree =  Math.toDegrees(-Math.atan2(leftMouth.y - rightMouth.y, leftMouth.x - rightMouth.x));
                    matrix.preRotate((float)-degree);

                    tempCanvas.drawBitmap(overlay, matrix,null);
                }catch (Exception e){
                    Log.e(TAG, "Exception caught: value null in facial coordinates");
                }





            }

            if (file.exists()) file.delete();

            try {
                FileOutputStream out = new FileOutputStream(file);
                tempBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();

        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case REQUEST_PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.

                if (grantResults.length > 0) {

                    boolean cameraResult = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeResult = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (cameraResult && writeResult) {
                        createCameraSource(facing);
                        makeVerizeFolder();
                    }
                    else {
                        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

                        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Verize")
                                .setMessage(R.string.not_all_permission)
                                .setPositiveButton(R.string.ok, listener)
                                .show();
                    }


                }

//                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Log.d(TAG, "Camera permission granted - initialize the camera source");
//                    // we have permission, so create the camerasource
//                    createCameraSource(facing);
//                    return;
//                }else{
//
//
//                }
//
//                if (grantResults.length > 0
//                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
//                    captureButton.setClickable(true);
//                    // permission was granted, yay! Do the
//                    // contacts-related task you need to do.
//                    Toast.makeText(MainActivity.this,"Write permission GRANTED", Toast.LENGTH_SHORT).show();
//                    makeVerizeFolder();
//
//                } else {
//                        captureButton.setClickable(false);
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//
//                    Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
//                            " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
//
//                    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            finish();
//                        }
//                    };
//
//                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                    builder.setTitle("Face Tracker sample")
//                            .setMessage(R.string.no_camera_permission)
//                            .setPositiveButton(R.string.ok, listener)
//                            .show();
//                }
//                return;
            }

        }

    }


    //==============================================================================================
    // Camera Source Preview
    //==============================================================================================

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);

        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }

}
