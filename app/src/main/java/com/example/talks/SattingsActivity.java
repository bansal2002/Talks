package com.example.talks;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Objects;

public class SattingsActivity extends AppCompatActivity {

    Button profileSubmit;
    EditText profileUsername,profileStatus;
    CircleImageView profileImage;

    private String currentUserId;
    private FirebaseAuth auth;
    private DatabaseReference rootRef;

    private Uri imageUri;
    private String imageUrl;

    private static final int GALLERY_PICK = 1;
    private StorageReference userProfileImageRef;
    private ProgressDialog progressDialog;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sattings);

        toolbar = (Toolbar) findViewById(R.id.settings_app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Profile setting");

        profileUsername = findViewById(R.id.profile_username);
        profileStatus = findViewById(R.id.profile_user_status);
        profileImage = findViewById(R.id.profile_image);
        profileSubmit = findViewById(R.id.profile_submit_button);

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile image");

        progressDialog = new ProgressDialog(this);

        profileSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSattings();
            }
        });

        profileUsername.setVisibility(View.INVISIBLE);

        RetriveUserProfileInfo();

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_PICK);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null){
            imageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK){

                progressDialog.setTitle("Uploading Image");
                progressDialog.setMessage("Please wait...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                Uri resultUri = result.getUri();

                StorageReference filePath = userProfileImageRef.child(currentUserId+".jpg");

                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                imageUrl = uri.toString();

                                rootRef.child("Users").child(currentUserId).child("images").setValue(imageUrl)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(getApplicationContext(),"image uplodad successfully",Toast.LENGTH_LONG).show();
                                                progressDialog.dismiss();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                });
                            }
                        });
                    }
                });
            }
        }
    }

    private void RetriveUserProfileInfo() {
        rootRef.child("Users").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.hasChild("name") && snapshot.hasChild("images")){
                            String retriveUsername = snapshot.child("name").getValue().toString();
                            String retriveStatus = snapshot.child("status").getValue().toString();
                            String retriveImage = snapshot.child("images").getValue().toString();

                            profileUsername.setText(retriveUsername);
                            profileStatus.setText(retriveStatus);
                            Glide.with(getApplicationContext()).load(retriveImage).into(profileImage);
                        }else if(snapshot.exists() && snapshot.hasChild("name")){
                            String retriveUsername = snapshot.child("name").getValue().toString();
                            String retriveStatus = snapshot.child("status").getValue().toString();

                            profileUsername.setText(retriveUsername);
                            profileStatus.setText(retriveStatus);
                        }else {
                            profileUsername.setVisibility(View.VISIBLE);
                            Toast.makeText(getApplicationContext(),"Enter profile details...",Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void updateSattings() {

        String username = profileUsername.getText().toString();
        String status = profileStatus.getText().toString();

        if (TextUtils.isEmpty(username)){
            Toast.makeText(this,"username not entered",Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(status)){
            Toast.makeText(this,"status enter entered",Toast.LENGTH_SHORT).show();
        }else {

            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("uId",currentUserId);
            hashMap.put("name",username);
            hashMap.put("status",status);
            rootRef.child("Users").child(currentUserId).updateChildren(hashMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                sendUserToMainActivity();
                                Toast.makeText(getApplicationContext(),"profile successfully added",Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void sendUserToMainActivity(){
        Intent intent = new Intent(SattingsActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}