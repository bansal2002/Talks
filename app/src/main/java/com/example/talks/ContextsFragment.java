package com.example.talks;

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

public class ContextsFragment extends Fragment {

    private View contactsView;
    private RecyclerView contactsRecyclerView;

    private DatabaseReference contactsRef,userRef;
    private FirebaseAuth auth;
    private String currentUserId;


    public ContextsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        contactsView = inflater.inflate(R.layout.fragment_contexts, container, false);

        contactsRecyclerView = contactsView.findViewById(R.id.contact_recycler_view);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        return contactsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ContactsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull ContactsViewHolder holder, int position, @NonNull Contacts model) {

                        String contactsUserId = getRef(position).getKey();

                        userRef.child(contactsUserId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.hasChild("images")){
                                    String contactImage = snapshot.child("images").getValue().toString();
                                    String contactName = snapshot.child("name").getValue().toString();
                                    String contactStatus = snapshot.child("status").getValue().toString();

                                    holder.userName.setText(contactName);
                                    holder.userStatus.setText(contactStatus);
                                    Glide.with(getContext()).load(contactImage).placeholder(R.drawable.ic_profile).into(holder.userImage);
                                }else {
                                    String contactName = snapshot.child("name").getValue().toString();
                                    String contactStatus = snapshot.child("status").getValue().toString();

                                    holder.userName.setText(contactName);
                                    holder.userStatus.setText(contactStatus);
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_find_friend_layout,parent,false);
                        ContactsViewHolder contactsViewHolder = new ContactsViewHolder(view);
                        return contactsViewHolder;
                    }
                };
        contactsRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder{

        TextView userName,userStatus;
        CircleImageView userImage;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.find_friend_user_name);
            userStatus = itemView.findViewById(R.id.find_friend_user_status);
            userImage = itemView.findViewById(R.id.find_friend_user_image);
        }
    }
}