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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.hardware.display.DisplayManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import abdullahhafeez.me.prototype2.GraphicOverlay;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;

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

    private volatile Face mFace;
    private int mFaceId;
    private float mFaceHappiness;

    FaceGraphic(GraphicOverlay overlay) {
        super(overlay);

        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];

        mFacePositionPaint = new Paint();
        mFacePositionPaint.setColor(selectedColor);

        mIdPaint = new Paint();
        mIdPaint.setColor(selectedColor);
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(selectedColor);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
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
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null) {
            return;
        }

        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        float y = translateY(face.getPosition().y + face.getHeight() / 2);

        List<Landmark> allLands = face.getLandmarks();

      PointF Fpoints = face.getPosition();
      //  for(Landmark a : allLands) {
            //canvas.drawCircle(x, y, FACE_POSITION_RADIUS, mFacePositionPaint);
          //  PointF point = a.getPosition();
          //  Log.d("POINT", point.toString());
//            Log.d("FACE POINT", Fpoints.toString());
            //canvas.drawCircle(Fpoints.x, Fpoints.y, FACE_POSITION_RADIUS, mFacePositionPaint);
            //canvas.drawCircle(Fpoints.x + point.x , Fpoints.y + point.y, FACE_POSITION_RADIUS, mFacePositionPaint);
     //   }

        //canvas.drawText("id: " + mFaceId, x + ID_X_OFFSET, y + ID_Y_OFFSET, mIdPaint);
        //canvas.drawText("happiness: " + String.format("%.2f", face.getIsSmilingProbability()), x - ID_X_OFFSET, y - ID_Y_OFFSET, mIdPaint);
        //canvas.drawText("right eye: " + String.format("%.2f", face.getIsRightEyeOpenProbability()), x + ID_X_OFFSET * 2, y + ID_Y_OFFSET * 2, mIdPaint);
        //canvas.drawText("left eye: " + String.format("%.2f", face.getIsLeftEyeOpenProbability()), x - ID_X_OFFSET*2, y - ID_Y_OFFSET*2, mIdPaint);



        // Draws a bounding box around the face.
        float xOffset = scaleX(face.getWidth() / 2.0f);
        float yOffset = scaleY(face.getHeight() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
        for(Landmark a : allLands) {
            //canvas.drawCircle(x, y, FACE_POSITION_RADIUS, mFacePositionPaint);
            PointF point = a.getPosition();
            //Log.d("POINT", point.toString());
            //Log.d("FACE POINT", Fpoints.toString());

            switch (a.getType()) {
                case 0:
                    Log.d("BOTTOM_MOUTH", point.toString());
                    break;
                case 1:
                    Log.d("LEFT_CHEEK", point.toString());
                    break;
                case 2:
                    Log.d("LEFT_EAR_TIP", point.toString());
                    break;
                case 3:
                    Log.d("LEFT_EAR", point.toString());
                    break;
                case 4:
                    Log.d("LEFT_EYE", point.toString());
                    break;
                case 5:
                    Log.d("LEFT_MOUTH", point.toString());
                    break;
                case 6:
                    Log.d("NOSE_BASE", point.toString());
                    break;
                case 7:
                    Log.d("RIGHT_CHEEK", point.toString());
                    break;
                case 8:
                    Log.d("RIGHT_EAR_TIP", point.toString());
                    break;
                case 9:
                    Log.d("RIGHT_EAR", point.toString());
                    break;
                case 10:
                    Log.d("RIGHT_EYE", point.toString());
                    break;
                case 11:
                    Log.d("RIGHT_MOUTH", point.toString());
                    break;
            }

            double viewWidth = canvas.getWidth();
            double viewHeight = canvas.getHeight();
            double imageWidth = 480;
            double imageHeight = 640;
            double scale = Math.min(viewWidth/imageWidth, viewHeight/imageHeight);

            //canvas.drawCircle(Fpoints.x, Fpoints.y, FACE_POSITION_RADIUS, mFacePositionPaint);
            //canvas.drawCircle(scaleX(point.x), scaleY(point.y), FACE_POSITION_RADIUS, mFacePositionPaint);


            Log.d("FacePoint", Fpoints.toString());
            canvas.drawCircle((int) (point.x*scale), (int) (point.y*scale), FACE_POSITION_RADIUS,mFacePositionPaint);

            //canvas.drawCircle(  scaleX((float) (point.x * scale)) , scaleY((float) (point.y * scale)), FACE_POSITION_RADIUS, mFacePositionPaint);
            //canvas.drawCircle(Fpoints.x + point.x , Fpoints.y + point.y, FACE_POSITION_RADIUS, mFacePositionPaint);
        }
        //canvas.drawCircle(left, top, FACE_POSITION_RADIUS,mFacePositionPaint);
        //canvas.drawCircle(xOffset, yOffset, FACE_POSITION_RADIUS, mFacePositionPaint);

        canvas.drawRect(left, top, right, bottom, mBoxPaint);
    }
}
