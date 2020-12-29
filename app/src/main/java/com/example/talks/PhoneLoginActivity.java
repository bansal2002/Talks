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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private EditText mobileNumber,verficationCode;
    private Button getOtp,verifyOtp;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private FirebaseAuth auth;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mobileNumber = findViewById(R.id.mobile_number);
        verficationCode = findViewById(R.id.verfication_code);
        getOtp = findViewById(R.id.get_verfication_code);
        verifyOtp = findViewById(R.id.verified_button);

        auth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);

        getOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String phoneNumber = mobileNumber.getText().toString();
                Toast.makeText(getApplicationContext(),phoneNumber,Toast.LENGTH_LONG).show();

                if (TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(PhoneLoginActivity.this,"Enter your number",Toast.LENGTH_SHORT);
                }else {

                    progressDialog.setTitle("Phone verification");
                    progressDialog.setMessage("Wait,while we are authenticate your number...");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                                    .setPhoneNumber("+91"+phoneNumber)       // Phone number to verify
                                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                    .setActivity(PhoneLoginActivity.this)                 // Activity (for callback binding)
                                    .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                                    .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                }
            }
        });

        verifyOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mobileNumber.setVisibility(View.GONE);
                getOtp.setVisibility(View.GONE);

                String verificationCode = verficationCode.getText().toString();

                if (TextUtils.isEmpty(verificationCode)){
                    Toast.makeText(PhoneLoginActivity.this,"Please enter code",Toast.LENGTH_SHORT);
                }else {

                    progressDialog.setTitle("Verification code");
                    progressDialog.setMessage("Wait,while we are verifing your code...");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId,verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

                progressDialog.dismiss();
                Toast.makeText(PhoneLoginActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();

                mobileNumber.setVisibility(View.VISIBLE);
                getOtp.setVisibility(View.VISIBLE);
                verficationCode.setVisibility(View.GONE);
                verifyOtp.setVisibility(View.GONE);

            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

                progressDialog.dismiss();
                Toast.makeText(PhoneLoginActivity.this,"verification code sent",Toast.LENGTH_SHORT).show();

                mVerificationId = verificationId;
                mResendToken = token;

                mobileNumber.setVisibility(View.GONE);
                getOtp.setVisibility(View.GONE);
                verficationCode.setVisibility(View.VISIBLE);
                verifyOtp.setVisibility(View.VISIBLE);

            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            Toast.makeText(PhoneLoginActivity.this,"Successfull",Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
                        } else {
                            progressDialog.dismiss();
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(PhoneLoginActivity.this,"Invalid",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(PhoneLoginActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

}