package abdullahhafeez.me.prototype2.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;

import android.content.ContentValues;
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
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


import abdullahhafeez.me.prototype2.adapters.MainOverlayRecyclerViewAdapter;
import abdullahhafeez.me.prototype2.R;
import abdullahhafeez.me.prototype2.others.RecyclerTouchListner;
import abdullahhafeez.me.prototype2.Services.VideoBuildService;
import abdullahhafeez.me.prototype2.data.TaskData;
import abdullahhafeez.me.prototype2.vision.CameraSource;
import abdullahhafeez.me.prototype2.vision.CameraSourcePreview;
import abdullahhafeez.me.prototype2.vision.FaceGraphic;
import abdullahhafeez.me.prototype2.vision.GraphicOverlay;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission.CAMERA;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private MediaRecorder mediaRecorder;

    private CameraSource mCameraSource = null;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    //Variable for checking google api availabilty in creating camera source
    private static final int RC_HANDLE_GMS = 9001;

    //variable for setting camera facing
    private int facing = 1;

    //variables for making video
    private static boolean makeVideoFlag = false;
    public static String verize_folder_main = "Verize";
    public static String verize_pics_original = "Original Pics";
    public static String verize_folder_edited = "Edited Pics";
    private int frameCounter = 0;
    private static int imageCounter = 0;

    //variables for threads in making video
    private List<Future<?>> futures;
    private ExecutorService exec;

    private Paint myRectPaint;
    private PointF leftEye = null;
    private PointF rightEye = null;
    private PointF leftMouth = null;
    private PointF rightMouth = null;
    private PointF noseBase = null;
    private double degree;
    private Matrix editMatrix;

    private String[] permissions = {CAMERA, WRITE_EXTERNAL_STORAGE, RECORD_AUDIO};
    private final int REQUEST_PERMISSION_CODE = 84;


    private File videoPath;
    private File picsPath;
    private File audioPath;
    private String currentVideoFolderName;

    public static Bitmap overlay;
    private boolean futureEditFlag;

    private RecyclerView mainOverlayRecyclerView;
    private ArrayList<Integer> overlayPreviewList;
    private ArrayList<Integer> overlayList;


    private NavigationView navigationView;
    private DrawerLayout drawer;
    private View navHeader;
    public static int currentDrawableOverlay = 0;


    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;

    public final String mypreference = "mypref";
    Intent buildServiceIntent;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        buildServiceIntent = new Intent(MainActivity.this, VideoBuildService.class);
        View decorView = getWindow().getDecorView();
          // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };


        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.getBackground().setAlpha(122);

        navigationView.setNavigationItemSelectedListener(this);

        navHeader = navigationView.getHeaderView(0);

        ////
        ////
            ////Section for making header layout////
        ((TextView)navHeader.findViewById(R.id.name)).setText("Your Name");
        ((TextView)navHeader.findViewById(R.id.email)).setText("Your Email");
        ////
        ////


        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);

        //Setting main overlay recyclerview
        mainOverlayRecyclerView = (RecyclerView) findViewById(R.id.mainOverlayRecyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mainOverlayRecyclerView.setLayoutManager(mLayoutManager);

        fetchOverlays();
        fetchPreviewOverlays();

        MainOverlayRecyclerViewAdapter mainOverlayRecyclerViewAdapter = new MainOverlayRecyclerViewAdapter(this, overlayPreviewList);
        mainOverlayRecyclerView.setAdapter(mainOverlayRecyclerViewAdapter);

        mainOverlayRecyclerView.addOnItemTouchListener(new RecyclerTouchListner(getApplicationContext(), mainOverlayRecyclerView, new RecyclerTouchListner.ClickListener() {
            @Override
            public void onClick(View view, int position) {

                    overlay = BitmapFactory.decodeResource(getResources(), overlayList.get(position));
                currentDrawableOverlay = overlayList.get(position);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        /////////////

        futures = new ArrayList<Future<?>>();
        exec = Executors.newFixedThreadPool(10);

        myRectPaint = new Paint();
        myRectPaint.setStrokeWidth(5);
        myRectPaint.setColor(Color.RED);
        myRectPaint.setStyle(Paint.Style.STROKE);


        int cameraPermissionCheck = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int writePermissionCheck = ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);
        int micPermissionCheck = ActivityCompat.checkSelfPermission(this, RECORD_AUDIO);

        if (cameraPermissionCheck == PackageManager.PERMISSION_GRANTED &&
                writePermissionCheck == PackageManager.PERMISSION_GRANTED &&
                micPermissionCheck == PackageManager.PERMISSION_GRANTED ) {
            createCameraSource(facing);
            makeVerizeFolder();
        }
        else {
            ActivityCompat.requestPermissions(MainActivity.this, permissions, REQUEST_PERMISSION_CODE);
        }


    }

    private void initialMediaRecorder() {

        mediaRecorder = new MediaRecorder();
//        ContentValues values = new ContentValues(3);
//        values.put(MediaStore.MediaColumns.TITLE, fileName);

        try {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        } catch (Exception e){
           Log.e("INITAUDIO", e.getMessage());
        }
    }

    private void fetchPreviewOverlays() {
        overlayPreviewList = new ArrayList<>();
        overlayPreviewList.add(R.drawable.blue_mustache_sample);
        overlayPreviewList.add(R.drawable.curly_mustache_sample);
        overlayPreviewList.add(R.drawable.simple_mustache_sample);
        overlayPreviewList.add(R.drawable.blue_mustache_sample);
        overlayPreviewList.add(R.drawable.joker_sample);

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
        overlayList.add(R.drawable.joker_hat);

        overlayList.add(R.drawable.curly_mustache);
        overlayList.add(R.drawable.simple_moustache);
        overlayList.add(R.drawable.blue_mustache);
        overlayList.add(R.drawable.curly_mustache);
        overlayList.add(R.drawable.simple_moustache);
    }

    public void makeVerizeFolder() {
        File f = new File(Environment.getExternalStorageDirectory(), verize_folder_main);
        if (!f.exists()) {
            f.mkdirs();
            Log.e("folder","verize created");
        }
    }

    public void galleryOnClick(View view) {

        startActivity(new Intent(MainActivity.this, GalleryActivity.class));

    }

    public void logoOnClick(View view) {

        Toast.makeText(this, "Profile coming soon", Toast.LENGTH_SHORT).show();

    }

    public void dehazeOnClick(View view) {
        drawer.openDrawer(Gravity.LEFT);
    }

    public void cameraCaptureOnClick(View view) {

        if (makeVideoFlag) {
            Log.e("adasda", "inif");
            mCameraSource.setPreviewInterface(null);
            try{

                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
            }catch (Exception e){}


            makeVideoFlag = false;

            // A) Await all runnables to be done (blocking)

            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }

            Snackbar.make(findViewById(android.R.id.content), "Video is processing", Snackbar.LENGTH_LONG)
                    .setAction("OK", null)
                    .setActionTextColor(Color.RED)
                    .show();


            videoPath = new File(Environment.getExternalStorageDirectory() + "/" + verize_folder_main , currentVideoFolderName);
            picsPath = new File(Environment.getExternalStorageDirectory() + "/" + verize_folder_main + "/" + currentVideoFolderName, verize_folder_edited);


            buildServiceIntent.putExtra("picsPath", picsPath.toString());
            Log.e("adsada", picsPath.toString());
            buildServiceIntent.putExtra("videoPath", videoPath.toString() + "/play.h264");
            buildServiceIntent.putExtra("audioPath", audioPath.toString() + "/play.aac");
            buildServiceIntent.putExtra("generalPath", audioPath.toString());

            buildServiceIntent.putExtra("imageCounter", imageCounter);
            Snackbar.make(findViewById(android.R.id.content), "Video is processing", Snackbar.LENGTH_LONG)
                    .setAction("OK", null)
                    .setActionTextColor(Color.RED)
                    .show();
            startService(buildServiceIntent);

        } else {
            makeVideoFlag = true;
            initialMediaRecorder();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HH_mm_ss");
            String currentTimeStamp = dateFormat.format(new Date());

            currentVideoFolderName = "video_" + currentTimeStamp;



            //making verize video folder
            File videoFile = new File(Environment.getExternalStorageDirectory() + "/" + verize_folder_main, currentVideoFolderName);
            if (!videoFile.exists()) {
                videoFile.mkdirs();
                Log.e(TAG,"Verize video folder created");
            }

            audioPath = new File(Environment.getExternalStorageDirectory() + "/" + verize_folder_main , currentVideoFolderName);


            mediaRecorder.setOutputFile(audioPath.toString() + "/play.aac");
            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Audio", "some issues");
            }


            imageCounter = -1;
            frameCounter = 0;
            Toast.makeText(this, "Video Started", Toast.LENGTH_SHORT).show();


            CameraSource.PreviewFrameInterface previewFrameInterface = new CameraSource.PreviewFrameInterface(){

               TaskData data = new TaskData();
                Future<?> f;
                Runnable worker;
                @Override
                public void getPreviewFrame(byte[] imageBytes, Camera camera) {
                    Log.e("adasda", "inelsepreviewframe");

                    if (makeVideoFlag) {
                        if (frameCounter%3 == 0) {
                            Log.e("adasda", "inelsepreviewframeFinal");

                            Log.e("Incoming", Integer.toString(imageBytes.length));
                            imageCounter++;


                            data = new TaskData(camera, imageBytes, imageCounter);
                            //new code
//                            data.setCamera(camera);
//                            data.setImageData(imageBytes);
//                            data.setCounterName(imageCounter);
                              doItAll(worker,f, data);

                        }
                        frameCounter++;

                    }

                }
            };

            mCameraSource.setPreviewInterface(previewFrameInterface);
        }

    }

    public synchronized void doItAll(Runnable worker, Future<?> f, TaskData data){
        Log.e("Syncing", Integer.toString(data.getCounterName()));

        worker = new ProcessImageTask(data);
        f = exec.submit(worker);
        futures.add(f);
    }

    public void storeOnClick(View view) {

        startActivity(new Intent(this, StoreActivity.class));

    }

    public void videoChatOnClick(View view) {
        Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
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


    private class ProcessImageTask implements Runnable{

        private  TaskData data;
        private Camera camera;
        private Camera.Parameters parameters;
        private Camera.Size size;
        YuvImage image;

        ProcessImageTask( TaskData data){
            this.camera = data.getCamera();
            this.data = data;
            parameters = null;
            size = null;
            image = null;


        }

        @Override
        public void run() {
            parameters = camera.getParameters();
            size = parameters.getPreviewSize();
            image = new YuvImage(data.getImageData(), ImageFormat.NV21,
                    size.width, size.height, null);
            Log.e("adasda", "executing : " + data.getCounterName());



            //making verize pics edited folder
            File verizeEditFile = new File(Environment.getExternalStorageDirectory() + "/" + verize_folder_main + "/" + currentVideoFolderName, verize_folder_edited);
            if (!verizeEditFile.exists()) {
                verizeEditFile.mkdirs();
                Log.e(TAG,"Verize edited pics folder created");
            }


            String picsFolderPath = "/" + verize_folder_main + "/" + currentVideoFolderName + "/" + verize_folder_edited;

            File file = new File(Environment.getExternalStorageDirectory()
                    .getPath() + picsFolderPath + "/picture" + data.getCounterName() + ".jpg");


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


            if (futureEditFlag) {
                //making verize pics original folder
                File verizeOriginalFile = new File(Environment.getExternalStorageDirectory() + "/" + verize_folder_main + "/" + currentVideoFolderName, verize_pics_original);
                if (!verizeOriginalFile.exists()) {
                    verizeOriginalFile.mkdirs();
                    Log.e(TAG,"Verize original pics folder created");
                }

                File originalImage = new File(verizeOriginalFile.toString(), "/picture" + data.getCounterName() + ".jpg");

                try {
                    FileOutputStream out = new FileOutputStream(originalImage);
                    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }



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
                    Log.e(TAG, "Landmarks are null");
                }


                for (Landmark a : allLands) {

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

                try {

                    editMatrix = new Matrix();

                    float scaleY;
                    float scaleX = (leftMouth.x - rightMouth.x)/180;
                    if(leftMouth.y < rightMouth.y) {
                        scaleY = (rightMouth.y - noseBase.y) / 90;
                        editMatrix.setTranslate(rightMouth.x - (rightMouth.y - noseBase.y) / 2, noseBase.y + (rightMouth.y - noseBase.y) / 2);
                    }
                    else {
                        scaleY= (leftMouth.y - noseBase.y)/90;
                        editMatrix.setTranslate(rightMouth.x - (rightMouth.y - noseBase.y) / 2,noseBase.y + (rightMouth.y - noseBase.y) / 4);

                    }
                    editMatrix.preScale(scaleX, scaleY);
                    degree =  Math.toDegrees(-Math.atan2(leftMouth.y - rightMouth.y, leftMouth.x - rightMouth.x));
                    editMatrix.preRotate((float)-degree);

                    tempCanvas.drawBitmap(overlay, editMatrix,null);
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

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        futureEditFlag = sharedPreferences.getBoolean("edit_preference_key", false);

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
                    boolean micResult = grantResults[2] == PackageManager.PERMISSION_GRANTED;

                    if (cameraResult && writeResult && micResult) {
                        createCameraSource(facing);
                        makeVerizeFolder();
                    }
                    else {
                        Log.e(TAG, "Permissions not granted: results len = " + grantResults.length);

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
            Log.e("Update","Update");
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


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_logout) {
            auth.signOut();

            SharedPreferences sharedpreferences = getSharedPreferences(mypreference,
                    Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = sharedpreferences.edit();

            editor.putString("email", "");
            editor.putString("password", "");


            editor.commit();

            finish();


        } else if (id == R.id.nav_help) {
            Intent intent = new Intent(MainActivity.this, HelpActivity.class);
            startActivity(intent);

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}