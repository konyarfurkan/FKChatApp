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
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView profile_name,profile_status,profile_totalFriends;
    private Button sendRequest_button,declineRequest_button;

    private DatabaseReference userDatabaseReference;
    private DatabaseReference friendRequestsDatabaseReference;
    private DatabaseReference friendDatabaseReference;
    private DatabaseReference notificationDatabaseReference;
    private FirebaseUser current_user;

    private ProgressDialog progressDialog;
    private String current_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id=getIntent().getStringExtra("user_id");
        userDatabaseReference= FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        friendRequestsDatabaseReference=FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        friendDatabaseReference=FirebaseDatabase.getInstance().getReference().child("Friends");
        notificationDatabaseReference=FirebaseDatabase.getInstance().getReference().child("Notifications");
        current_user= FirebaseAuth.getInstance().getCurrentUser();

        profileImageView=findViewById(R.id.profile_imageView);
        profile_name=findViewById(R.id.profile_displayName);
        profile_status=findViewById(R.id.profile_status);
        profile_totalFriends=findViewById(R.id.profile_totalFriends);
        sendRequest_button=findViewById(R.id.profile_sendRequest_button);
        declineRequest_button=findViewById(R.id.profile_declineRequest_button);

        current_state="not friends";
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("");
        progressDialog.setMessage("");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        userDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String display_name=dataSnapshot.child("display_name").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                String image=dataSnapshot.child("image").getValue().toString();

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

                if(current_state.equals("not friends")){

                    friendRequestsDatabaseReference.child(current_user.getUid()).child(user_id).child("request_type")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){

                                friendRequestsDatabaseReference.child(user_id).child(current_user.getUid()).child("request_type")
                                        .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        HashMap<String,String> notification_data=new HashMap<>();
                                        notification_data.put("from",current_user.getUid());
                                        notification_data.put("type","request");

                                        notificationDatabaseReference.child(user_id).push().setValue(notification_data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                current_state="request sent";
                                                sendRequest_button.setText("Cancel Friend Request");

                                                declineRequest_button.setVisibility(View.INVISIBLE);
                                                declineRequest_button.setEnabled(false);

                                            }
                                        });



                                       // Toast.makeText(ProfileActivity.this, "Request Sent Succesfully", Toast.LENGTH_SHORT).show();

                                    }
                                });

                            }
                            else{

                                Toast.makeText(ProfileActivity.this, "Failed Sending Request", Toast.LENGTH_SHORT).show();
                                
                            }
                            sendRequest_button.setEnabled(true);


                        }
                    });

                }

                if(current_state.equals("friends")){

                    friendDatabaseReference.child(current_user.getUid()).child(user_id)
                            .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            friendDatabaseReference.child(user_id).child(current_user.getUid())
                                    .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    sendRequest_button.setEnabled(true);
                                    current_state="not friends";
                                    sendRequest_button.setText("Send Friend Request");
                                    declineRequest_button.setVisibility(View.INVISIBLE);
                                    declineRequest_button.setEnabled(false);

                                }
                            });

                        }
                    });


                }

                //------------CANCEL REQUEST STATE-------
                if(current_state.equals("request sent")){

                    friendRequestsDatabaseReference.child(current_user.getUid()).child(user_id)
                            .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            friendRequestsDatabaseReference.child(user_id).child(current_user.getUid())
                                    .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    sendRequest_button.setEnabled(true);
                                    current_state="not friends";
                                    sendRequest_button.setText("Send Friend Request");

                                }
                            });

                        }
                    });

                }



                //-------- REQUEST RECEIVED STATE -------

                if(current_state.equals("request received")){

                    final String current_date= DateFormat.getDateTimeInstance().format(new Date());

                    friendDatabaseReference.child(current_user.getUid()).child(user_id).setValue(current_date)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    friendDatabaseReference.child(user_id).child(current_user.getUid()).setValue(current_date)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    friendRequestsDatabaseReference.child(current_user.getUid()).child(user_id)
                                                            .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                            friendRequestsDatabaseReference.child(user_id).child(current_user.getUid())
                                                                    .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                    sendRequest_button.setEnabled(true);
                                                                    current_state="friends";
                                                                    sendRequest_button.setText("Unfriend this person");
                                                                    declineRequest_button.setVisibility(View.INVISIBLE);
                                                                    declineRequest_button.setEnabled(false);

                                                                }
                                                            });

                                                        }
                                                    });

                                                }
                                            });

                                }
                            });

                }


            }
        });

    }
}
