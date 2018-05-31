package com.furkan.fk;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private RecyclerView recyclerView_friendRequestsList;
    private View mainView;

    private DatabaseReference friendRequestsDatabaseReference;
    private DatabaseReference usersDatabaseReference;

    private FirebaseAuth firebaseAuth;

    private String current_user_id;


    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_requests, container, false);

        recyclerView_friendRequestsList = mainView.findViewById(R.id.friendRequests_list);

        recyclerView_friendRequestsList.setHasFixedSize(true);
        recyclerView_friendRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));

        firebaseAuth = FirebaseAuth.getInstance();
        current_user_id = firebaseAuth.getCurrentUser().getUid();

        friendRequestsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        friendRequestsDatabaseReference.keepSynced(true);
        usersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        usersDatabaseReference.keepSynced(true);

        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        final FirebaseRecyclerAdapter<FriendRequests, RequestsFragment.FriendRequestsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<FriendRequests, FriendRequestsViewHolder>(

                FriendRequests.class,
                R.layout.users_single_layout,
                RequestsFragment.FriendRequestsViewHolder.class,
                friendRequestsDatabaseReference

        ) {



            @Override
            protected void populateViewHolder(final FriendRequestsViewHolder viewHolder, FriendRequests model, final int position) {


                final String user_id = getRef(position).getKey();

                friendRequestsDatabaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id)){


                            usersDatabaseReference.child(user_id).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (!user_id.equals(current_user_id)) {

                                        final String user_displayName = dataSnapshot.child("display_name").getValue().toString();
                                        final String user_thumbImage = dataSnapshot.child("thumb_image").getValue().toString();
                                        if (dataSnapshot.hasChild("online")) {

                                            String userOnline = dataSnapshot.child("online").getValue().toString();
                                            viewHolder.setUserOnline(userOnline);

                                        }

                                        viewHolder.setName(user_displayName);
                                        viewHolder.setImage(user_thumbImage);

                                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                                profileIntent.putExtra("user_id", user_id);
                                                startActivity(profileIntent);

                                            }
                                        });

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            friendRequestsDatabaseReference.child(current_user_id).child(user_id).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild("request_type")) {
                                        String request_type = dataSnapshot.child("request_type").getValue().toString();
                                        viewHolder.setRequest_type(request_type);

                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

        };

        recyclerView_friendRequestsList.setAdapter(firebaseRecyclerAdapter);

    }


    public static class FriendRequestsViewHolder extends RecyclerView.ViewHolder {

        View mView;


        public FriendRequestsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setName(String name) {

            TextView userNameView = mView.findViewById(R.id.user_single_displayName);
            userNameView.setVisibility(View.VISIBLE);
            userNameView.setText(name);

        }

        public void setRequest_type(String request_type) {

            TextView userStatusView = mView.findViewById(R.id.user_single_status);
            userStatusView.setVisibility(View.VISIBLE);
            userStatusView.setText(request_type);

        }

        public void setImage(final String thumb_image) {

            CircleImageView userImageView = mView.findViewById(R.id.user_single_image);
            userImageView.setVisibility(View.VISIBLE);
            Picasso.get().load(thumb_image).placeholder(R.mipmap.default_avatar).into(userImageView);


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
