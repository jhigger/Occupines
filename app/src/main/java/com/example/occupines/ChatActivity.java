package com.example.occupines;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private LoadingDialog loadingDialog;

    private TextView username;
    private ImageView userImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        loadingDialog = new LoadingDialog(this);

        username = findViewById(R.id.username);
        userImage = findViewById(R.id.userImage);

        Bundle extras = getIntent().getExtras();
        String userId = "";
        if (extras != null) userId = extras.getString("id");

        getUserInfo(userId);

        ImageButton sendButton = findViewById(R.id.sendButton);
        String finalUserId = userId;
        sendButton.setOnClickListener(v -> {
            EditText chatMessage = findViewById(R.id.chatMessage);
            String message = chatMessage.getText().toString();
            if (!message.trim().isEmpty()) {
                sendMessage(finalUserId, message.trim());
            } else Utility.showToast(this, "Empty message not allowed");
            chatMessage.setText("");
        });
    }

    private void getUserInfo(String userId) {
        loadingDialog.start();
        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        assert document != null;
                        if (document.exists()) {
                            String documentId = document.getId();
                            StorageReference profileImageRef = storageRef
                                    .child("images")
                                    .child(documentId)
                                    .child("profile");

                            try {
                                File localFile = File.createTempFile(documentId, "jpg");
                                Log.d(TAG, Uri.fromFile(localFile).toString());
                                profileImageRef.getFile(localFile).addOnCompleteListener(task1 -> {
                                    Picasso.get().load(localFile)
                                            .placeholder(R.drawable.ic_user)
                                            .error(R.drawable.ic_user)
                                            .priority(Picasso.Priority.HIGH)
                                            .networkPolicy(NetworkPolicy.OFFLINE)
                                            .centerInside()
                                            .fit()
                                            .into(userImage);
                                    username.setText(document.getString("username"));
                                    loadingDialog.dismiss();
                                });
                                if (localFile.delete()) {
                                    Log.d(TAG, "Temp file deleted");
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    private void sendMessage(String receiver, String msg) {
        Map<String, Object> message = new HashMap<>();
        message.put("sender", currentUser.getUid());
        message.put("receiver", receiver);
        message.put("message", msg);
        message.put("createdAt", FieldValue.serverTimestamp());

        db.collection("messages").document()
                .set(message)
                .addOnSuccessListener(aVoid -> Utility.showToast(this, "Message sent"))
                .addOnFailureListener(e -> {
                    Utility.showToast(this, "Error: Submission failed");
                    Log.w(TAG, "Error writing document", e);
                });
    }
}