package com.example.talks;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.talks.modal.Contacts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChatsFragment extends Fragment {

    private View chatsView;
    private RecyclerView chatsRecyclerView;

    private DatabaseReference chatRef,userRef;
    private FirebaseAuth auth;
    private String currentUserId;

    public ChatsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        chatRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        chatsView = inflater.inflate(R.layout.fragment_chats, container, false);
        chatsRecyclerView = chatsView.findViewById(R.id.chats_recycler_view);
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return chatsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatRef,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ChatsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull ChatsViewHolder holder, int position, @NonNull Contacts model) {

                        final String userId = getRef(position).getKey();
                        final String[] images = {"default_image"};

                        userRef.child(userId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    if (snapshot.hasChild("images")){
                                        images[0] = snapshot.child("images").getValue().toString();
                                        Glide.with(getContext()).load(images[0]).placeholder(R.drawable.ic_profile).into(holder.userImage);
                                    }
                                    String name = snapshot.child("name").getValue().toString();

                                    holder.userName.setText(name);
                                    holder.userStatus.setText("time");

                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(getContext(),ChatActivity.class);
                                            intent.putExtra("chat_user_id",userId);
                                            intent.putExtra("chat_user_name",name);
                                            intent.putExtra("chat_user_image", images[0]);
                                            startActivity(intent);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(getContext()).inflate(R.layout.user_find_friend_layout,parent,false);
                        ChatsViewHolder viewHolder = new ChatsViewHolder(view);
                        return viewHolder;
                    }
                };
        chatsRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder{

        TextView userName,userStatus;
        CircleImageView userImage;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.find_friend_user_name);
            userStatus = itemView.findViewById(R.id.find_friend_user_status);
            userImage = itemView.findViewById(R.id.find_friend_user_image);
        }
    }
}