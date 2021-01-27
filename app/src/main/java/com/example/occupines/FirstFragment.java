package com.example.occupines;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FirstFragment extends Fragment {

    private static final String TAG = "FirstFragment";

    private FirebaseFirestore db;
    private Utility utils;

    public FirstFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        utils = new Utility();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_first, container, false);

        ImageView userImage = view.findViewById(R.id.user);
        userImage.setOnClickListener(v -> setCurrentFragment(new ProfileFragment(), userImage));

        setImage(userImage);
        getData();

        return view;
    }

    private void getDocuments(List<String> list) {
        CollectionReference root = db.collection("properties");
        root.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                //Gets all document in root collection
                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                    list.add(document.getId());
                }
                Log.d(TAG, list.toString());
            } else {
                Log.d(TAG, "Error getting documents: ", task.getException());
            }
        });

    }

    private void getData() {
        List<String> list = new ArrayList<>();
        getDocuments(list);

        utils.showToast(getContext(), list.toString());
        for (int i = 0; i < list.size(); i++) {
            DocumentReference docRef = db.collection("cities").document(list.get(i));
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            });
        }
    }

    private void setCurrentFragment(Fragment fragment, ImageView userImage) {
        assert getFragmentManager() != null;
        getFragmentManager()
                .beginTransaction()
                .addSharedElement(userImage, "profile")
                .addToBackStack("FirstFragment")
                .replace(R.id.flFragment, fragment)
                .commit();
    }

    private void setImage(ImageView userImage) {
        Picasso.get().load(MainActivity.localFile)
                .noPlaceholder()
                .error(R.drawable.ic_user)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .centerCrop()
                .fit()
                .into(userImage);
    }

}