package com.example.occupines;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class FirstFragment extends Fragment {

    public static File localFile;
    private StorageReference storageRef;
    private ImageView userImage;

    public FirstFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        downloadImage();
    }

    private void downloadImage() {
        //Create a reference with an initial file path and name
        storageRef = FirebaseStorage.getInstance().getReference();

        StorageReference pathReference = storageRef.child("kairos.png");

        try {
            localFile = File.createTempFile("profile", "png");

            pathReference.getFile(localFile)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Successfully downloaded data to local file
                        Picasso.get().load(localFile)
                                .noPlaceholder()
                                .error(R.drawable.ic_user)
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .fit()
                                .into(userImage);
                    })
                    .addOnFailureListener(exception -> {
                        // Handle failed download
                        new Utility().showToast(getContext(), "Failed to get photo");
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        userImage = view.findViewById(R.id.user);
        userImage.setOnClickListener(v -> setCurrentFragment(new ProfileFragment(), v));

        TextView text = view.findViewById(R.id.textView);
        String hello = "Hello " + Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName();
        text.setText(hello);

    }

    private void setCurrentFragment(Fragment fragment, View view) {
        ImageView imageView = view.findViewById(R.id.user);

        assert getFragmentManager() != null;
        getFragmentManager()
                .beginTransaction()
                .addSharedElement(imageView, "profile")
                .addToBackStack("FirstFragment")
                .replace(R.id.flFragment, fragment)
                .commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }
}