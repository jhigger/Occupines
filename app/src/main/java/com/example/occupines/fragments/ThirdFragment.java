package com.example.occupines.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.occupines.LoadingDialog;
import com.example.occupines.R;
import com.example.occupines.adapters.UserAdapter;
import com.example.occupines.models.Chat;
import com.example.occupines.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
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

    //Setup global variables
    private static final String TAG = "ThirdFragment";

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

    //Instantiate objects
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

    //Gets chat messages from firestore
    private void getMessages() {
        //Start loading animation
        loadingDialog.start();
        //Remove all elements of usersList
        usersList.clear();

        //Get users that you have chat from messages collection from firestore
        db.collection("messages")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        //Loop through the documents
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            //Get the chat data of a document
                            Chat chat = new Chat(
                                    document.getString("sender"),
                                    document.getString("receiver"),
                                    document.getString("message")
                            );

                            //If the receiver of the message is the current user
                            //Then add the message sender to usersList
                            if (chat.getReceiver().equals(currentUser.getUid())) {
                                usersList.add(chat.getSender());
                            }
                            //If the sender of the message is the current user
                            //Then add the message receiver to usersList
                            if (chat.getSender().equals(currentUser.getUid())) {
                                usersList.add(chat.getReceiver());
                            }
                            /*  NOTE: usersList uses a Set interface
                                      Set interface is an unordered collection or
                                      list in which duplicates are not allowed   */

                            Log.d(TAG, "usersList: " + usersList.size());
                            getUserInfo();
                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    //Gets user info from firestore
    private void getUserInfo() {
        //Remove all elements of users
        users.clear();

        //Get users info to show in recycler view
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                    if (document.exists()) {
                        //Get id of new user
                        String userId = document.getId();
                        //Get username of new user
                        String username = document.getString("username");
                        //Path to profile picture of user
                        StorageReference profileImageRef = storageRef
                                .child("images")
                                .child(userId)
                                .child("profile");
                        //Get profile picture of new user
                        try {
                            File localFile = File.createTempFile(userId, "jpg");
                            Log.d(TAG, Uri.fromFile(localFile).toString());
                            profileImageRef.getFile(localFile).addOnCompleteListener(task1 -> {
                                //Save new user object
                                User newUser = new User(userId, username, localFile);
                                //Check if users is the same size as usersList
                                if (users.size() != usersList.size()) {
                                    //Loop through usersList and check if new user is in usersList
                                    for (String id : usersList) {
                                        if (newUser.getId().equals(id) && !users.contains(newUser)) {
                                            //Add this new user in the recycler view
                                            users.add(newUser);
                                            Log.d(TAG, "users: " + users.size());
                                        }
                                    }
                                }
                                //Refresh userAdapter
                                if (userAdapter != null) userAdapter.notifyDataSetChanged();
                                //Stop loading animation
                                loadingDialog.dismiss();
                            });
                            //Delete temporary file
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
        //Set values to null to prevent memory leak
        userAdapter = null;
        recyclerView.setAdapter(null);
        super.onDestroyView();
    }
}