package abdullahhafeez.me.prototype3.Services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import abdullahhafeez.me.prototype3.activities.MainActivity;
import abdullahhafeez.me.prototype3.R;
import abdullahhafeez.me.prototype3.activities.VideoEditActivity;
import abdullahhafeez.me.prototype3.others.MyVideoReceiver;

/**
 * Created by Abdullah on 10/3/2017.
 */

public class ImageEditService extends IntentService {

    private static final String TAG = "ImageEditService";

    private String currentdirectory;
    private int selectedoverlay;
    private int imageCounter;

    private Bitmap overlay;

    private int video_id = 1;

    private NotificationManager notificationManager;
    private NotificationCompat.Builder notifyBuilder;
    private File[] listImages;
    private File originalPicsFile;


    private File verizeEditFile;

    private Paint myRectPaint;
    private PointF leftEye = null;
    private PointF rightEye = null;
    private PointF leftMouth = null;
    private PointF rightMouth = null;
    private PointF noseBase = null;
    private double degree;
    private Matrix editMatrix;

    public ImageEditService() {
        super("ImageEditService");
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notifyBuilder = new NotificationCompat.Builder(this);
        notifyBuilder.setContentTitle("Verize-Edit-Frames")
                .setContentText("Images are Processing")
                .setSmallIcon(R.drawable.download);

        notifyBuilder.setProgress(0, 0, true);
        notifyBuilder.setAutoCancel(true);
        notificationManager.notify(video_id, notifyBuilder.build());

        currentdirectory = intent.getStringExtra("currentdirectory");
        selectedoverlay = intent.getIntExtra("selectedoverlay",-1);
        overlay = BitmapFactory.decodeResource(getResources(), selectedoverlay);

        originalPicsFile = new File(currentdirectory, MainActivity.verize_pics_original);

        //making verize pics edited folder
        verizeEditFile = new File(currentdirectory, MainActivity.verize_folder_edited);
        if (!verizeEditFile.exists()) {
            verizeEditFile.mkdirs();
            Log.e(TAG,"Verize edited pics folder created");
        }



        listImages = originalPicsFile.listFiles();

        imageCounter = listImages.length;

        for (int j = 0; j< imageCounter; j++) {

            Bitmap rotatedBitmap = BitmapFactory.decodeFile(currentdirectory + "/" + MainActivity.verize_pics_original + "/" + "picture" + j +".jpg");

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




                if(selectedoverlay == R.drawable.blue_mustache ||
                        selectedoverlay == R.drawable.curly_mustache ||
                        selectedoverlay == R.drawable.simple_moustache ) {

                    for (Landmark a : allLands) {

                        if (a.getType() == Landmark.LEFT_MOUTH) {
                            leftMouth = a.getPosition();
                        } else if (a.getType() == Landmark.NOSE_BASE) {
                            noseBase = a.getPosition();
                        } else if (a.getType() == Landmark.RIGHT_MOUTH) {
                            rightMouth = a.getPosition();
                        }

                    }

                    try {

                        editMatrix = new Matrix();

                        float scaleY;
                        float scaleX = (leftMouth.x - rightMouth.x) / 180;
                        if (leftMouth.y < rightMouth.y) {
                            scaleY = (rightMouth.y - noseBase.y) / 90;
                            editMatrix.setTranslate(rightMouth.x - (rightMouth.y - noseBase.y) / 2, noseBase.y + (rightMouth.y - noseBase.y) / 2);
                        } else {
                            scaleY = (leftMouth.y - noseBase.y) / 90;
                            editMatrix.setTranslate(rightMouth.x - (rightMouth.y - noseBase.y) / 2, noseBase.y + (rightMouth.y - noseBase.y) / 4);

                        }
                        editMatrix.preScale(scaleX, scaleY);
                        degree = Math.toDegrees(-Math.atan2(leftMouth.y - rightMouth.y, leftMouth.x - rightMouth.x));
                        editMatrix.preRotate((float) -degree);

                        tempCanvas.drawBitmap(overlay, editMatrix, null);
                    } catch (Exception e) {
                        Log.e(TAG, "Exception caught: value null in facial coordinates");
                    }
                }
                else if(selectedoverlay == R.drawable.joker_hat){
                    for (Landmark a : allLands) {

                        if (a.getType() == Landmark.LEFT_EYE) {
                            leftEye = a.getPosition();
                        } else if (a.getType() == Landmark.NOSE_BASE) {
                            noseBase = a.getPosition();
                        } else if (a.getType() == Landmark.RIGHT_EYE) {
                            rightEye = a.getPosition();
                        }
                    }

                    PointF pointF = faces.valueAt(i).getPosition();
                    float x = pointF.x + faces.valueAt(i).getWidth() / 2;
                    float y = pointF.y + faces.valueAt(i).getHeight() / 2;

                    // Draws a bounding box around the face.
                    float xOffset = faces.valueAt(i).getWidth() / 2.5f;
                    float yOffset = faces.valueAt(i).getHeight() / 2.5f;
                    float left = x - xOffset;
                    float top = y - yOffset;
                    float right = x + xOffset;
                    float bottom = y + yOffset;

                    try {

                        editMatrix = new Matrix();

                        float scaleY = (right - left) / 500;
                        float scaleX = (left - right) / 500;

                        if (Math.abs(leftEye.y - rightEye.y) > 5) {
                            if (leftEye.y < rightEye.y) {
                                //scaleY = (rightMouth.y - noseBase.y) / 90;
                                editMatrix.setTranslate(left + (2*xOffset ), top - yOffset);


                            } else {
                                //scaleY = (leftMouth.y - noseBase.y) / 90;
                                //editMatrix.setTranslate();

                                editMatrix.setTranslate(left + (3*xOffset ), top - (yOffset/3) );

                            }
                        }
                        else{
                            editMatrix.setTranslate(left + (2*xOffset + xOffset/2 ), top - (yOffset - yOffset/3));
                        }

                        editMatrix.preScale(scaleX, scaleY);
                        degree = Math.toDegrees(Math.atan2(leftEye.y - rightEye.y, leftEye.x - rightEye.x));
                        //degree =  Math.toDegrees(-Math.atan2(rightEye.y - leftEye.y, rightEye.x - leftEye.x));

                        editMatrix.preRotate((float) -degree);

                        if(overlay != null) {

                            tempCanvas.drawBitmap(overlay, editMatrix, null);

                            Bitmap nose = BitmapFactory.decodeResource(getResources(), R.drawable.joker_nose);
                            tempCanvas.drawBitmap(nose, noseBase.x - (nose.getWidth()/2), noseBase.y - (nose.getHeight()/2), null);
                        }


                    } catch (Exception e) {
                        Log.e(TAG, "Exception caught: value null in facial coordinates");
                    }
                }

                else if(selectedoverlay == R.drawable.anonymous){

                    for (Landmark a : allLands) {

                        if (a.getType() == Landmark.LEFT_EYE) {
                            leftEye = a.getPosition();
                        } else if (a.getType() == Landmark.RIGHT_EYE) {
                            rightEye = a.getPosition();
                        }
                    }

                    PointF pointF = faces.valueAt(i).getPosition();
                    float x = pointF.x + faces.valueAt(i).getWidth() / 2;
                    float y = pointF.y + faces.valueAt(i).getHeight() / 2;

                    // Draws a bounding box around the face.
                    float xOffset = faces.valueAt(i).getWidth() / 2.5f;
                    float yOffset = faces.valueAt(i).getHeight() / 2.5f;
                    float left = x - xOffset;
                    float top = y - yOffset;
                    float right = x + xOffset;
                    float bottom = y + yOffset;

                    try {

                        editMatrix = new Matrix();

                        float scaleX = (right - left)/650;
                        float scaleY = (right - left)/570;

                        editMatrix.setTranslate(left + xOffset/5 ,top + yOffset/4);

//                        if (Math.abs(leftEye.y - rightEye.y) > 5) {
//                            if (leftEye.y < rightEye.y) {
//                                //scaleY = (rightMouth.y - noseBase.y) / 90;
//                                Log.e("LEFT","UPAR");
//                                editMatrix.setTranslate(left + (2*xOffset ), top - yOffset);
//
//
//                            } else {
//                                //scaleY = (leftMouth.y - noseBase.y) / 90;
//                                //editMatrix.setTranslate();
//
//                                Log.e("LEFT","neechay");
//                                editMatrix.setTranslate(left + (3*xOffset ), top - (yOffset/3) );
//
//                            }
//                        }
//                        else{
//                            editMatrix.setTranslate(left + (2*xOffset + xOffset/2 ), top - (yOffset - yOffset/3));
//                            Log.e("LEFT","Neutral");
//                        }

                        editMatrix.preScale(scaleX, scaleY);
                        degree = Math.toDegrees(Math.atan2(leftEye.y - rightEye.y, leftEye.x - rightEye.x));
                        //degree =  Math.toDegrees(-Math.atan2(rightEye.y - leftEye.y, rightEye.x - leftEye.x));

                        editMatrix.preRotate((float) -degree);

                        tempCanvas.drawBitmap(overlay, editMatrix, null);


                    } catch (Exception e) {
                        Log.e(TAG, "Exception caught: value null in facial coordinates");
                    }
                }

                else  if( selectedoverlay == R.drawable.glasses){

                    for (Landmark a : allLands) {

                        if (a.getType() == Landmark.LEFT_EYE) {
                            leftEye = a.getPosition();
                        } else if (a.getType() == Landmark.RIGHT_EYE) {
                            rightEye = a.getPosition();
                        }
                    }

                    PointF pointF = faces.valueAt(i).getPosition();
                    float x = pointF.x + faces.valueAt(i).getWidth() / 2;
                    float y = pointF.y + faces.valueAt(i).getHeight() / 2;

                    // Draws a bounding box around the face.
                    float xOffset = faces.valueAt(i).getWidth() / 2.5f;
                    float yOffset = faces.valueAt(i).getHeight() / 2.5f;
                    float left = x - xOffset;
                    float top = y - yOffset;
                    float right = x + xOffset;
                    float bottom = y + yOffset;

                    try {

                        editMatrix = new Matrix();

                        float scaleX = (right - left)/900;
                        float scaleY = (right - left)/1000;

                        editMatrix.setTranslate(left  ,top + yOffset/2);

//                        if (Math.abs(leftEye.y - rightEye.y) > 5) {
//                            if (leftEye.y < rightEye.y) {
//                                //scaleY = (rightMouth.y - noseBase.y) / 90;
//                                Log.e("LEFT","UPAR");
//                                editMatrix.setTranslate(left + (2*xOffset ), top - yOffset);
//
//
//                            } else {
//                                //scaleY = (leftMouth.y - noseBase.y) / 90;
//                                //editMatrix.setTranslate();
//
//                                Log.e("LEFT","neechay");
//                                editMatrix.setTranslate(left + (3*xOffset ), top - (yOffset/3) );
//
//                            }
//                        }
//                        else{
//                            editMatrix.setTranslate(left + (2*xOffset + xOffset/2 ), top - (yOffset - yOffset/3));
//                            Log.e("LEFT","Neutral");
//                        }

                        editMatrix.preScale(scaleX, scaleY);
                        degree = Math.toDegrees(Math.atan2(leftEye.y - rightEye.y, leftEye.x - rightEye.x));
                        //degree =  Math.toDegrees(-Math.atan2(rightEye.y - leftEye.y, rightEye.x - leftEye.x));

                        editMatrix.preRotate((float) -degree);

                        tempCanvas.drawBitmap(overlay, editMatrix, null);

                    } catch (Exception e) {
                        Log.e(TAG, "Exception caught: value null in facial coordinates");
                    }
                }
                else  if (selectedoverlay == R.drawable.flower_crown) {

                    for (Landmark a : allLands) {

                        if (a.getType() == Landmark.LEFT_EYE) {
                            leftEye = a.getPosition();
                        } else if (a.getType() == Landmark.RIGHT_EYE) {
                            rightEye = a.getPosition();
                        }
                    }

                    PointF pointF = faces.valueAt(i).getPosition();
                    float x = pointF.x + faces.valueAt(i).getWidth() / 2;
                    float y = pointF.y + faces.valueAt(i).getHeight() / 2;

                    // Draws a bounding box around the face.
                    float xOffset = faces.valueAt(i).getWidth() / 2.5f;
                    float yOffset = faces.valueAt(i).getHeight() / 2.5f;
                    float left = x - xOffset;
                    float top = y - yOffset;
                    float right = x + xOffset;
                    float bottom = y + yOffset;

                    try {

                        editMatrix = new Matrix();

                        float scaleX = (right - left)/800;
                        float scaleY = (right - left)/600;
                        editMatrix.setTranslate(left + xOffset/10  ,top - yOffset/2);

//                        if (Math.abs(leftEye.y - rightEye.y) > 5) {
//                            if (leftEye.y < rightEye.y) {
//                                //scaleY = (rightMouth.y - noseBase.y) / 90;
//                                Log.e("LEFT","UPAR");
//                                editMatrix.setTranslate(left + (2*xOffset ), top - yOffset);
//
//
//                            } else {
//                                //scaleY = (leftMouth.y - noseBase.y) / 90;
//                                //editMatrix.setTranslate();
//
//                                Log.e("LEFT","neechay");
//                                editMatrix.setTranslate(left + (3*xOffset ), top - (yOffset/3) );
//
//                            }
//                        }
//                        else{
//                            editMatrix.setTranslate(left + (2*xOffset + xOffset/2 ), top - (yOffset - yOffset/3));
//                            Log.e("LEFT","Neutral");
//                        }

                        editMatrix.preScale(scaleX, scaleY);
                        degree = Math.toDegrees(Math.atan2(leftEye.y - rightEye.y, leftEye.x - rightEye.x));
                        //degree =  Math.toDegrees(-Math.atan2(rightEye.y - leftEye.y, rightEye.x - leftEye.x));

                        editMatrix.preRotate((float) -degree);

                        tempCanvas.drawBitmap(overlay, editMatrix, null);

                    } catch (Exception e) {
                        Log.e(TAG, "Exception caught: value null in facial coordinates");
                    }
                }
                else if(selectedoverlay == R.drawable.dog_ear) {

                    for (Landmark a : allLands) {

                        if (a.getType() == Landmark.LEFT_EYE) {
                            leftEye = a.getPosition();
                        } else if (a.getType() == Landmark.NOSE_BASE) {
                            noseBase = a.getPosition();
                        } else if (a.getType() == Landmark.RIGHT_EYE) {
                            rightEye = a.getPosition();
                        }
                    }

                    PointF pointF = faces.valueAt(i).getPosition();
                    float x = pointF.x + faces.valueAt(i).getWidth() / 2;
                    float y = pointF.y + faces.valueAt(i).getHeight() / 2;

                    // Draws a bounding box around the face.
                    float xOffset = faces.valueAt(i).getWidth() / 2.5f;
                    float yOffset = faces.valueAt(i).getHeight() / 2.5f;
                    float left = x - xOffset;
                    float top = y - yOffset;
                    float right = x + xOffset;
                    float bottom = y + yOffset;

                    try {

                        editMatrix = new Matrix();

                        float scaleX = (right - left)/800;
                        float scaleY = (right - left)/600;
                        editMatrix.setTranslate(left + xOffset/8  ,top - yOffset/3);

//                        if (Math.abs(leftEye.y - rightEye.y) > 5) {
//                            if (leftEye.y < rightEye.y) {
//                                //scaleY = (rightMouth.y - noseBase.y) / 90;
//                                Log.e("LEFT","UPAR");
//                                editMatrix.setTranslate(left + (2*xOffset ), top - yOffset);
//
//
//                            } else {
//                                //scaleY = (leftMouth.y - noseBase.y) / 90;
//                                //editMatrix.setTranslate();
//
//                                Log.e("LEFT","neechay");
//                                editMatrix.setTranslate(left + (3*xOffset ), top - (yOffset/3) );
//
//                            }
//                        }
//                        else{
//                            editMatrix.setTranslate(left + (2*xOffset + xOffset/2 ), top - (yOffset - yOffset/3));
//                            Log.e("LEFT","Neutral");
//                        }

                        editMatrix.preScale(scaleX, scaleY);
                        degree = Math.toDegrees(Math.atan2(leftEye.y - rightEye.y, leftEye.x - rightEye.x));
                        //degree =  Math.toDegrees(-Math.atan2(rightEye.y - leftEye.y, rightEye.x - leftEye.x));

                        editMatrix.preRotate((float) -degree);

                        if(overlay != null) {

                            tempCanvas.drawBitmap(overlay, editMatrix, null);

                            Bitmap nose = BitmapFactory.decodeResource(getResources(), R.drawable.dog_nose);
                            tempCanvas.drawBitmap(nose, noseBase.x - (nose.getWidth()/2), noseBase.y - (nose.getHeight()/2), null);
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "Exception caught: value null in facial coordinates");
                    }
                }
                else if(selectedoverlay == R.drawable.rabbit_ears){

                    for (Landmark a : allLands) {

                        if (a.getType() == Landmark.LEFT_EYE) {
                            leftEye = a.getPosition();
                        } else if (a.getType() == Landmark.NOSE_BASE) {
                            noseBase = a.getPosition();
                        } else if (a.getType() == Landmark.RIGHT_EYE) {
                            rightEye = a.getPosition();
                        }
                    }

                    PointF pointF = faces.valueAt(i).getPosition();
                    float x = pointF.x + faces.valueAt(i).getWidth() / 2;
                    float y = pointF.y + faces.valueAt(i).getHeight() / 2;

                    // Draws a bounding box around the face.
                    float xOffset = faces.valueAt(i).getWidth() / 2.5f;
                    float yOffset = faces.valueAt(i).getHeight() / 2.5f;
                    float left = x - xOffset;
                    float top = y - yOffset;
                    float right = x + xOffset;
                    float bottom = y + yOffset;

                    try {

                        editMatrix = new Matrix();

                        editMatrix.setTranslate(left + xOffset/6  ,top - yOffset/2);
                        float scaleX = (right - left)/700;
                        float scaleY = (right - left)/700;

//                        if (Math.abs(leftEye.y - rightEye.y) > 5) {
//                            if (leftEye.y < rightEye.y) {
//                                //scaleY = (rightMouth.y - noseBase.y) / 90;
//                                Log.e("LEFT","UPAR");
//                                editMatrix.setTranslate(left + (2*xOffset ), top - yOffset);
//
//
//                            } else {
//                                //scaleY = (leftMouth.y - noseBase.y) / 90;
//                                //editMatrix.setTranslate();
//
//                                Log.e("LEFT","neechay");
//                                editMatrix.setTranslate(left + (3*xOffset ), top - (yOffset/3) );
//
//                            }
//                        }
//                        else{
//                            editMatrix.setTranslate(left + (2*xOffset + xOffset/2 ), top - (yOffset - yOffset/3));
//                            Log.e("LEFT","Neutral");
//                        }

                        editMatrix.preScale(scaleX, scaleY);
                        degree = Math.toDegrees(Math.atan2(leftEye.y - rightEye.y, leftEye.x - rightEye.x));
                        //degree =  Math.toDegrees(-Math.atan2(rightEye.y - leftEye.y, rightEye.x - leftEye.x));

                        editMatrix.preRotate((float) -degree);

                        if(overlay != null) {

                            tempCanvas.drawBitmap(overlay, editMatrix, null);

                            Bitmap nose = BitmapFactory.decodeResource(getResources(), R.drawable.rabbit_nose);
                            tempCanvas.drawBitmap(nose, noseBase.x - (nose.getWidth()/2), noseBase.y - (nose.getHeight()/2), null);
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "Exception caught: value null in facial coordinates");
                    }
                }
                else if(selectedoverlay == R.drawable.cat_ears){

                    for (Landmark a : allLands) {

                        if (a.getType() == Landmark.LEFT_EYE) {
                            leftEye = a.getPosition();
                        } else if (a.getType() == Landmark.NOSE_BASE) {
                            noseBase = a.getPosition();
                        } else if (a.getType() == Landmark.RIGHT_EYE) {
                            rightEye = a.getPosition();
                        }
                    }

                    PointF pointF = faces.valueAt(i).getPosition();
                    float x = pointF.x + faces.valueAt(i).getWidth() / 2;
                    float y = pointF.y + faces.valueAt(i).getHeight() / 2;

                    // Draws a bounding box around the face.
                    float xOffset = faces.valueAt(i).getWidth() / 2.5f;
                    float yOffset = faces.valueAt(i).getHeight() / 2.5f;
                    float left = x - xOffset;
                    float top = y - yOffset;
                    float right = x + xOffset;
                    float bottom = y + yOffset;

                    try {

                        editMatrix = new Matrix();

                        editMatrix.setTranslate(left + xOffset/6 ,top - yOffset/4);
                        float scaleX = (right - left)/800;
                        float scaleY = (right - left)/600;

//                        if (Math.abs(leftEye.y - rightEye.y) > 5) {
//                            if (leftEye.y < rightEye.y) {
//                                //scaleY = (rightMouth.y - noseBase.y) / 90;
//                                Log.e("LEFT","UPAR");
//                                editMatrix.setTranslate(left + (2*xOffset ), top - yOffset);
//
//
//                            } else {
//                                //scaleY = (leftMouth.y - noseBase.y) / 90;
//                                //editMatrix.setTranslate();
//
//                                Log.e("LEFT","neechay");
//                                editMatrix.setTranslate(left + (3*xOffset ), top - (yOffset/3) );
//
//                            }
//                        }
//                        else{
//                            editMatrix.setTranslate(left + (2*xOffset + xOffset/2 ), top - (yOffset - yOffset/3));
//                            Log.e("LEFT","Neutral");
//                        }

                        editMatrix.preScale(scaleX, scaleY);
                        degree = Math.toDegrees(Math.atan2(leftEye.y - rightEye.y, leftEye.x - rightEye.x));
                        //degree =  Math.toDegrees(-Math.atan2(rightEye.y - leftEye.y, rightEye.x - leftEye.x));

                        editMatrix.preRotate((float) -degree);

                        if(overlay != null) {

                            tempCanvas.drawBitmap(overlay, editMatrix, null);
                            Log.e("overlay", "not null");
                            Bitmap nose = BitmapFactory.decodeResource(getResources(), R.drawable.cat_nose);
                            tempCanvas.drawBitmap(nose, noseBase.x - (nose.getWidth()/2), noseBase.y - (nose.getHeight()/2), null);
                        }else {
                            Log.e("overlay","null");
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "Exception caught: value null in facial coordinates");
                    }
                }else{
                    Log.e("overlay","full else");}

            }

            File file = new File(verizeEditFile, "picture" + j+ ".jpg");

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


        notifyBuilder.setContentText("Frames Done");
        notifyBuilder.setProgress(0, 0, false);

        notificationManager.notify(video_id, notifyBuilder.build());


    }


    @Override
    public void onDestroy() {

        Intent intent = new Intent(this, VideoBuildService.class);
        intent.putExtra("picsPath", verizeEditFile.toString());
        intent.putExtra("videoPath", currentdirectory + "/play.h264");
        intent.putExtra("audioPath", currentdirectory + "/play.aac");
        intent.putExtra("generalPath", currentdirectory);
        intent.putExtra("imageCounter", imageCounter);
        intent.putExtra("receiver", VideoEditActivity.receiverEdit);

        startService(intent);


        super.onDestroy();
    }
}
