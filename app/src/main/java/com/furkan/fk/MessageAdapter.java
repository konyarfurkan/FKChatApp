package com.furkan.fk;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by furkan on 30.05.2018.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> messagesList;

    private FirebaseAuth firebaseAuth;

    public MessageAdapter(List<Messages> messagesList){

        this.messagesList=messagesList;

    }



    @Override
    public MessageViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout,parent,false);

        return new MessageViewHolder(view);


    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView messageText;
        public CircleImageView profileImage;

        public MessageViewHolder(View itemView) {
            super(itemView);

            messageText=itemView.findViewById(R.id.message_text_layout);
            profileImage=itemView.findViewById(R.id.message_profile_layout);

        }
    }

    public void onBindViewHolder(MessageViewHolder holder, int position) {

        firebaseAuth=FirebaseAuth.getInstance();
        String current_user_id=firebaseAuth.getCurrentUser().getUid();

        Messages c=messagesList.get(position);
        String from_user=c.getFrom();

        if(from_user.equals(current_user_id)){

            holder.messageText.setBackgroundColor(Color.WHITE);
            holder.messageText.setTextColor(Color.BLACK);

        }
        else{

            holder.messageText.setBackgroundResource(R.drawable.messages_text_background);
            holder.messageText.setTextColor(Color.WHITE);

        }

        holder.messageText.setText(c.getMessage());



    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }


}
