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
import java.util.List;
import java.util.Objects;

public class ThirdFragment extends Fragment {

    private static final String TAG = "ThirdFragment";
    private static final String COLLECTION = "users";

    private FirebaseUser user;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private LoadingDialog loadingDialog;

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> users;

    public ThirdFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = FirebaseAuth.getInstance().getCurrentUser();
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

        getUsers();

        userAdapter = new UserAdapter(users);
        recyclerView.setAdapter(userAdapter);
        return view;
    }

    private void getUsers() {
        loadingDialog.start();
        users.clear();
        db.collection(COLLECTION)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            if (!document.getId().equals(user.getUid())) {
                                String id = document.getId();
                                String username = document.getString("username");

                                StorageReference propertyImageRef = storageRef
                                        .child("images")
                                        .child(id)
                                        .child("profile");

                                try {
                                    File localFile = File.createTempFile(id, "jpg");
                                    Log.d(TAG, Uri.fromFile(localFile).toString());
                                    propertyImageRef.getFile(localFile).addOnCompleteListener(task1 -> {
                                        users.add(new User(id, username, localFile));
                                        userAdapter.notifyDataSetChanged();
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
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    @Override
    public void onDestroyView() {
        users.clear();
        userAdapter = null;
        recyclerView.setAdapter(null);
        super.onDestroyView();
    }
}