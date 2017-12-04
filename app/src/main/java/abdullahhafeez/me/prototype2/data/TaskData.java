package abdullahhafeez.me.prototype2.data;

import android.hardware.Camera;

/**
 * Created by Abdullah on 8/16/2017.
 */

public class TaskData {

    private Camera camera;
    private byte[] imageData;
    private int counterName;

    public TaskData(){}

    public TaskData(Camera camera, byte[] imageData, int counterName) {
        this.camera = camera;
        this.imageData = imageData;
        this.counterName =counterName;
    }

    public int getCounterName() {
        return counterName;
    }

    public void setCounterName(int counterName) {
        this.counterName = counterName;
    }

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }
}
