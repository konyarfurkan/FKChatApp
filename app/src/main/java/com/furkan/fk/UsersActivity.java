package com.furkan.fk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView_usersList;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private String current_user_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        toolbar=findViewById(R.id.users_appBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        recyclerView_usersList=findViewById(R.id.users_list);

        recyclerView_usersList.setHasFixedSize(true);
        recyclerView_usersList.setLayoutManager(new LinearLayoutManager(this));

        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users");

        firebaseAuth=FirebaseAuth.getInstance();
        current_user_id=firebaseAuth.getCurrentUser().getUid();

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Users,UsersViewHolder>firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Users, UsersViewHolder>(

                Users.class,
                R.layout.users_single_layout,
                UsersViewHolder.class,
                databaseReference

        ) {
            @Override
            protected void populateViewHolder(final UsersViewHolder viewHolder, final Users model, int position) {



                final String user_id=getRef(position).getKey();

                FirebaseDatabase.getInstance().getReference().child("Users").child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (!user_id.equals(current_user_id)) {

                        if(dataSnapshot.hasChild("online")){

                            viewHolder.setName(model.getDisplay_name());
                            viewHolder.setStatus(model.getStatus());
                            viewHolder.setImage(model.getThumb_image());
                            String userOnline=dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnline(userOnline);

                        }

                    }}

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent profileIntent=new Intent(UsersActivity.this,ProfileActivity.class);
                        profileIntent.putExtra("user_id",user_id);
                        startActivity(profileIntent);

                    }
                });

            }
        };

        recyclerView_usersList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);

            mView=itemView;

        }

        public void setName(String name){

            TextView userNameView=mView.findViewById(R.id.user_single_displayName);
            userNameView.setVisibility(View.VISIBLE);
            userNameView.setText(name);

        }

        public void setStatus(String status){

            TextView userStatusView=mView.findViewById(R.id.user_single_status);
            userStatusView.setVisibility(View.VISIBLE);
            userStatusView.setText(status);

        }

        public void setImage(final String thumb_image){

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
