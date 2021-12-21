package com.recommender.physicalActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Register extends AppCompatActivity {

    EditText username,email,password;
    FirebaseAuth mAuth;
    AppCompatButton registerBtn;
    TextView loginText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        registerBtn = findViewById(R.id.registerBtn);
        loginText = findViewById(R.id.loginHereBtn);
        mAuth = FirebaseAuth.getInstance();

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRegisterClicked(view);
            }
        });

        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this, Login.class));
                finish();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        // if user logged in, go to sign-in screen
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, UserDetails.class));
            finish();
        }
    }

    public void onRegisterClicked(View view) {

        //Fetching data
        String emailInput = email.getText().toString().trim();
        String pass = password.getText().toString().trim();
        final String user = username.getText().toString().trim();

        //Validation check
        if (TextUtils.isEmpty(user)) {
            Toast.makeText(getApplicationContext(), "Enter username!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(emailInput)) {
            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(pass)) {
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            return;
        }

        //create user with email/password by adding complete listener
        mAuth.createUserWithEmailAndPassword(emailInput, pass)
            .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    Toast.makeText(Register.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();

                    // If sign-in fails, display a message to the user. If sign-in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful()) {
                        Toast.makeText(Register.this, "Authentication failed." + task.getException(),
                                Toast.LENGTH_LONG).show();
                        Log.e("MyTag", task.getException().toString());
                    } else {

                        startActivity(new Intent(Register.this, UserDetails.class));
                        finish();
                    }
                }
            });
    }
}