package com.furkan.fk;


import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private RecyclerView chatsList;

    private DatabaseReference chatsDatabaseReference;
    private DatabaseReference messagesDatabaseReference;
    private DatabaseReference usersDatabaseReference;

    private FirebaseAuth firebaseAuth;

    private String current_user_id;

    private View MainView;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        MainView = inflater.inflate(R.layout.fragment_chats, container, false);

        chatsList = MainView.findViewById(R.id.chats_list);

        firebaseAuth = FirebaseAuth.getInstance();
        current_user_id = firebaseAuth.getCurrentUser().getUid();

        chatsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Chat").child(current_user_id);
        chatsDatabaseReference.keepSynced(true);

        usersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        usersDatabaseReference.keepSynced(true);

        messagesDatabaseReference = FirebaseDatabase.getInstance().getReference().child("messages").child(current_user_id);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        chatsList.setHasFixedSize(true);
        chatsList.setLayoutManager(linearLayoutManager);

        return MainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Query chatsQuery = chatsDatabaseReference.orderByChild("timestamp");

        FirebaseRecyclerAdapter<Chats, ChatsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Chats, ChatsViewHolder>(

                Chats.class,
                R.layout.users_single_layout,
                ChatsViewHolder.class,
                chatsQuery

        ) {
            @Override
            protected void populateViewHolder(final ChatsViewHolder viewHolder, final Chats model, int position) {

                final String list_user_id = getRef(position).getKey();
                Query lastMessageQuery = messagesDatabaseReference.child(list_user_id).limitToLast(1);

                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        String data = dataSnapshot.child("message").getValue().toString();
                        viewHolder.setMessage(data, model.isSeen());

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

                usersDatabaseReference.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("display_name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                        if (dataSnapshot.hasChild("online")) {

                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnline(userOnline);

                        }

                        viewHolder.setName(userName);
                        viewHolder.setImage(userThumb);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("user_id", list_user_id);
                                chatIntent.putExtra("user_name", userName);
                                startActivity(chatIntent);

                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        chatsList.setAdapter(firebaseRecyclerAdapter);


    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ChatsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setName(String name) {

            TextView userNameView = mView.findViewById(R.id.user_single_displayName);
            userNameView.setVisibility(View.VISIBLE);
            userNameView.setText(name);

        }

        public void setImage(final String thumb_image) {

            CircleImageView userImageView = mView.findViewById(R.id.user_single_image);
            userImageView.setVisibility(View.VISIBLE);
            Picasso.get().load(thumb_image).placeholder(R.mipmap.default_avatar).into(userImageView);


        }

        public void setMessage(String message, boolean isSeen) {

            TextView userStatusView = mView.findViewById(R.id.user_single_status);
            userStatusView.setVisibility(View.VISIBLE);
            userStatusView.setText(message);

            if (!isSeen) {

                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.ITALIC);

            } else {

                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);

            }

        }

        public void setUserOnline(String online_status) {

            ImageView userOnlineView = mView.findViewById(R.id.user_single_online);
            if (online_status.equals("true")) {

                userOnlineView.setVisibility(View.VISIBLE);

            } else {

                userOnlineView.setVisibility(View.GONE);

            }


        }

    }


}
