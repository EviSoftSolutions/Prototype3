package abdullahhafeez.me.prototype3.livechat.main;

import android.Manifest;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import abdullahhafeez.me.prototype3.R;
import abdullahhafeez.me.prototype3.livechat.call.CallActivity;
import abdullahhafeez.me.prototype3.databinding.ActivityVideoChatMainBinding;


import static abdullahhafeez.me.prototype3.livechat.util.Constants.EXTRA_ROOMID;

/**
 * Handles the initial setup where the user selects which room to join.
 */
public class VideoChatMainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "VideoChatMainActivity";
    private static final int CONNECTION_REQUEST = 1;
    private static final int RC_CALL = 111;
    private ActivityVideoChatMainBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_chat_main);
        binding.connectButton.setOnClickListener(v -> connect());
        binding.roomEdittext.requestFocus();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
       // EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

   // @AfterPermissionGranted(RC_CALL)
    private void connect() {
     //   String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
//        if (EasyPermissions.hasPermissions(this, perms)) {
            connectToRoom(binding.roomEdittext.getText().toString());
       // } else {
       //     EasyPermissions.requestPermissions(this, "Need some permissions", RC_CALL, perms);
      //  }
    }

    private void connectToRoom(String roomId) {
        Intent intent = new Intent(this, CallActivity.class);
        intent.putExtra(EXTRA_ROOMID, roomId);
        startActivityForResult(intent, CONNECTION_REQUEST);
    }
}
