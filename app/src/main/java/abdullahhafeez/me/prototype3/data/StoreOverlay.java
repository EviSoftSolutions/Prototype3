package abdullahhafeez.me.prototype3.data;

/**
 * Created by Abdullah on 11/22/2017.
 */

public class StoreOverlay {

    private int overlay;
    private String overlayName;

    public StoreOverlay(int overlay, String overlayName) {
        this.overlay = overlay;
        this.overlayName = overlayName;
    }

    public int getOverlay() {
        return overlay;
    }

    public void setOverlay(int overlay) {
        this.overlay = overlay;
    }

    public String getOverlayName() {
        return overlayName;
    }

    public void setOverlayName(String overlayName) {
        this.overlayName = overlayName;
    }
}
