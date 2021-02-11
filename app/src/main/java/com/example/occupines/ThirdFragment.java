package com.example.occupines;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ThirdFragment extends Fragment {

    private static final String TAG = "ThirdFragment";
    private static final String COLLECTION = "messages";

    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private LoadingDialog loadingDialog;

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> users;
    private Set<String> usersList;

    public ThirdFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        loadingDialog = new LoadingDialog(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_third, container, false);
        recyclerView = view.findViewById(R.id.messagesRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        users = new ArrayList<>();
        usersList = new LinkedHashSet<>();

        getMessages();

        userAdapter = new UserAdapter(users);
        recyclerView.setAdapter(userAdapter);
        return view;
    }

    private void getMessages() {
        loadingDialog.start();
        usersList.clear();

        db.collection(COLLECTION)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            Chat chat = new Chat(
                                    document.getString("sender"),
                                    document.getString("receiver"),
                                    document.getString("message")
                            );

                            if (chat.getReceiver().equals(currentUser.getUid())) {
                                usersList.add(chat.getSender());
                            }
                            if (chat.getSender().equals(currentUser.getUid())) {
                                usersList.add(chat.getReceiver());
                            }

                            Log.d(TAG, "usersList: " + usersList.size());
                            getUserInfo();
                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    private void getUserInfo() {
        users.clear();

        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                    if (document.exists()) {
                        String userId = document.getId();
                        String username = document.getString("username");

                        StorageReference profileImageRef = storageRef
                                .child("images")
                                .child(userId)
                                .child("profile");

                        try {
                            File localFile = File.createTempFile(userId, "jpg");
                            Log.d(TAG, Uri.fromFile(localFile).toString());
                            profileImageRef.getFile(localFile).addOnCompleteListener(task1 -> {
                                User newUser = new User(userId, username, localFile);
                                if (users.size() != usersList.size()) {
                                    for (String id : usersList) {
                                        if (newUser.getId().equals(id) && !users.contains(newUser)) {
                                            users.add(newUser);
                                            Log.d(TAG, "users: " + users.size());
                                        }
                                    }
                                }

                                if (userAdapter != null) userAdapter.notifyDataSetChanged();
                                loadingDialog.dismiss();
                            });
                            if (localFile.delete()) {
                                Log.d(TAG, "Temp file deleted");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        userAdapter = null;
        recyclerView.setAdapter(null);
        super.onDestroyView();
    }
}