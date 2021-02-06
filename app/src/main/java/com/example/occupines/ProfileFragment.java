package com.example.occupines;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.transition.TransitionInflater;
import android.util.Log;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Objects;


public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private static final String COLLECTION = "properties";
    private static final int PICK_IMAGE = 123;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
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
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userImage = view.findViewById(R.id.user);
        setImage(userImage);

        ImageButton edit = view.findViewById(R.id.editButton);
        TextView name = view.findViewById(R.id.fullName);
        TextView email = view.findViewById(R.id.textEmail);

        Button viewProperty = view.findViewById(R.id.viewProperty);
        Button listProperty = view.findViewById(R.id.listProperty);

        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        name.setText(Objects.requireNonNull(user.getDisplayName()));
        email.setText(Objects.requireNonNull(user.getEmail()));

        edit.setOnClickListener(v -> {
            Intent profileIntent = new Intent();
            profileIntent.setType("image/*");
            profileIntent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(profileIntent, "Select Image."), PICK_IMAGE);
        });

        viewProperty.setOnClickListener(v -> db.collection(COLLECTION).document(Objects.requireNonNull(mAuth.getUid()))
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        assert document != null;
                        if (document.exists()) {
                            Log.d(TAG, "Document exists!");
                            setCurrentFragment(new PropertyFragment());
                        } else {
                            Log.d(TAG, "Document does not exist!");
                            Utility.showToast(getContext(), "You have no property listed");
                        }
                    } else {
                        Log.d(TAG, "Failed with: ", task.getException());
                    }
                }));

        listProperty.setOnClickListener(v -> db.collection(COLLECTION).document(Objects.requireNonNull(mAuth.getUid()))
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        assert document != null;
                        if (document.exists()) {
                            Log.d(TAG, "Document exists!");
                            Utility.showToast(getContext(), "You can only submit 1 property");
                        } else {
                            Log.d(TAG, "Document does not exist!");
                            setCurrentFragment(new FormFragment());
                        }
                    } else {
                        Log.d(TAG, "Failed with: ", task.getException());
                    }
                }));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE && resultCode == AppCompatActivity.RESULT_OK && data.getData() != null) {
            imagePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(Objects.requireNonNull(getActivity()).getContentResolver(), imagePath);
                userImage.setImageBitmap(bitmap);
                uploadImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setCurrentFragment(Fragment fragment) {
        assert getFragmentManager() != null;
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
                .resize(500, 500)
                .into(userImage);
    }

    private void uploadImage() {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        //images/User id/profile.jpg
        StorageReference pathReference = storageRef.child("images").child(Objects.requireNonNull(mAuth.getUid())).child("profile");
        //Compress image then upload
        UploadTask uploadTask = pathReference.putFile(Utility.compressImage(Objects.requireNonNull(getContext()), imagePath));
        uploadTask
                .addOnSuccessListener(taskSnapshot -> {
                    Utility.showToast(getContext(), "Profile picture uploaded");
                    //Restart activity to reload new uploaded image
                    Intent intent = Objects.requireNonNull(getActivity()).getIntent();
                    getActivity().finish();
                    startActivity(intent);
                })
                .addOnFailureListener(e -> Utility.showToast(getContext(), "Error: Uploading profile picture"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }
}