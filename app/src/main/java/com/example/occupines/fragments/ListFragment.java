package com.example.occupines.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.occupines.LoadingDialog;
import com.example.occupines.R;
import com.example.occupines.adapters.MyListAdapter;
import com.example.occupines.models.Property;
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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class ListFragment extends Fragment {

    private static final String TAG = "ListFragment";

    private FirebaseUser user;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private LoadingDialog loadingDialog;

    private TextView noPosts;
    private RecyclerView recyclerView;
    private MyListAdapter mAdapter;
    private ArrayList<Property> itemsData;

    public ListFragment() {
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
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        noPosts = view.findViewById(R.id.noPosts);
        // 1. get a reference to recyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        // 2. set layoutManger
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        // this is data for recycler view
        itemsData = new ArrayList<>();
        getDocuments();
        // 3. create an adapter
        mAdapter = new MyListAdapter(itemsData, user.getUid());
        // 4. set adapter
        recyclerView.setAdapter(mAdapter);
        // 5. set item animator to DefaultAnimator
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        return view;
    }

    private void getDocuments() {
        loadingDialog.start();
        itemsData.clear();
        Query query = db.collection("properties");

        if (FirstFragment.checked.isEmpty()) {
            query = query.orderBy("createdAt", Query.Direction.ASCENDING);
        } else {
            if (FirstFragment.checked.contains("House")) {
                query = query.whereEqualTo("type", "House");
            } else if (FirstFragment.checked.contains("Apartment")) {
                query = query.whereEqualTo("type", "Apartment");
            } else if (FirstFragment.checked.contains("Boarding")) {
                query = query.whereEqualTo("type", "Boarding");
            }

            if (FirstFragment.checked.contains("Ascending")) {
                query = query.orderBy("price", Query.Direction.ASCENDING);
            } else if (FirstFragment.checked.contains("Descending")) {
                query = query.orderBy("price", Query.Direction.DESCENDING);
            }
        }

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (itemsData.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    noPosts.setVisibility(View.VISIBLE);
                }
                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                    if (document.exists()) {
                        String documentId = document.getId();
                        String userId = documentId.substring(0, documentId.length() - 2);
                        int num = Integer.parseInt(documentId.substring(documentId.length() - 1));

                        AtomicReference<StorageReference> propertyImageRef = new AtomicReference<>(storageRef
                                .child("images")
                                .child(userId)
                                .child("property" + num)
                                .child("image1"));

                        try {
                            final Property[] propertyPost = new Property[1];
                            File imageFile1 = File.createTempFile(userId, "jpg");
                            propertyImageRef.get().getFile(imageFile1).addOnCompleteListener(task1 -> {
                                propertyPost[0] = new Property(
                                        imageFile1,
                                        document.getString("type"),
                                        Objects.requireNonNull(document.getDouble("price")),
                                        document.getString("location"),
                                        document.getString("owner"),
                                        document.getString("info"),
                                        documentId);

                                propertyImageRef.set(storageRef
                                        .child("images")
                                        .child(userId)
                                        .child("property" + num)
                                        .child("image2"));

                                File imageFile2 = null;
                                File imageFile3 = null;
                                File imageFile4 = null;
                                try {
                                    imageFile2 = File.createTempFile(userId, "jpg");
                                    imageFile3 = File.createTempFile(userId, "jpg");
                                    imageFile4 = File.createTempFile(userId, "jpg");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                File finalImageFile = imageFile2;
                                File finalImageFile3 = imageFile3;
                                File finalImageFile1 = imageFile4;
                                propertyImageRef.get().getFile(imageFile2).addOnCompleteListener(task2 -> {
                                    propertyPost[0].setImageFile2(finalImageFile);

                                    propertyImageRef.set(storageRef
                                            .child("images")
                                            .child(userId)
                                            .child("property" + num)
                                            .child("image3"));

                                    propertyImageRef.get().getFile(finalImageFile3).addOnCompleteListener(task3 -> {
                                        propertyPost[0].setImageFile3(finalImageFile3);

                                        propertyImageRef.set(storageRef
                                                .child("images")
                                                .child(userId)
                                                .child("property" + num)
                                                .child("image4"));

                                        propertyImageRef.get().getFile(finalImageFile1).addOnCompleteListener(task4 -> {
                                            propertyPost[0].setImageFile4(finalImageFile1);

                                            if (!FirstFragment.location.isEmpty()) {
                                                if (propertyPost[0].getLocation().toLowerCase().contains(FirstFragment.location.toLowerCase())) {
                                                    itemsData.add(propertyPost[0]);
                                                }
                                            } else {
                                                itemsData.add(propertyPost[0]);
                                            }
                                            recyclerView.setVisibility(View.VISIBLE);
                                            noPosts.setVisibility(View.GONE);
                                            loadingDialog.dismiss();
                                            if (mAdapter != null) mAdapter.notifyDataSetChanged();
                                        });

                                        if (finalImageFile1.delete())
                                            Log.d(TAG, "Temp file deleted");

                                    });

                                    if (finalImageFile3.delete()) Log.d(TAG, "Temp file deleted");
                                });
                                if (imageFile2.delete()) Log.d(TAG, "Temp file deleted");
                            });
                            if (imageFile1.delete()) Log.d(TAG, "Temp file deleted");

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.d(TAG, "No such document");
                        if (itemsData.isEmpty()) {
                            recyclerView.setVisibility(View.GONE);
                            noPosts.setVisibility(View.VISIBLE);
                            loadingDialog.dismiss();
                        }
                    }
                }
            } else {
                Log.w(TAG, "Error getting documents.", task.getException());
                loadingDialog.dismiss();
            }
        });
    }

    @Override
    public void onDestroyView() {
        mAdapter = null;
        recyclerView.setAdapter(null);
        super.onDestroyView();
    }
}