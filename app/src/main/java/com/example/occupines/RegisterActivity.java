package com.example.occupines;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "Register: ";
    private FirebaseAuth mAuth;
    private Utility utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        utils = new Utility();

        buttonEvents();
    }

    private void buttonEvents() {
        Button signUp = findViewById(R.id.signUp2);
        signUp.setOnClickListener(v -> {
            utils.toggleButton(signUp);

            String name = ((EditText) findViewById(R.id.editTextFullName)).getText().toString();
            String email = ((EditText) findViewById(R.id.email)).getText().toString();
            String password = ((EditText) findViewById(R.id.password)).getText().toString();
            String rePassword = ((EditText) findViewById(R.id.rePassword)).getText().toString();

            if (utils.checkInputs(name, email, password, rePassword)) {
                if (utils.matchPassword(password, rePassword)) {
                    signUp(email, password);
                } else {
                    utils.showToast(RegisterActivity.this, "Password does not match.");
                }
            } else {
                utils.showToast(RegisterActivity.this, "Some fields are empty.");
            }

            utils.toggleButton(signUp);
        });
    }

    private void updateUI(FirebaseUser user) {
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    protected void updateName(FirebaseUser user) {
        EditText fullName = findViewById(R.id.editTextFullName);

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(fullName.getText().toString()).build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User profile updated.");
                    }
                });
    }

    private void signUp(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success");
                        utils.showToast(RegisterActivity.this, "You can now sign in.");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateName(user);
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        utils.showToast(RegisterActivity.this, "Authentication failed.");
                        updateUI(null);
                    }
                });
    }
}