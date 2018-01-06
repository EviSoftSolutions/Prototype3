package abdullahhafeez.me.prototype3.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jcodec.movtool.Util;

import abdullahhafeez.me.prototype3.R;
import abdullahhafeez.me.prototype3.data.Profile;
import abdullahhafeez.me.prototype3.others.Utils;


public class LoginActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private ProgressBar progressBar;
    private Button btnSignup, btnLogin, btnReset;
    private TextView smsLogo;
    private String name;
    private String email;
    private String profileImageUrl;

    private SharedPreferences sharedpreferences;

    public final String mypreference = "mypref";

    public FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set the view now
        setContentView(R.layout.activity_login);

        inputEmail =   findViewById(R.id.email);
        inputPassword =  findViewById(R.id.password);
        progressBar =  findViewById(R.id.progressBar);
        btnSignup =    findViewById(R.id.btn_signup);
        btnLogin =     findViewById(R.id.btn_login);
        btnReset =     findViewById(R.id.btn_reset_password);

        smsLogo =  findViewById(R.id.smsLogo);
        smsLogo.setTypeface(Typeface.createFromAsset(getAssets(),"Lobster-Regular.ttf"));

        database = FirebaseDatabase.getInstance();

        sharedpreferences = getSharedPreferences(mypreference,
                Context.MODE_PRIVATE);
           name = sharedpreferences.getString("name", "");
           email = sharedpreferences.getString("email", "");
           profileImageUrl = sharedpreferences.getString("imageurl","");

        if (!sharedpreferences.getString("email", "").isEmpty()) {

            Utils.userId = sharedpreferences.getString("userId", "");
            Utils.profile = new Profile(name, email, profileImageUrl);

            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        makeListeners();


    }

    private void makeListeners() {

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                finish();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = inputEmail.getText().toString();
                final String password = inputPassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //authenticate user
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                progressBar.setVisibility(View.GONE);
                                if (!task.isSuccessful()) {
                                    // there was an error
                                    if (password.length() < 6) {
                                        inputPassword.setError("Password too short, enter minimum 6 characters!");
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Authentication failed, check your email and password or sign up", Toast.LENGTH_LONG).show();
                                    }
                                } else {

                                    Utils.userId = auth.getCurrentUser().getUid();
                                    myRef = database.getReference("user/");


                                    myRef.addChildEventListener(new ChildEventListener() {
                                        @Override
                                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                                            if(dataSnapshot.getKey().equals(Utils.userId)){
                                                Utils.profile = dataSnapshot.getValue(Profile.class);
                                                Log.e("asda", Utils.profile.getEmail());
                                                myRef.removeEventListener(this);
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

                                    myRef.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            saveCredentialsLocally(Utils.userId, Utils.profile);

                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });



                                }
                            }
                        });
            }
        });

    }


    public void saveCredentialsLocally(String userId, Profile profile) {
        SharedPreferences.Editor editor = sharedpreferences.edit();

        editor.putString("userId", userId);
        editor.putString("email", profile.getEmail());
        editor.putString("name", profile.getName());
        editor.putString("imageurl", "empty");


        editor.commit();
    }

}
