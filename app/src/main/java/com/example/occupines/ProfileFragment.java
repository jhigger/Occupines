package com.example.occupines;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private FirebaseAuth mAuth;
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

        getImage();

        ImageButton edit = view.findViewById(R.id.editButton);
        TextView name = view.findViewById(R.id.fullName);
        TextView email = view.findViewById(R.id.textEmail);

        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        name.setText(Objects.requireNonNull(user.getDisplayName()));
        email.setText(Objects.requireNonNull(user.getEmail()));

//        edit.setOnClickListener(v -> {
//            Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            startActivityForResult(gallery, 1000);
//        });
    }

    private void getImage() {
        File localFile = MainActivity.localFile;

        Picasso.get().load(localFile)
                .noPlaceholder()
                .error(R.drawable.ic_user)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .centerInside()
                .fit()
                .into(userImage);
    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == 1000) {
//            if (resultCode == Activity.RESULT_OK) {
//                assert data != null;
//                Uri imageUri = data.getData();
//                performCrop((String.valueOf(imageUri)));
//            }
//        }
//        if (requestCode == 400) {
//            if (resultCode == Activity.RESULT_OK) {
//                assert data != null;
//                Uri imageUri = data.getData();
//                Bundle extras = data.getExtras();
//                Bitmap selectedBitmap = extras.getParcelable("data");
//                // Set The Bitmap Data To ImageView
//                userImage.setImageBitmap(selectedBitmap);
//                userImage.setScaleType(ImageView.ScaleType.FIT_XY);
//
//                FirebaseUser user = mAuth.getCurrentUser();
//                updateImage(user, imageUri);
//            }
//        }
//    }

    private void updateImage(FirebaseUser user, Uri imageUri) {
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(imageUri).build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                        }
                    });
        }
    }

    private void performCrop(String picUri) {
        //Start Crop Activity
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        // indicate image type and Uri
        File f = new File(picUri);
        Uri contentUri = Uri.fromFile(f);

        cropIntent.setDataAndType(contentUri, "image/*");
        // set crop properties
        cropIntent.putExtra("crop", "true");
        // indicate aspect of desired crop
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        // indicate output X and Y
        cropIntent.putExtra("outputX", 280);
        cropIntent.putExtra("outputY", 280);

        // retrieve data on return
        cropIntent.putExtra("return-data", true);
        // start the activity - we handle returning in onActivityResult
        startActivityForResult(cropIntent, 400);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }
}