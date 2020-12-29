package com.example.talks;

import android.content.Intent;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.talks.modal.Contacts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class RequestActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView requestRecyclerView;

    private DatabaseReference requestRef,usersRef,contactsRef;
    private FirebaseAuth auth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        requestRef = FirebaseDatabase.getInstance().getReference().child("chatRequest");
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        toolbar = (Toolbar) findViewById(R.id.request_app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Requests");

        requestRecyclerView = findViewById(R.id.request_recycler_view);
        requestRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(requestRef.child(currentUserId),Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, RequestActivity.RequestViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, RequestActivity.RequestViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull RequestActivity.RequestViewHolder holder, int position, @NonNull Contacts model) {
                        holder.acceptRequest.setVisibility(View.VISIBLE);
                        holder.cancelRequest.setVisibility(View.VISIBLE);

                        final String requestUserKay = getRef(position).getKey();
                        DatabaseReference getTypeRef = getRef(position).child("requestType").getRef();

                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    String type = snapshot.getValue().toString();

                                    if (type.equals("received")){
                                        usersRef.child(requestUserKay).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.hasChild("images")){
                                                    String contactImage = snapshot.child("images").getValue().toString();
                                                    String contactName = snapshot.child("name").getValue().toString();
                                                   // String contactStatus = snapshot.child("status").getValue().toString();

                                                    holder.userName.setText(contactName);
                                                    holder.userStatus.setText("send a friend request");
                                                    Glide.with(getApplicationContext()).load(contactImage).placeholder(R.drawable.ic_profile).into(holder.userImage);
                                                }else {
                                                    String contactName = snapshot.child("name").getValue().toString();
                                                   // String contactStatus = snapshot.child("status").getValue().toString();

                                                    holder.userName.setText(contactName);
                                                    holder.userStatus.setText("send a friend request");
                                                }

                                                holder.acceptRequest.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        contactsRef.child(currentUserId).child(requestUserKay).child("Contacts")
                                                                .setValue("saved").addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                contactsRef.child(requestUserKay).child(currentUserId).child("Contacts")
                                                                        .setValue("saved").addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        requestRef.child(currentUserId).child(requestUserKay)
                                                                                .removeValue()
                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {
                                                                                        requestRef.child(requestUserKay).child(currentUserId)
                                                                                                .removeValue()
                                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onSuccess(Void aVoid) {

                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                });
                                                                    }
                                                                });
                                                            }
                                                        });
                                                    }
                                                });

                                                holder.cancelRequest.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        requestRef.child(currentUserId).child(requestUserKay)
                                                                .removeValue()
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        requestRef.child(requestUserKay).child(currentUserId)
                                                                                .removeValue()
                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {

                                                                                    }
                                                                                });
                                                                    }
                                                                });
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public RequestActivity.RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.user_find_friend_layout,parent,false);
                        RequestActivity.RequestViewHolder viewHolder = new RequestActivity.RequestViewHolder(view);
                        return viewHolder;
                    }
                };
        requestRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder{

        TextView userName,userStatus;
        CircleImageView userImage;
        Button acceptRequest,cancelRequest;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.find_friend_user_name);
            userStatus = itemView.findViewById(R.id.find_friend_user_status);
            userImage = itemView.findViewById(R.id.find_friend_user_image);
            acceptRequest = itemView.findViewById(R.id.accept_button);
            cancelRequest = itemView.findViewById(R.id.cancel_button);
        }
    }
}