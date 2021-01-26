package com.example.occupines;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Objects;


public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private static final int PICK_IMAGE = 123;

    private FirebaseAuth mAuth;
    private Uri imagePath;
    private ImageView userImage;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSharedElementEnterTransition(TransitionInflater.from(getContext()).inflateTransition(android.R.transition.move));
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userImage = view.findViewById(R.id.user);
        setImage(userImage);

        ImageButton edit = view.findViewById(R.id.editButton);
        TextView name = view.findViewById(R.id.fullName);
        TextView email = view.findViewById(R.id.textEmail);

//        Button messages = view.findViewById(R.id.messages);
        Button listProperty = view.findViewById(R.id.listProperty);

        FirebaseUser user = mAuth.getCurrentUser();
        name.setText(Objects.requireNonNull(user.getDisplayName()));
        email.setText(Objects.requireNonNull(user.getEmail()));

        edit.setOnClickListener(v -> {
            Intent profileIntent = new Intent();
            profileIntent.setType("image/*");
            profileIntent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(profileIntent, "Select Image."), PICK_IMAGE);
        });

        listProperty.setOnClickListener(v -> setCurrentFragment(new FormFragment()));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE && resultCode == AppCompatActivity.RESULT_OK && data.getData() != null) {
            imagePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imagePath);
                userImage.setImageBitmap(bitmap);
                uploadImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setCurrentFragment(Fragment fragment) {
        getFragmentManager()
                .beginTransaction()
                .addToBackStack(TAG)
                .replace(R.id.flFragment, fragment)
                .commit();
    }

    private void setImage(ImageView userImage) {
        Picasso.get().load(MainActivity.localFile)
                .noPlaceholder()
                .error(R.drawable.ic_user)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .centerCrop()
                .resize(200, 200)
                .into(userImage);
    }

    private void uploadImage() {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference pathReference = storageRef.child("images").child(mAuth.getUid()).child("profile"); //User id/Images/Profile Pic.jpg
        UploadTask uploadTask = pathReference.putFile(imagePath);
        uploadTask
                .addOnSuccessListener(taskSnapshot -> {
                    new Utility().showToast(getContext(), "Profile picture uploaded");
                    Intent intent = getActivity().getIntent();
                    getActivity().finish();
                    startActivity(intent);
                })
                .addOnFailureListener(e -> new Utility().showToast(getContext(), "Error: Uploading profile picture"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }
}