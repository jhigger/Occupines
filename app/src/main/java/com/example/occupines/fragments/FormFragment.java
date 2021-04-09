package com.example.occupines.fragments;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.occupines.LoadingDialog;
import com.example.occupines.R;
import com.example.occupines.Utility;
import com.example.occupines.models.Property;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FormFragment extends Fragment {

    private static final String TAG = "FormFragment";
    private static final int PICK_IMAGE = 123;
    private static final String COLLECTION = "properties";

    private FirebaseFirestore db;
    private String userId;
    private StorageReference storageRef;
    private FirebaseAuth mAuth;
    private LoadingDialog loadingDialog;

    private Uri imagePath;
    private ImageView photo;
    private boolean pickedImage;

    private Spinner type;
    private EditText price;
    private EditText location;
    private EditText info;
    private Button submit;

    private int propertyCount;

    public FormFragment() {
        // Required empty public constructor
    }

    public static FormFragment newInstance(Property property) {
        FormFragment fragment = new FormFragment();
        Bundle args = new Bundle();
        args.putParcelable("property", property);
        fragment.setArguments(args);
        return fragment;
    }

    public static FormFragment newInstance(int propertyCount) {
        FormFragment fragment = new FormFragment();
        Bundle args = new Bundle();
        args.putInt("propertyCount", propertyCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userId = mAuth.getUid();
        storageRef = FirebaseStorage.getInstance().getReference();
        loadingDialog = new LoadingDialog(getActivity());
        pickedImage = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_form, container, false);
        //Get ImageView reference
        photo = view.findViewById(R.id.photo);
        //Set ImageView on click event
        photo.setOnClickListener(v -> {
            Intent profileIntent = new Intent();
            profileIntent.setType("image/*");
            profileIntent.setAction(Intent.ACTION_GET_CONTENT);
            //Open gallery to select photo
            startActivityForResult(Intent.createChooser(profileIntent, "Select Image."), PICK_IMAGE);
        });

        //Get spinner reference
        type = view.findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.types, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        type.setAdapter(adapter);

        //Get field references
        price = view.findViewById(R.id.price);
        location = view.findViewById(R.id.location);
        info = view.findViewById(R.id.info);

        //Get button reference
        submit = view.findViewById(R.id.submit);
        //Set button on click event
        submit.setOnClickListener(v -> {
            //Get String from fields
            String typeString = type.getSelectedItem().toString();
            String priceString = price.getText().toString();
            String locationString = location.getText().toString();
            String infoString = info.getText().toString();

            //Check if fields are empty
            if (!priceString.isEmpty() && !locationString.isEmpty() && !infoString.isEmpty()) {
                //Check if user already picked an image from gallery
                if (pickedImage) {
                    //Call submitProperty method
                    submitProperty(typeString, Double.parseDouble(priceString), locationString, infoString);
                } else {
                    Utility.showToast(getContext(), "Please choose an image.");
                }
            } else {
                Utility.showToast(getContext(), "Some fields are empty.");
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Check if data given from other fragment is empty
        //It is not empty if FormFragment was called from edit property
        if (getArguments() != null) {
            if (getArguments().getParcelable("property") != null) {

                Property property = getArguments().getParcelable("property");

                downloadImage(property);

                switch (property.getType()) {
                    case "House":
                        type.setSelection(0);
                        break;
                    case "Apartment":
                        type.setSelection(1);
                        break;
                    case "Boarding":
                        type.setSelection(2);
                        break;
                }

                price.setText(new DecimalFormat("#,##0.00").format(property.getPrice()));
                location.setText(property.getLocation());
                info.setText(property.getInfo());

                submit.setText(R.string.save);
                submit.setOnClickListener(v -> updateProperty(
                        type.getSelectedItem().toString(),
                        Double.parseDouble(price.getText().toString()),
                        location.getText().toString(),
                        info.getText().toString(),
                        property.getId()
                ));
            } else if (getArguments().getInt("propertyCount") != 0) {
                propertyCount = getArguments().getInt("propertyCount");
            }
        }
    }

    //Takes image from gallery
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE && resultCode == AppCompatActivity.RESULT_OK && data.getData() != null) {
            imagePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(Objects.requireNonNull(getActivity()).getContentResolver(), imagePath);
                photo.setImageBitmap(bitmap);
                pickedImage = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //Updates property from edit property
    private void updateProperty(String type, double price, String location, String info, String id) {
        loadingDialog.start();

        Map<String, Object> property = new HashMap<>();
        property.put("type", type);
        property.put("price", price);
        property.put("location", location);
        property.put("info", info);
        property.put("updatedAt", FieldValue.serverTimestamp());

        db.collection(COLLECTION).document(id)
                .update(property)
                .addOnCompleteListener(task -> loadingDialog.dismiss())
                .addOnSuccessListener(aVoid -> {
                    if (pickedImage) {
                        uploadImage();
                    } else {
                        assert getFragmentManager() != null;
                        getFragmentManager().popBackStack();
                    }
                    Utility.showToast(getContext(), "Property updated");
                })
                .addOnFailureListener(e -> {
                    Utility.showToast(getContext(), "Error: Submission failed");
                    Log.w(TAG, "Error writing document", e);
                });
    }

    //Submits property to firestore properties collection
    private void submitProperty(String type, double price, String location, String info) {
        loadingDialog.start();

        Map<String, Object> property = new HashMap<>();
        property.put("type", type);
        property.put("price", price);
        property.put("location", location);
        property.put("owner", Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName());
        property.put("info", info);
        property.put("createdAt", FieldValue.serverTimestamp());

        propertyCount++;
        db.collection("users").document(userId)
                .update("propertyCount", propertyCount);

        db.collection("users").document(userId + "-" + 1).get().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                DocumentSnapshot document1 = task1.getResult();
                assert document1 != null;
                // exists
                if (document1.exists()) {
                    Log.d(TAG, "Document exists!");
                    db.collection("users").document(userId + "-" + 2).get().addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful()) {
                            DocumentSnapshot document2 = task2.getResult();
                            assert document2 != null;
                            // exists
                            if (document2.exists()) {
                                Log.d(TAG, "Document exists!");
                                db.collection("users").document(userId + "-" + 3).get().addOnCompleteListener(task3 -> {
                                    if (task3.isSuccessful()) {
                                        DocumentSnapshot document = task3.getResult();
                                        assert document != null;

                                        // doesn't exists
                                        // create new doc 3
                                        createDoc(property, 3);

                                    } else {
                                        Log.d(TAG, "Failed with: ", task3.getException());
                                    }
                                });
                            }
                            // doesn't exists
                            else {
                                // create new doc 2
                                createDoc(property, 2);
                            }
                        } else {
                            Log.d(TAG, "Failed with: ", task2.getException());
                        }
                    });
                }
                // doesn't exists
                else {
                    // create new doc 1
                    createDoc(property, 1);
                }
            } else {
                Log.d(TAG, "Failed with: ", task1.getException());
            }
        });
    }

    private void createDoc(Map<String, Object> property, int i) {
        db.collection(COLLECTION).document(userId + "-" + i)
                .set(property)
                .addOnCompleteListener(task -> loadingDialog.dismiss())
                .addOnSuccessListener(aVoid -> {
                    uploadImage();
                    Utility.showToast(getContext(), "Property submitted");
                })
                .addOnFailureListener(e -> {
                    Utility.showToast(getContext(), "Error: Submission failed");
                    Log.w(TAG, "Error writing document", e);
                });
    }

    //Downloads the photo from firebase storage when editing
    private void downloadImage(Property property) {
        Picasso.get().load(property.getLocalFile())
                .placeholder(R.drawable.ic_camera)
                .error(R.drawable.ic_camera)
                .priority(Picasso.Priority.HIGH)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .centerInside()
                .fit()
                .into(photo);
    }

    //Uploads the photo chosen from gallery to firebase storage
    private void uploadImage() {
        //images/User id/property.jpg
        StorageReference pathReference = storageRef.child("images")
                .child(Objects.requireNonNull(mAuth.getUid()))
                .child("property" + propertyCount)
                .child("image1");
        UploadTask uploadTask = pathReference.putFile(Utility.compressImage(Objects.requireNonNull(getContext()), imagePath));
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            assert getFragmentManager() != null;
            getFragmentManager().popBackStack();
        });
    }

}