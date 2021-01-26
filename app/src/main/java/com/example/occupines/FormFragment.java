package com.example.occupines;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FormFragment extends Fragment {

    private static final String TAG = "FormFragment";
    private static final int PICK_IMAGE = 123;
    StorageReference storageRef;
    private FirebaseAuth mAuth;
    private Uri imagePath;
    private ImageView photo;

    public FormFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_form, container, false);

        photo = view.findViewById(R.id.photo);

        photo.setOnClickListener(v -> {
            Intent profileIntent = new Intent();
            profileIntent.setType("image/*");
            profileIntent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(profileIntent, "Select Image."), PICK_IMAGE);
        });

        Spinner spinner = view.findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.types, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        EditText price = view.findViewById(R.id.price);
        EditText location = view.findViewById(R.id.location);
        EditText info = view.findViewById(R.id.info);

        Button submit = view.findViewById(R.id.submit);
        submit.setOnClickListener(v -> {
            String type = spinner.getSelectedItem().toString();
            int priceNumber = Integer.parseInt(price.getText().toString());
            String locationString = location.getText().toString();
            String infoString = info.getText().toString();

            submitProperty(type, priceNumber, locationString, infoString);
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE && resultCode == AppCompatActivity.RESULT_OK && data.getData() != null) {
            imagePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(Objects.requireNonNull(getActivity()).getContentResolver(), imagePath);
                photo.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadImage() {
        StorageReference pathReference = storageRef.child("images").child(Objects.requireNonNull(mAuth.getUid())).child("property"); //images/User id/property.jpg
        UploadTask uploadTask = pathReference.putFile(imagePath);
        uploadTask.addOnCompleteListener(taskSnapshot -> {
            assert getFragmentManager() != null;
            getFragmentManager().popBackStack();
        });
    }

    private void submitProperty(String type, int price, String location, String info) {
        uploadImage();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> property = new HashMap<>();
        property.put("type", type);
        property.put("price", price);
        property.put("location", location);
        property.put("info", info);

        db.collection("properties").document(Objects.requireNonNull(mAuth.getUid()))
                .set(property)
                .addOnSuccessListener(aVoid -> {
                    new Utility().showToast(getContext(), "Property submitted");
                    Log.d(TAG, "DocumentSnapshot successfully written!");
                })
                .addOnFailureListener(e -> {
                    new Utility().showToast(getContext(), "Error: Submission failed");
                    Log.w(TAG, "Error writing document", e);
                });
    }

}