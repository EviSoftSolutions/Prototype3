package abdullahhafeez.me.prototype3.activities;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import abdullahhafeez.me.prototype3.R;
import abdullahhafeez.me.prototype3.adapters.ContactsAdapter;
import abdullahhafeez.me.prototype3.data.Profile;
import abdullahhafeez.me.prototype3.livechat.call.CallActivity;
import abdullahhafeez.me.prototype3.livechat.main.VideoChatMainActivity;
import abdullahhafeez.me.prototype3.others.RecyclerTouchListener;
import abdullahhafeez.me.prototype3.others.Utils;


import static abdullahhafeez.me.prototype3.livechat.util.Constants.EXTRA_ROOMID;

public class ContactsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private ArrayList<Profile> allProfiles;
    private ArrayList<String> userIDList;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private static final int CONNECTION_REQUEST = 1;
    private static final int RC_CALL = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_contacts);
        database = FirebaseDatabase.getInstance();
        allProfiles = new ArrayList<>();
        userIDList = new ArrayList<>();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        setupAdapter();

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {


                Intent intent = new Intent(ContactsActivity.this, CallActivity.class);
                //TODO: Add to firebase of other mail
                intent.putExtra("sendermail", allProfiles.get(position).getEmail());
                intent.putExtra("sendername", allProfiles.get(position).getName());
                intent.putExtra("receiverID", userIDList.get(position));
                 connect();
                 startActivity(intent);

            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));


    }

    //Video Chat mat

    private void connect() {

            connectToRoom(Utils.userId);
            //TODO: Add to firebase of other Gmail

    }

    private void connectToRoom(String roomId) {
        Intent intent = new Intent(this, CallActivity.class);
        intent.putExtra(EXTRA_ROOMID, roomId);
        startActivityForResult(intent, CONNECTION_REQUEST);
    }


    private void setupAdapter() {
        myRef = database.getReference("user");

        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Profile profile = dataSnapshot.getValue(Profile.class);
                userIDList.add(dataSnapshot.getKey());
                allProfiles.add(profile);
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

                int size = allProfiles.size();
                for (int i = 0; i< size;i++) {

                    String userId = userIDList.get(i);
                    if (userId.equals(Utils.userId)) {
                        userIDList.remove(userId);
                        allProfiles.remove(i);
                        break;
                    }


                }

                ContactsAdapter contactsAdapter = new ContactsAdapter(ContactsActivity.this, allProfiles, userIDList);

                recyclerView.setAdapter(contactsAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}
