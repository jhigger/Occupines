package com.example.occupines;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class ListFragment extends Fragment {

    private static final String TAG = "ListFragment";
    private final String COLLECTION = "properties";

    private FirebaseFirestore db;
    private StorageReference storageRef;
    private LoadingDialog loadingDialog;

    private MyListAdapter mAdapter;
    private ArrayList<PropertyPost> itemsData;

    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        loadingDialog = new LoadingDialog(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        // 1. get a reference to recyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        // 2. set layoutManger
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        // this is data for recycler view
        itemsData = new ArrayList<>();
        getDocuments();
        // 3. create an adapter
        mAdapter = new MyListAdapter(itemsData);
        // 4. set adapter
        recyclerView.setAdapter(mAdapter);
        // 5. set item animator to DefaultAnimator
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        return view;
    }

    private void getDocuments() {
        loadingDialog.start();
        db.collection(COLLECTION)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            getData(document.getId());
                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    private void getData(String documentId) {
        db.collection(COLLECTION).document(documentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        assert document != null;
                        if (document.exists()) {
                            StorageReference propertyImageRef = storageRef
                                    .child("images")
                                    .child(documentId)
                                    .child("property");

                            try {
                                File localFile = File.createTempFile(documentId, "jpg");
                                propertyImageRef.getFile(localFile).addOnCompleteListener(task1 -> {
                                    //noinspection ConstantConditions
                                    PropertyPost propertyPost = new PropertyPost(
                                            localFile,
                                            document.getString("type"),
                                            document.getDouble("price"),
                                            document.getString("location"),
                                            document.getString("owner"),
                                            document.getString("info"));

                                    itemsData.add(propertyPost);
                                    mAdapter.notifyDataSetChanged();
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                    loadingDialog.dismiss();
                });
    }
}