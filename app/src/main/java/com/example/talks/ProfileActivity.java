package com.example.talks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private String recevierUserId,senderUserId,currentState;
    private CircleImageView profileImage;
    TextView profileUsername,profileStatus;
    Button sendRequest,declineRequest;

    private DatabaseReference profileRef,chatRequestRef,contactsRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();

        profileRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("chatRequest");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        recevierUserId = getIntent().getExtras().get("visit_user_id").toString();

        profileImage = findViewById(R.id.user_profile_image);
        profileUsername = findViewById(R.id.user_profile_user_name);
        profileStatus = findViewById(R.id.user_profile_user_status);
        sendRequest = findViewById(R.id.profile_send_request);
        declineRequest = findViewById(R.id.profile_decline_request);

        currentState = "new";
        senderUserId = auth.getCurrentUser().getUid();

        RetriveUserInfo();
    }

    private void RetriveUserInfo() {

        profileRef.child(recevierUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("images")){
                    String userName = snapshot.child("name").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();
                    String userImage = snapshot.child("images").getValue().toString();

                    Glide.with(getApplicationContext()).load(userImage).placeholder(R.drawable.ic_profile).into(profileImage);
                    profileUsername.setText(userName);
                    profileStatus.setText(userStatus);

                    ManageChatRequest();
                }else {
                    String userName = snapshot.child("name").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();

                    profileUsername.setText(userName);
                    profileStatus.setText(userStatus);

                    ManageChatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void ManageChatRequest() {

        chatRequestRef.child(senderUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(recevierUserId)){
                            String requestType = snapshot.child(recevierUserId).child("requestType").getValue().toString();
                            if (requestType.equals("sent")){
                                currentState = "requestSent";
                                sendRequest.setText("cancel request");
                            }else if(requestType.equals("received")){
                                currentState = "request_received";
                                sendRequest.setText("accept request");
                                declineRequest.setVisibility(View.VISIBLE);
                                declineRequest.setEnabled(true);

                                declineRequest.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        cancelChatRequest();
                                    }
                                });
                            }
                        }else {
                            contactsRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.hasChild(recevierUserId)){
                                                currentState = "friends";
                                                sendRequest.setText("remove request");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        if (!senderUserId.equals(recevierUserId)){

            sendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendRequest.setEnabled(false);

                    if (currentState.equals("new")){
                        sentChatRequest();
                    }else if (currentState.equals("requestSent")){
                        cancelChatRequest();
                    }else if (currentState.equals("request_received")){
                        acceptChatRequest();
                    }else if (currentState.equals("friends")){
                        removeContact();
                    }
                }
            });

        }else {
            sendRequest.setVisibility(View.GONE);
        }
    }

    private void removeContact() {
        contactsRef.child(senderUserId).child(recevierUserId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        contactsRef.child(recevierUserId).child(senderUserId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        sendRequest.setEnabled(true);
                                        currentState = "new";
                                        sendRequest.setText("send request");

                                        declineRequest.setVisibility(View.GONE);
                                        declineRequest.setEnabled(false);
                                    }
                                });
                    }
                });
    }

    private void acceptChatRequest() {

        contactsRef.child(senderUserId).child(recevierUserId).child("Contacts")
                .setValue("saved")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        contactsRef.child(recevierUserId).child(senderUserId).child("Contacts")
                                .setValue("saved")
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        chatRequestRef.child(senderUserId).child(recevierUserId)
                                                .removeValue()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        chatRequestRef.child(recevierUserId).child(senderUserId)
                                                                .removeValue()
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        sendRequest.setEnabled(true);
                                                                        currentState = "friends";
                                                                        sendRequest.setText("remove request");

                                                                        declineRequest.setEnabled(false);
                                                                        declineRequest.setVisibility(View.GONE);
                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    private void cancelChatRequest() {
        chatRequestRef.child(senderUserId).child(recevierUserId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        chatRequestRef.child(recevierUserId).child(senderUserId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        sendRequest.setEnabled(true);
                                        currentState = "new";
                                        sendRequest.setText("send request");

                                        declineRequest.setVisibility(View.GONE);
                                        declineRequest.setEnabled(false);
                                    }
                                });
                    }
                });
    }

    private void sentChatRequest() {
        chatRequestRef.child(senderUserId).child(recevierUserId)
                .child("requestType").setValue("sent")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        chatRequestRef.child(recevierUserId).child(senderUserId)
                                .child("requestType").setValue("received")
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        sendRequest.setEnabled(true);
                                        currentState = "requestSent";
                                        sendRequest.setText("Cancel request");
                                    }
                                });
                    }
                });
    }
}