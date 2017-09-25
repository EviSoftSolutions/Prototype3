/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package abdullahhafeez.me.prototype2;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import abdullahhafeez.me.prototype2.GraphicOverlay;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
class FaceGraphic extends GraphicOverlay.Graphic {
    private static final float FACE_POSITION_RADIUS = 5.0f;
    private static final float ID_TEXT_SIZE = 30.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;
    private boolean save = true;
    private static final String TAG = "FaceGraphic";

    float faceLeft = -1;
    float faceTop = -1;


    private static final int COLOR_CHOICES[] = {
        Color.BLUE,
        Color.CYAN,
        Color.GREEN,
        Color.MAGENTA,
        Color.RED,
        Color.WHITE,
        Color.YELLOW
    };
    private static int mCurrentColorIndex = 0;

    private Paint mFacePositionPaint;
    private Paint mIdPaint;
    private Paint mBoxPaint;
    private Matrix matrix;
    private Matrix matrix1;
    private Matrix matrix2;
    private volatile Face mFace;
    private int mFaceId;
    private GraphicOverlay overlay;
    double imageWidth = 480;
    double imageHeight = 640;
    PointF point;
    float x;
    float y;

    PointF leftEye = null;
    PointF rightEye = null;
    PointF noseBase = null;
    PointF leftMouth = null;
    PointF rightMouth =null;

    PointF Fpoints;
    List<Landmark> allLands;
    float xOffset;
    float yOffset;
    float left;
    float top;
    float right;
    float bottom;
    double viewWidth;
    double viewHeight;
    Bitmap bitmap;
    double degree;

    FaceGraphic(GraphicOverlay overlay) {
        super(overlay);
        matrix =new Matrix();
        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];
        this.overlay =overlay;
        mFacePositionPaint = new Paint();
        mFacePositionPaint.setColor(selectedColor);

        mIdPaint = new Paint();
        mIdPaint.setColor(selectedColor);
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(selectedColor);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
        bitmap = BitmapFactory.decodeResource(overlay.getContext().getResources(), R.drawable.moustache);
    }

    void setId(int id) {
        mFaceId = id;
    }

    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    void updateFace(Face face) {
        mFace = face;
        postInvalidate();
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas, View view) {
        Face face = mFace;
        if (face == null) {
            return;
        }
        // Draws a circle at the position of the detected face, with the face's track id below.
         x = translateX(face.getPosition().x + face.getWidth() / 2);
         y = translateY(face.getPosition().y + face.getHeight() / 2);
         allLands = face.getLandmarks();
         Fpoints = face.getPosition();
        // Draws a bounding box around the face.
         xOffset = scaleX(face.getWidth() / 2.5f);
         yOffset = scaleY(face.getHeight() / 2.5f);
         left = x - xOffset;
         top = y - yOffset;
         right = x + xOffset;
         bottom = y + yOffset;
         viewWidth = canvas.getWidth();
         viewHeight = canvas.getHeight();
        //double scale = Math.min(viewWidth / imageWidth, viewHeight / imageHeight);
        for(Landmark a : allLands) {
             point = a.getPosition();

            if (a.getType() == Landmark.LEFT_MOUTH){
                    leftMouth = a.getPosition();
            }
            else if (a.getType() ==Landmark.NOSE_BASE){
                noseBase = a.getPosition();
            }else if (a.getType() == Landmark.RIGHT_MOUTH){
                rightMouth = a.getPosition();
            }
             //canvas.drawCircle((int) (translateX(point.x)), (int) (translateY(point.y)), FACE_POSITION_RADIUS, mFacePositionPaint);
            if(a.getType() == Landmark.LEFT_EYE)
                leftEye = a.getPosition();
            else if(a.getType() == Landmark.RIGHT_EYE)
                rightEye = a.getPosition();
            try {
                float scaleY;
                matrix.setTranslate(translateX(leftMouth.x) - ((translateY(leftMouth.y)) - translateY(noseBase.y)) ,translateY(noseBase.y));

                float scaleX = ((translateX(rightMouth.x) - translateX(leftMouth.x)))/180;
                if(leftMouth.y >= rightMouth.y)
                    scaleY= ((translateY(leftMouth.y)) - translateY(noseBase.y))/80;
                else{
                    scaleY= ((translateY(rightMouth.y)) - translateY(noseBase.y))/80;
                }
                matrix.preScale(scaleX, scaleY);
                degree =  Math.toDegrees(-Math.atan2(leftMouth.y - rightMouth.y, leftMouth.x - rightMouth.x));
                matrix.preRotate((float)degree);

                canvas.drawBitmap(bitmap, matrix,null);
            }catch (Exception e){
                Log.e(TAG, "Exception caught: value null in facial coordinates");
            }

        }



    }
}
