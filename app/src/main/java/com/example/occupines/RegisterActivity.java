package com.example.occupines;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Utility.removeBlinkOnTransition(RegisterActivity.this);

        buttonEvents();
    }

    private void buttonEvents() {
        Button signUp = findViewById(R.id.signUp2);
        signUp.setOnClickListener(v -> {
            Utility.toggleButton(signUp);

            String name = ((EditText) findViewById(R.id.editTextFullName)).getText().toString();
            String email = ((EditText) findViewById(R.id.email)).getText().toString();
            String password = ((EditText) findViewById(R.id.password)).getText().toString();
            String rePassword = ((EditText) findViewById(R.id.rePassword)).getText().toString();

            if (Utility.checkInputs(name, email, password, rePassword)) {
                if (Utility.doesMatch(password, rePassword)) {
                    signUp(email, password);
                } else {
                    Utility.showToast(RegisterActivity.this, "Password does not match.");
                }
            } else {
                Utility.showToast(RegisterActivity.this, "Some fields are empty.");
            }

            Utility.toggleButton(signUp);
        });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            EditText fullName = findViewById(R.id.editTextFullName);
            updateName(user, fullName.getText().toString());

            startActivity(new Intent(RegisterActivity.this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }

    private void updateName(FirebaseUser firebaseUser, String fullName) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(fullName).build();

        firebaseUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User profile updated.");
                    }
                });

        HashMap<String, Object> user = new HashMap<>();
        user.put("username", fullName);
        user.put("imageUrl", "default");
        db.collection("users").document(firebaseUser.getUid()).set(user);
    }

    private void signUp(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success");
                        Utility.showToast(RegisterActivity.this, "You are now signed in.");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        try {
                            throw Objects.requireNonNull(task.getException());
                        } catch (FirebaseAuthWeakPasswordException e) {
                            Utility.showToast(RegisterActivity.this, "Authentication failed: Weak Password.");
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            Utility.showToast(RegisterActivity.this, "Authentication failed: Invalid Email.");
                        } catch (FirebaseAuthUserCollisionException e) {
                            Utility.showToast(RegisterActivity.this, "Authentication failed: User already exists.");
                        } catch (Exception e) {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Utility.showToast(RegisterActivity.this, "Authentication failed.");
                        }
                        updateUI(null);
                    }
                });
    }
}