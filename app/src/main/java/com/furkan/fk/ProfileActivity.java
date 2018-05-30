package com.furkan.fk;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView profile_name, profile_status, profile_totalFriends;
    private Button sendRequest_button, declineRequest_button;

    private DatabaseReference userDatabaseReference;
    private DatabaseReference friendRequestsDatabaseReference;
    private DatabaseReference friendDatabaseReference;
    private DatabaseReference notificationDatabaseReference;
    private DatabaseReference rootDatabaseReference;
    private FirebaseUser current_user;

    private ProgressDialog progressDialog;
    private String current_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");

        rootDatabaseReference = FirebaseDatabase.getInstance().getReference();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        friendRequestsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        friendDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Friends");
        notificationDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Notifications");
        current_user = FirebaseAuth.getInstance().getCurrentUser();

        profileImageView = findViewById(R.id.profile_imageView);
        profile_name = findViewById(R.id.profile_displayName);
        profile_status = findViewById(R.id.profile_status);
        profile_totalFriends = findViewById(R.id.profile_totalFriends);
        sendRequest_button = findViewById(R.id.profile_sendRequest_button);
        declineRequest_button = findViewById(R.id.profile_declineRequest_button);

        declineRequest_button.setVisibility(View.INVISIBLE);
        declineRequest_button.setEnabled(false);

        current_state = "not friends";
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("");
        progressDialog.setMessage("");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        userDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String display_name = dataSnapshot.child("display_name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                profile_name.setText(display_name);
                profile_status.setText(status);
                Picasso.get().load(image).placeholder(R.mipmap.default_avatar).into(profileImageView);

                //-------- FRIENDS LIST  / REQUEST FEATURE -----
                friendRequestsDatabaseReference.child(current_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(user_id)) {

                            String requestType = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if (requestType.equals("received")) {

                                current_state = "request received";
                                sendRequest_button.setText("Accept Friend Request");
                                declineRequest_button.setVisibility(View.VISIBLE);
                                declineRequest_button.setEnabled(true);

                            } else if (requestType.equals("sent")) {

                                current_state = "request sent";
                                sendRequest_button.setText("Cancel Friend Request");
                                declineRequest_button.setVisibility(View.INVISIBLE);
                                declineRequest_button.setEnabled(false);

                            }
                            progressDialog.dismiss();
                        } else {

                            friendDatabaseReference.child(current_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(user_id)) {

                                        current_state = "friends";
                                        sendRequest_button.setText("Unfriend this person");
                                        declineRequest_button.setVisibility(View.INVISIBLE);
                                        declineRequest_button.setEnabled(false);

                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    progressDialog.dismiss();

                                }
                            });

                        }

                    }


                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                progressDialog.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sendRequest_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendRequest_button.setEnabled(false);

                //-------------NOT FRIENDS STATE------------

                if (current_state.equals("not friends")) {

                    DatabaseReference newNotificationReference = rootDatabaseReference.child("Notifications").child(user_id).push();
                    String newNotificationId = newNotificationReference.getKey();

                    HashMap<String, String> notification_data = new HashMap<>();
                    notification_data.put("from", current_user.getUid());
                    notification_data.put("type", "request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friend Requests/" + current_user.getUid() + "/" + user_id + "/request_type", "sent");
                    requestMap.put("Friend Requests/" + user_id + "/" + current_user.getUid() + "/request_type", "received");
                    requestMap.put("Notifications/" + user_id + "/" + newNotificationId, notification_data);

                    rootDatabaseReference.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError != null) {

                                Toast.makeText(ProfileActivity.this, "There was some error in sending request", Toast.LENGTH_SHORT).show();

                            }

                            sendRequest_button.setEnabled(true);
                            current_state = "request sent";
                            sendRequest_button.setText("Cancel Friend Request");

                        }
                    });
                }

                //------------CANCEL REQUEST STATE-------
                if (current_state.equals("request sent")) {

                    friendRequestsDatabaseReference.child(current_user.getUid()).child(user_id)
                            .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            friendRequestsDatabaseReference.child(user_id).child(current_user.getUid())
                                    .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    sendRequest_button.setEnabled(true);
                                    current_state = "not friends";
                                    sendRequest_button.setText("Send Friend Request");

                                }
                            });

                        }
                    });

                }


                //-------- REQUEST RECEIVED STATE -------

                if (current_state.equals("request received")) {

                    final String current_date = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + current_user.getUid() + "/" + user_id + "/date", current_date);
                    friendsMap.put("Friends/" + user_id + "/" + current_user.getUid() + "/date", current_date);

                    friendsMap.put("Friend Requests/" + current_user.getUid() + "/" + user_id, null);
                    friendsMap.put("Friend Requests/" + user_id + "/" + current_user.getUid(), null);

                    rootDatabaseReference.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError == null) {

                                sendRequest_button.setEnabled(true);
                                current_state = "friends";
                                sendRequest_button.setText("Unfriend this person");

                                declineRequest_button.setVisibility(View.INVISIBLE);
                                declineRequest_button.setEnabled(false);

                            } else {

                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                            }

                        }
                    });

                }
                // ==============UNFRIEND==============

                if (current_state.equals("friends")) {

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + current_user.getUid() + "/" + user_id, null);
                    unfriendMap.put("Friends/" + user_id + "/" + current_user.getUid(), null);

                    rootDatabaseReference.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError == null) {

                                current_state = "not friends";
                                sendRequest_button.setText("Send Friend Request");

                                declineRequest_button.setVisibility(View.INVISIBLE);
                                declineRequest_button.setEnabled(false);

                            } else {

                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                            }

                            sendRequest_button.setEnabled(true);

                        }
                    });

                }


            }
        });

        declineRequest_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map declineRequestsMap = new HashMap();
                declineRequestsMap.put("Friend Requests/" + current_user.getUid() + "/" + user_id, null);
                declineRequestsMap.put("Friend Requests/" + user_id + "/" + current_user.getUid(), null);

                rootDatabaseReference.updateChildren(declineRequestsMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                        if (databaseError == null) {


                            current_state = "not friends";
                            sendRequest_button.setText("Send Friend Request");

                            declineRequest_button.setVisibility(View.INVISIBLE);
                            declineRequest_button.setEnabled(false);

                        } else {

                            String error = databaseError.getMessage();
                            Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                        }

                        sendRequest_button.setEnabled(true);

                    }
                });

            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();

        FirebaseDatabase.getInstance().getReference().child("Users").child(current_user.getUid()).child("online").setValue(ServerValue.TIMESTAMP);


    }

    @Override
    protected void onResume() {
        super.onResume();

        FirebaseDatabase.getInstance().getReference().child("Users").child(current_user.getUid()).child("online").setValue("true");


    }

}
