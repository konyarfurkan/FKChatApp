package com.furkan.fk;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String chatUser;
    private String chatUser_displayName;

    private Toolbar toolbar;

    private DatabaseReference rootDatabaseReference;

    private TextView chat_displayName,chat_lastSeen;
    private CircleImageView chat_profileImage;

    private FirebaseAuth firebaseAuth;
    private String current_user_id;

    private ImageButton chat_AddButton,chat_SendButton;
    private EditText chatMessage;

    private RecyclerView mMessagesList;

    private final List<Messages> messagesList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;

    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        toolbar=findViewById(R.id.chat_app_bar);

        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        rootDatabaseReference= FirebaseDatabase.getInstance().getReference();
        firebaseAuth=FirebaseAuth.getInstance();
        current_user_id=firebaseAuth.getCurrentUser().getUid();

        chatUser=getIntent().getStringExtra("user_id");
        chatUser_displayName=getIntent().getStringExtra("user_displayName");


        getSupportActionBar().setTitle(chatUser_displayName);
        LayoutInflater layoutInflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view=layoutInflater.inflate(R.layout.chat_custom_bar,null);

        actionBar.setCustomView(action_bar_view);

        chat_displayName=findViewById(R.id.custom_bar_displayName);
        chat_lastSeen=findViewById(R.id.custom_bar_lastSeen);
        chat_profileImage=findViewById(R.id.custom_bar_image);

        chat_AddButton=findViewById(R.id.chat_addMessage_button);
        chat_SendButton=findViewById(R.id.chat_sendMessage_button);
        chatMessage=findViewById(R.id.chat_Message);

        messageAdapter=new MessageAdapter(messagesList);
        mMessagesList=findViewById(R.id.chat_messagesList);

        linearLayoutManager=new LinearLayoutManager(this);
        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(linearLayoutManager);
        mMessagesList.setAdapter(messageAdapter);

        loadMessages();

        chat_displayName.setText(chatUser_displayName);

        rootDatabaseReference.child("Users").child(chatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String online=dataSnapshot.child("online").getValue().toString();
                String thumbImage=dataSnapshot.child("thumb_image").getValue().toString();

                Picasso.get().load(thumbImage).placeholder(R.mipmap.default_avatar).into(chat_profileImage);


                if (online.equals("true")){

                    chat_lastSeen.setText("Online");

                }
                else{

                    GetTimeAgo getTimeAgo=new GetTimeAgo();
                    long lastTime=Long.parseLong(online);
                    String lastSeenTime=getTimeAgo.getTimeAgo(lastTime,getApplicationContext());

                    chat_lastSeen.setText(lastSeenTime);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        rootDatabaseReference.child("Chat").child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(chatUser)){

                    Map chatAddMap=new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp",ServerValue.TIMESTAMP);

                    Map chatUserMap=new HashMap();
                    chatUserMap.put("Chat/"+current_user_id+"/"+chatUser,chatAddMap);
                    chatUserMap.put("Chat/"+chatUser+"/"+current_user_id,chatAddMap);

                    rootDatabaseReference.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError!=null){

                                Log.d("CHAT LOG",databaseError.getMessage().toString());

                            }

                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        chat_SendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage();

            }
        });



    }

    private void loadMessages() {

        rootDatabaseReference.child("messages").child(current_user_id).child(chatUser).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message=dataSnapshot.getValue(Messages.class);

                messagesList.add(message);
                messageAdapter.notifyDataSetChanged();

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

    }

    private void sendMessage() {

        String message=chatMessage.getText().toString();

        if(!TextUtils.isEmpty(message)){

            String current_userRef="messages/"+current_user_id+"/"+chatUser;
            String chatUserRef="messages/"+chatUser+"/"+current_user_id;

            DatabaseReference user_message_push=rootDatabaseReference.child("Messages").child(current_user_id).child(chatUser).push();
            String push_id=user_message_push.getKey();

            Map messageMap=new HashMap();
            messageMap.put("message",message);
            messageMap.put("seen",false);
            messageMap.put("type","text");
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("from",current_user_id);

            Map messageUserMap=new HashMap();
            messageUserMap.put(current_userRef+"/"+push_id,messageMap);
            messageUserMap.put(chatUserRef+"/"+push_id,messageMap);

            chatMessage.setText("");

            rootDatabaseReference.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if(databaseError!=null){

                        Log.d("CHAT LOG2",databaseError.getMessage().toString());

                    }


                }
            });



        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        FirebaseDatabase.getInstance().getReference().child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("online").setValue(ServerValue.TIMESTAMP);

    }

    @Override
    protected void onResume() {
        super.onResume();

        FirebaseDatabase.getInstance().getReference().child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("online").setValue("true");


    }

}
