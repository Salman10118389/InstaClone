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

public class LoginActivity extends AppCompatActivity {

    private EditText emailSignIn, passSignIn;
    private Button signInButton;
    private TextView textRedirectSignUp;
    private FirebaseAuth signInAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signInAuth = FirebaseAuth.getInstance();
        emailSignIn = findViewById(R.id.signUp_EmailSignIn);
        passSignIn = findViewById(R.id.singUp_PasswordSignIn);
        signInButton = findViewById(R.id.sign_in_button);

        textRedirectSignUp = findViewById(R.id.textViewSignUpRedirect);
        textRedirectSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email_signIn = emailSignIn.getText().toString();
                String pass_signIn = passSignIn.getText().toString();
                if (!email_signIn.isEmpty() && !pass_signIn.isEmpty()){
                    signInAuth.signInWithEmailAndPassword(email_signIn, pass_signIn).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(LoginActivity.this, "Login Successfull !", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            }else{
                                Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    Toast.makeText(LoginActivity.this, "No Empty Fields Allowed !", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}