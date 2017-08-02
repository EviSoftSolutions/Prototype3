package abdullahhafeez.me.prototype2;

import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.content.Context;

import java.io.IOException;

public class ImageSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private Camera camera;
    private SurfaceHolder surfaceHolder;

    public ImageSurfaceView(Context context, Camera camera) {
        super(context);
        this.camera = camera;
        this.surfaceHolder = getHolder();
        this.surfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            this.camera.setDisplayOrientation(90);
            Parameters p = camera.getParameters();
            p.setFlashMode(Parameters.FLASH_MODE_OFF);
            camera.setParameters(p);
            this.camera.setPreviewDisplay(holder);
            this.camera.startPreview();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        this.camera.stopPreview();
        this.camera.release();

    }


}
