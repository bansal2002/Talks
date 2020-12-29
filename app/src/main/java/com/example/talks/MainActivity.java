package com.example.talks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.talks.Adapters.TabsAccesserAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private TabsAccesserAdapter tabsAccesserAdapter;
    private FirebaseUser currentUser;
    private FirebaseAuth auth;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(toolbar);

        viewPager = findViewById(R.id.main_tabs_Pager);
        tabsAccesserAdapter = new TabsAccesserAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabsAccesserAdapter);

        tabLayout = findViewById(R.id.main_tabs);
        tabLayout.setupWithViewPager(viewPager);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        rootRef = FirebaseDatabase.getInstance().getReference();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(currentUser == null){
            sendUserToLoginAtivity();
        }else {
            verfyUserExistence();
        }
    }

    private void verfyUserExistence() {
        String currentUserId = auth.getCurrentUser().getUid();

        rootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.child("name").exists()){
                    Toast.makeText(MainActivity.this,"Welcome",Toast.LENGTH_SHORT).show();
                }else {
                    sendUserToSattingsAtivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.option_menu,menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()){
            case R.id.find_friend_option:
                sendUserToFindFriendActivity();
                break;
            case R.id.setting_option:
                sendUserToSattingsAtivity();
                break;
            case R.id.create_group_option:
                requestNewGroup();
                break;
            case R.id.logout_option:
                auth.signOut();
                sendUserToLoginAtivity();
                break;
            case R.id.request_option:
                senduserToRequestActivity();
                break;
        }
        return true;
    }

    private void senduserToRequestActivity() {
        Intent intent = new Intent(MainActivity.this,RequestActivity.class);
        startActivity(intent);
    }


    private void requestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Enter group name :");

        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("harami group");
        
        builder.setCancelable(false);
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = groupNameField.getText().toString();

                if (TextUtils.isEmpty(groupName)){
                    Toast.makeText(getApplicationContext(),"Enter group name",Toast.LENGTH_SHORT).show();
                }else {
                    createNewGroup(groupName);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void createNewGroup(String groupName) {

        rootRef.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(getApplicationContext(),"Group created successfully",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendUserToLoginAtivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void sendUserToSattingsAtivity() {
        Intent intent = new Intent(MainActivity.this, SattingsActivity.class);
        startActivity(intent);
    }

    private void sendUserToFindFriendActivity() {
        Intent intent = new Intent(MainActivity.this, FindFriendActivity.class);
        startActivity(intent);
    }
}