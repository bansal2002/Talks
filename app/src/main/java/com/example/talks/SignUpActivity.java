package com.example.talks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    Button createAccountButton;
    EditText signUpEmail,signUpPassword;
    TextView alreadyHaveAccount;

    private FirebaseAuth auth;
    private DatabaseReference rootRef;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        createAccountButton = findViewById(R.id.sign_up_button);
        signUpEmail = findViewById(R.id.sign_up_email);
        signUpPassword = findViewById(R.id.sign_up_password);
        alreadyHaveAccount = findViewById(R.id.already_have_account);

        progressDialog = new ProgressDialog(this);

        auth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        alreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToLoginActivity();
            }
        });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });
    }

    private void createNewAccount() {
        String email = signUpEmail.getText().toString();
        String password = signUpPassword.getText().toString();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this,"Email not entered",Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please enter password",Toast.LENGTH_SHORT).show();
        }else {

            progressDialog.setTitle("Creating new account");
            progressDialog.setMessage("Wait,while account is created");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            auth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){

                                String currentUserId = auth.getCurrentUser().getUid();
                                rootRef.child("Users").child(currentUserId).setValue("");

                                sendUserToMainActivity();
                                Toast.makeText(SignUpActivity.this,"Account created successfully",Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }else {
                                String message = task.getException().toString();
                                Toast.makeText(SignUpActivity.this,"Something went wrong",Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }
                    });
        }
    }

    private void sendUserToLoginActivity() {
        Intent intent = new Intent(SignUpActivity.this,LoginActivity.class);
        startActivity(intent);
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(SignUpActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}