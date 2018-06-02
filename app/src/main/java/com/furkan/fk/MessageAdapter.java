package com.furkan.fk;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by furkan on 30.05.2018.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> messagesList;

    private FirebaseAuth firebaseAuth;

    public MessageAdapter(List<Messages> messagesList) {

        this.messagesList = messagesList;

    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout, parent, false);

        return new MessageViewHolder(view);

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public TextView messageUserDisplayName;
        public CircleImageView messageUserImage;
        public TextView messageTime;
        public ImageView messageImage;

        public MessageViewHolder(View itemView) {
            super(itemView);

            messageText=itemView.findViewById(R.id.message_single_Text);
            messageUserDisplayName=itemView.findViewById(R.id.message_single_DisplayName);
            messageUserImage=itemView.findViewById(R.id.message_single_Image);
            messageTime=itemView.findViewById(R.id.message_single_Time);
            messageImage=itemView.findViewById(R.id.message_ImageMessage);

        }
    }

    public void onBindViewHolder(final MessageViewHolder holder, int position) {

        firebaseAuth = FirebaseAuth.getInstance();
        String current_user_id = firebaseAuth.getCurrentUser().getUid();

        Messages c = messagesList.get(position);
        String from_user = c.getFrom();
        String message_type=c.getType();


        GetTimeAgo getTimeAgo = new GetTimeAgo();
        long TIME=c.getTime();
        String lastSeenTime = getTimeAgo.getTimeAgo2(TIME);

        if (from_user.equals(current_user_id)) {



            if(!message_type.equals("text")){

                holder.messageText.setVisibility(View.INVISIBLE);
                holder.messageImage.setVisibility(View.VISIBLE);
                Picasso.get().load(c.getMessage()).placeholder(R.mipmap.default_avatar).into(holder.messageImage);

            }else{

                holder.messageImage.setVisibility(View.GONE);
                holder.messageText.setVisibility(View.VISIBLE);
                holder.messageText.setText(c.getMessage());

            }

            holder.messageText.setTextColor(Color.BLACK);
            holder.messageTime.setText(lastSeenTime);



            FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(current_user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String displayName=dataSnapshot.child("display_name").getValue().toString();
                            holder.messageUserDisplayName.setText(displayName);

                            String thumbImage=dataSnapshot.child("thumb_image").getValue().toString();

                            Picasso.get().load(thumbImage).placeholder(R.mipmap.default_avatar).into(holder.messageUserImage);



                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


        } else {


            if(!message_type.equals("text")){


                holder.messageText.setVisibility(View.INVISIBLE);
                holder.messageImage.setVisibility(View.VISIBLE);
                Picasso.get().load(c.getMessage()).placeholder(R.mipmap.default_avatar).into(holder.messageImage);

            }else{

                holder.messageText.setText(c.getMessage());
                holder.messageImage.setVisibility(View.GONE);
                holder.messageText.setVisibility(View.VISIBLE);

            }
            holder.messageText.setTextColor(Color.argb(255,3,147,166));
            holder.messageTime.setText(lastSeenTime);

            FirebaseDatabase.getInstance().getReference().child("Users").child(from_user).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    String displayName=dataSnapshot.child("display_name").getValue().toString();
                    holder.messageUserDisplayName.setText(displayName);

                    String thumbImage=dataSnapshot.child("thumb_image").getValue().toString();

                    Picasso.get().load(thumbImage).placeholder(R.mipmap.default_avatar).into(holder.messageUserImage);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }



    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }


}
/*if(from_user.equals(current_user_id)){

        holder.messageText.setBackgroundColor(Color.WHITE);
        holder.messageText.setTextColor(Color.BLACK);
        holder.messageDisplayName
        .setText(FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id).child("display_name").toString());

        }

        else{

        holder.messageText.setBackgroundResource(R.drawable.messages_text_background);
        holder.messageText.setTextColor(Color.WHITE);
        holder.messageDisplayName.setVisibility(View.INVISIBLE);

        }*/