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
public class FriendsFragment extends Fragment {

    private RecyclerView recyclerView_friendsList;
    private DatabaseReference friendsDatabaseReference;
    private DatabaseReference usersDatabaseReference;
    private FirebaseAuth firebaseAuth;

    private String current_user_id;

    private View mainView;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mainView = inflater.inflate(R.layout.fragment_friends, container, false);

        recyclerView_friendsList=mainView.findViewById(R.id.friends_list);

        recyclerView_friendsList.setHasFixedSize(true);
        recyclerView_friendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        firebaseAuth=FirebaseAuth.getInstance();
        current_user_id=firebaseAuth.getCurrentUser().getUid();

        friendsDatabaseReference= FirebaseDatabase.getInstance().getReference().child("Friends").child(current_user_id);
        friendsDatabaseReference.keepSynced(true);
        usersDatabaseReference=FirebaseDatabase.getInstance().getReference().child("Users");
        usersDatabaseReference.keepSynced(true);

        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends,FriendsViewHolder>friendsRecyclerAdapter=new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class,
                R.layout.users_single_layout,
                FriendsViewHolder.class,
                friendsDatabaseReference
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends friends, int position) {

                viewHolder.setDate(friends.getDate());

                final String list_user_id=getRef(position).getKey();

                usersDatabaseReference.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {


                        if (!list_user_id.equals(current_user_id)) {
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

                                CharSequence options[] = new CharSequence[]{"Open Profile", "Send message"};

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        if (which == 0) {

                                            Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                            profileIntent.putExtra("user_id", list_user_id);
                                            startActivity(profileIntent);

                                        }
                                        if (which == 1) {

                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("user_id", list_user_id);
                                            chatIntent.putExtra("user_displayName", user_displayName);
                                            startActivity(chatIntent);

                                        }

                                    }
                                });

                                builder.show();


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

        recyclerView_friendsList.setAdapter(friendsRecyclerAdapter);

    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);

            mView=itemView;

        }

        public void setDate(String date){

            TextView userStatusView=mView.findViewById(R.id.user_single_status);
            userStatusView.setVisibility(View.VISIBLE);
            userStatusView.setText(date);

        }

        public void setName(String name){

            TextView userNameView=mView.findViewById(R.id.user_single_displayName);
            userNameView.setVisibility(View.VISIBLE);
            userNameView.setText(name);

        }

        public void setImage(String thumb_image){

            CircleImageView userImageView=mView.findViewById(R.id.user_single_image);
            userImageView.setVisibility(View.VISIBLE);

            Picasso.get().load(thumb_image).placeholder(R.mipmap.default_avatar).into(userImageView);

        }

        public void setUserOnline(String online_status){

            ImageView userOnlineView=mView.findViewById(R.id.user_single_online);
            if(online_status.equals("true")){

                userOnlineView.setVisibility(View.VISIBLE);

            }
            else{

                userOnlineView.setVisibility(View.INVISIBLE);

            }


        }

    }

}
