package com.example.occupines;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "Login: ";
    private FirebaseAuth mAuth;
    private Utility utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        utils = new Utility();

        buttonEvents();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void buttonEvents() {
        Button signIn = findViewById(R.id.signIn);
        Button signUp = findViewById(R.id.signUp);

        signIn.setOnClickListener(v -> {
            utils.toggleButton(signIn);
            utils.toggleButton(signUp);

            String email = ((EditText) findViewById(R.id.email)).getText().toString();
            String password = ((EditText) findViewById(R.id.password)).getText().toString();

            if (utils.checkInputs(email, password)) {
                signIn(email, password);
            } else {
                utils.showToast(LoginActivity.this, "Some fields are empty.");
            }

            utils.toggleButton(signIn);
            utils.toggleButton(signUp);
        });

        signUp.setOnClickListener(v -> {
            utils.toggleButton(signUp);
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            utils.toggleButton(signUp);
        });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");
                        utils.showToast(LoginActivity.this, "Login success.");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        utils.showToast(LoginActivity.this, "Authentication failed.");
                        updateUI(null);
                    }
                });
    }
}