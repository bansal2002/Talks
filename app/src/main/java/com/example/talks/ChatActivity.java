package com.example.talks;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.talks.Adapters.MessageAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private String msgReceiverId,msgRecevierName,msgReceiverImage,senderUserId;

    private Toolbar toolbar;

    private TextView chatRecevierName,lastSeen;
    private CircleImageView chatReceiverImage;
    private RecyclerView chatRecyclerView;

    private ImageView sendMessage;
    private EditText messageEdittext;

    private FirebaseAuth auth;
    private DatabaseReference rootRef;

    private final List<messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        auth = FirebaseAuth.getInstance();
        senderUserId = auth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        toolbar = (Toolbar) findViewById(R.id.chat_appbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

//        chatRecevierName =(TextView) findViewById(R.id.custom_name);
//        lastSeen =(TextView) findViewById(R.id.custom_last_seen);
//        chatReceiverImage =(CircleImageView) findViewById(R.id.custom_image);

        messageEdittext = findViewById(R.id.gc_message_edittext);
        sendMessage = findViewById(R.id.gc_send_imageview);

        msgReceiverId = getIntent().getExtras().get("chat_user_id").toString();
        msgRecevierName = getIntent().getExtras().get("chat_user_name").toString();
        msgReceiverImage = getIntent().getExtras().get("chat_user_image").toString();

//        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View view = layoutInflater.inflate(R.layout.custom_chat_bar,null);
//        getSupportActionBar().setCustomView(view);

        getSupportActionBar().setTitle(msgRecevierName);

        messageAdapter = new MessageAdapter(messagesList);
        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        linearLayoutManager = new LinearLayoutManager(this);
        chatRecyclerView.setLayoutManager(linearLayoutManager);
        chatRecyclerView.setAdapter(messageAdapter);

//       chatRecevierName.setText(msgRecevierName);
//       Glide.with(getApplicationContext()).load(msgReceiverImage).placeholder(R.drawable.ic_profile).into(chatReceiverImage);

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        rootRef.child("Messages").child(senderUserId).child(msgReceiverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        messages messages = snapshot.getValue(com.example.talks.messages.class);

                        messagesList.add(messages);

                        messageAdapter.notifyDataSetChanged();

                        chatRecyclerView.smoothScrollToPosition(chatRecyclerView.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void SendMessage() {

        String message = messageEdittext.getText().toString();

        if (TextUtils.isEmpty(message)){
            Toast.makeText(getApplicationContext(),"Please enter message",Toast.LENGTH_SHORT).show();
        }else {
            String messageSenderRef = "Messages/"+senderUserId+"/"+msgReceiverId;
            String messageReceiverRef = "Messages/"+msgReceiverId+"/"+senderUserId;

            DatabaseReference userMsgKeyRef = rootRef.child("Messages").child(senderUserId)
                    .child(msgReceiverId).push();

            String msgPushId = userMsgKeyRef.getKey();

            Map msgTextBody = new HashMap();
            msgTextBody.put("message",message);
            msgTextBody.put("type","text");
            msgTextBody.put("from",senderUserId);

            Map msgBodyDetails = new HashMap();
            msgBodyDetails.put(messageSenderRef+"/"+msgPushId,msgTextBody);
            msgBodyDetails.put(messageReceiverRef+"/"+msgPushId,msgTextBody);

            rootRef.updateChildren(msgBodyDetails).addOnSuccessListener(new OnSuccessListener() {
                @Override
                public void onSuccess(Object o) {
                    messageEdittext.setText("");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_SHORT).show();
                    messageEdittext.setText("");
                }
            });
        }
    }
}