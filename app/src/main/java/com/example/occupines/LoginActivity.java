package com.example.occupines;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private FirebaseAuth mAuth;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Utility.removeBlinkOnTransition(LoginActivity.this);

        mAuth = FirebaseAuth.getInstance();
        loadingDialog = new LoadingDialog(LoginActivity.this);

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
            Utility.toggleButton(signIn);
            Utility.toggleButton(signUp);

            String email = ((EditText) findViewById(R.id.email)).getText().toString();
            String password = ((EditText) findViewById(R.id.password)).getText().toString();

            if (Utility.checkInputs(email, password)) {
                signIn(email, password);
            } else {
                Utility.showToast(LoginActivity.this, "Some fields are empty.");
            }

            Utility.toggleButton(signIn);
            Utility.toggleButton(signUp);
        });

        signUp.setOnClickListener(v -> {
            Utility.toggleButton(signUp);

            //Shared element transition
            ImageView imageView = findViewById(R.id.icon);
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(LoginActivity.this, imageView, "icon");
            startActivity(intent, options.toBundle());

            Utility.toggleButton(signUp);
        });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    private void signIn(String email, String password) {
        loadingDialog.start();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");
                        Utility.showToast(LoginActivity.this, "Login success.");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthInvalidUserException e) {
                            Utility.showToast(LoginActivity.this, "Authentication failed: User not found.");
                        } catch (Exception e) {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Utility.showToast(LoginActivity.this, "Authentication failed.");
                        }
                        updateUI(null);
                    }
                    loadingDialog.dismiss();
                });
    }
}