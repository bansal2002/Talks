package com.example.talks.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.talks.ChatActivity;
import com.example.talks.R;
import com.example.talks.messages;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<messages> userMsgList;
    private FirebaseAuth auth;
    private DatabaseReference userRef;

    public MessageAdapter(List<messages> userMsgList){
        this.userMsgList = userMsgList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_msg_layout,parent,false);

        auth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

        String senderMsgId = auth.getCurrentUser().getUid();
        messages messages = userMsgList.get(position);

        String fromUserId = messages.getFrom();
        String typeMsg = messages.getType();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild("images")){
                    String receiverImage = snapshot.child("images").getValue().toString();

                    Glide.with(holder.itemView).load(receiverImage).placeholder(R.drawable.ic_profile).into(holder.messageReceiverImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (typeMsg.equals("text")){
            holder.receiverMsg.setVisibility(View.GONE);
            holder.messageReceiverImage.setVisibility(View.GONE);
            holder.senderMsg.setVisibility(View.GONE);

            if (fromUserId.equals(senderMsgId)){
                holder.senderMsg.setVisibility(View.VISIBLE);
                holder.senderMsg.setBackgroundResource(R.drawable.sender_msg_layout);
                holder.senderMsg.setText(messages.getMessage());

            }else {

                holder.receiverMsg.setVisibility(View.VISIBLE);
                holder.messageReceiverImage.setVisibility(View.VISIBLE);

                holder.receiverMsg.setBackgroundResource(R.drawable.reseiver_msg_layout);
                holder.receiverMsg.setText(messages.getMessage());
            }
        }

    }

    @Override
    public int getItemCount() {
        return userMsgList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public CircleImageView messageReceiverImage;
        public TextView senderMsg,receiverMsg;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageReceiverImage = itemView.findViewById(R.id.chat_message_profile_image);
            senderMsg = itemView.findViewById(R.id.sender_msg);
            receiverMsg = itemView.findViewById(R.id.receiver_msg);
        }
    }
}
