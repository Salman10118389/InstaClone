package com.example.instaclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    private EditText emailSignUp, passwordSignUp;
    private Button signUpButton;
    private TextView signInText;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //get Connected FIrebase
        auth = FirebaseAuth.getInstance();

        //Connect with XML
        emailSignUp = findViewById(R.id.signUp_Email);
        passwordSignUp = findViewById(R.id.singUp_Password);
        signUpButton = findViewById(R.id.sign_in_button);
        signInText = findViewById(R.id.sign_in_text);

        signInText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, SetUpActivity.class));
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailSignUp.getText().toString();
                String password = passwordSignUp.getText().toString();

                if (!email.isEmpty() && !password.isEmpty()){
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(SignUpActivity.this, "Register Successfull", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SignUpActivity.this, SetUpActivity.class));
                                finish();
                            }else{
                                Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    Toast.makeText(SignUpActivity.this, "Please Enter Your Email & Password !", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}