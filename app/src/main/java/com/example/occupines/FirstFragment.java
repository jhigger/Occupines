package com.example.occupines;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class FirstFragment extends Fragment {

    private static final String TAG = "FirstFragment";

    private FirebaseFirestore db;
    private LoadingDialog loadingDialog;

    private TextView type;
    private TextView price;
    private TextView location;
    private TextView owner;
    private TextView info;
    private ImageView photo;

    public FirstFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        loadingDialog = new LoadingDialog(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_first, container, false);

        ImageView userImage = view.findViewById(R.id.user);
        userImage.setOnClickListener(v -> setCurrentFragment(new ProfileFragment(), userImage));

        setImage(userImage);

        photo = view.findViewById(R.id.photoResult);
        type = view.findViewById(R.id.typeResult);
        price = view.findViewById(R.id.priceResult);
        location = view.findViewById(R.id.locationResult);
        owner = view.findViewById(R.id.ownerResult);
        info = view.findViewById(R.id.infoResult);

        getDocuments();

        return view;
    }

    private void getDocuments() {
        loadingDialog.start();
        db.collection("properties")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            Log.d(TAG, document.getId() + " => " + document.getData());
                            getData(document.getId());
                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    private void getData(String documentId) {
        db.collection("properties").document(documentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        assert document != null;
                        if (document.exists()) {
                            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                            StorageReference propertyImageRef = storageRef
                                    .child("images")
                                    .child(documentId)
                                    .child("property");

                            File localFile;
                            try {
                                localFile = File.createTempFile("photo", "jpg");
                                propertyImageRef.getFile(localFile).addOnCompleteListener(v ->
                                        Picasso.get().load(localFile)
                                                .noPlaceholder()
                                                .networkPolicy(NetworkPolicy.OFFLINE)
                                                .centerInside()
                                                .fit()
                                                .into(photo));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            type.setText(document.getString("type"));
                            price.setText(String.valueOf(document.getDouble("price")));
                            location.setText(document.getString("location"));
                            owner.setText(document.getString("owner"));
                            info.setText(document.getString("info"));
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                    loadingDialog.dismiss();
                });
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