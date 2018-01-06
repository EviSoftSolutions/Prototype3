package abdullahhafeez.me.prototype3.activities;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.security.auth.login.LoginException;

import abdullahhafeez.me.prototype3.R;
import abdullahhafeez.me.prototype3.data.Profile;
import abdullahhafeez.me.prototype3.others.Utils;
import de.mrapp.android.dialog.ProgressDialog;

public class ProfileActivity extends AppCompatActivity {

    ImageView profileImage;
    EditText profileNameText;
    TextView profileEmailText;
    Button updateButton;
    private String profileImageUrl = "empty";
    private Uri imageUri;
    private FirebaseStorage firebaseStorage;
    private FirebaseDatabase firebaseDatabase;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private ProgressDialog.Builder dialogBuilder;
    private ProgressDialog dialog;
    SharedPreferences sharedpreferences;
    public final String mypreference = "mypref";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImage =  findViewById(R.id.profileImage);
        profileNameText =  findViewById(R.id.profileNameText);
        profileEmailText =  findViewById(R.id.profileEmailText);
        updateButton = findViewById(R.id.updateButton);

        profileNameText.setText(Utils.profile.getName());
        profileEmailText.setText(Utils.profile.getEmail());
        dialogBuilder = new ProgressDialog.Builder(this);
        dialogBuilder.setMessage("Your profile is updating");
        dialogBuilder.setProgressBarPosition(ProgressDialog.ProgressBarPosition.LEFT);
        dialog = dialogBuilder.create();

        sharedpreferences = getSharedPreferences(mypreference,
                Context.MODE_PRIVATE);

        if (!Utils.profile.getProfilePhotoUrl().equals("empty")) {
            Glide.with(ProfileActivity.this).load(Utils.profile.getProfilePhotoUrl()).apply(RequestOptions.circleCropTransform()).into(profileImage);
        }

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, Utils.RC_PHOTO_PICKER);
            }
        });


        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();

                if (!Utils.profile.getProfilePhotoUrl().equals("empty")) {

                    storageReference = firebaseStorage.getReferenceFromUrl(Utils.profile.getProfilePhotoUrl());
                    storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    });
                }
                uploadProfileImage();
            }
        });



    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case Utils.RC_PHOTO_PICKER:
                if(resultCode == RESULT_OK){
                    try {
                        imageUri = imageReturnedIntent.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        Bitmap profileImageBmp = BitmapFactory.decodeStream(imageStream);
                        profileImage.setImageBitmap(profileImageBmp);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
        }
    }

    private void uploadProfileImage() {

        storageReference = firebaseStorage.getReference();

        if (imageUri != null) {
            StorageReference myRef = storageReference.child(imageUri.getLastPathSegment());

            myRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    profileImageUrl = taskSnapshot.getDownloadUrl().toString();
                    saveProfileData();
                }
            });


        }
        else{
            saveProfileData();
        }




    }

    private void saveProfileData() {
        final Profile profile = new Profile(profileNameText.getText().toString(),
                profileEmailText.getText().toString(),
                profileImageUrl);
        SharedPreferences.Editor editor = sharedpreferences.edit();

        editor.putString("imageurl", profileImageUrl);
        editor.putString("name", profileNameText.getText().toString());

        editor.commit();

        databaseReference = firebaseDatabase.getReference("user/");


        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(Utils.userId.equals(dataSnapshot.getKey())){
                    databaseReference.child(Utils.userId).setValue(profile).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            dialog.hide();
                            finish();
                        }
                    });
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        Utils.profile = profile;

    }
}
