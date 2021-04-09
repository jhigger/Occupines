package com.example.occupines.fragments;

import android.content.Intent;
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
import com.squareup.picasso.Callback;
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

    private Uri imagePath1;
    private Uri imagePath2;
    private Uri imagePath3;
    private Uri imagePath4;
    private Uri imagePath5;
    private ImageView photo1;
    private ImageView photo2;
    private ImageView photo3;
    private ImageView photo4;
    private ImageView photo5;
    private boolean hasPickedImage1;
    private boolean hasPickedImage2;
    private boolean hasPickedImage3;
    private boolean hasPickedImage4;
    private boolean hasPickedImage5;

    private int imageCount;
    private int picked;

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
        hasPickedImage1 = false;
        hasPickedImage2 = false;
        hasPickedImage3 = false;
        hasPickedImage4 = false;
        hasPickedImage5 = false;
        imageCount = 0;
        picked = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_form, container, false);
        //Get ImageView reference
        photo1 = view.findViewById(R.id.photo1);
        photo2 = view.findViewById(R.id.photo2);
        photo3 = view.findViewById(R.id.photo3);
        photo4 = view.findViewById(R.id.photo4);
        photo5 = view.findViewById(R.id.photo5);
        //Set ImageView on click event
        photo1.setOnClickListener(v -> pickImage(1));
        photo2.setOnClickListener(v -> pickImage(2));
        photo3.setOnClickListener(v -> pickImage(3));
        photo4.setOnClickListener(v -> pickImage(4));
        photo5.setOnClickListener(v -> pickImage(5));

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
                if (hasPickedImage1 || hasPickedImage2 || hasPickedImage3 || hasPickedImage4 || hasPickedImage5) {
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

    private void pickImage(int i) {
        Intent profileIntent = new Intent();
        profileIntent.setType("image/*");
        profileIntent.setAction(Intent.ACTION_GET_CONTENT);
        picked = i;
        //Open gallery to select photo
        startActivityForResult(Intent.createChooser(profileIntent, "Select Image."), PICK_IMAGE);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Check if data given from other fragment is empty
        //It is not empty if FormFragment was called from edit property
        if (getArguments() != null) {
            if (getArguments().getParcelable("property") != null) {

                Property property = getArguments().getParcelable("property");

                downloadImages(property);

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

                price.setText(new DecimalFormat("###0").format(property.getPrice()));
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
            try {
                Log.d(TAG, "onActivityResult: " + picked);
                switch (picked) {
                    case 1:
                        imagePath1 = data.getData();
                        photo1.setImageBitmap(MediaStore.Images.Media.getBitmap(Objects.requireNonNull(getActivity()).getContentResolver(), imagePath1));
                        hasPickedImage1 = true;
                        break;
                    case 2:
                        imagePath2 = data.getData();
                        photo2.setImageBitmap(MediaStore.Images.Media.getBitmap(Objects.requireNonNull(getActivity()).getContentResolver(), imagePath2));
                        hasPickedImage2 = true;
                        break;
                    case 3:
                        imagePath3 = data.getData();
                        photo3.setImageBitmap(MediaStore.Images.Media.getBitmap(Objects.requireNonNull(getActivity()).getContentResolver(), imagePath3));
                        hasPickedImage3 = true;
                        break;
                    case 4:
                        imagePath4 = data.getData();
                        photo4.setImageBitmap(MediaStore.Images.Media.getBitmap(Objects.requireNonNull(getActivity()).getContentResolver(), imagePath4));
                        hasPickedImage4 = true;
                        break;
                    case 5:
                        imagePath5 = data.getData();
                        photo5.setImageBitmap(MediaStore.Images.Media.getBitmap(Objects.requireNonNull(getActivity()).getContentResolver(), imagePath5));
                        hasPickedImage5 = true;
                        break;
                }
                imageCount++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //Updates property from edit property
    private void updateProperty(String type, double price, String location, String info, String id) {
        loadingDialog.start();

        int imageCount = 0;

        if (hasPickedImage1 || photo1.getTag() != null) {
            if (photo1.getTag().equals("hasImage")) {
                imageCount++;
            }
        }
        if (hasPickedImage2 || photo2.getTag() != null) {
            if (photo2.getTag().equals("hasImage")) {
                imageCount++;
            }
        }
        if (hasPickedImage3 || photo3.getTag() != null) {
            if (photo3.getTag().equals("hasImage")) {
                imageCount++;
            }
        }
        if (hasPickedImage4 || photo4.getTag() != null) {
            if (photo4.getTag().equals("hasImage")) {
                imageCount++;
            }
        }
        if (hasPickedImage5 || photo5.getTag() != null) {
            if (photo5.getTag().equals("hasImage")) {
                imageCount++;
            }
        }

        Map<String, Object> property = new HashMap<>();
        property.put("type", type);
        property.put("price", price);
        property.put("location", location);
        property.put("info", info);
        property.put("updatedAt", FieldValue.serverTimestamp());
        property.put("imageCount", imageCount);

        db.collection(COLLECTION).document(id)
                .update(property)
                .addOnCompleteListener(task -> loadingDialog.dismiss())
                .addOnSuccessListener(aVoid -> {
                    if (hasPickedImage1 || hasPickedImage2 || hasPickedImage3 || hasPickedImage4 || hasPickedImage5) {
                        uploadImage(Integer.parseInt(id.substring(id.length() - 1)));
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
        property.put("imageCount", imageCount);

        propertyCount++;
        db.collection("users").document(userId)
                .update("propertyCount", propertyCount);

        db.collection(COLLECTION).document(userId + "-" + 1).get().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                DocumentSnapshot document1 = task1.getResult();
                assert document1 != null;
                // exists
                if (document1.exists()) {
                    Log.d(TAG, "Document exists!");
                    db.collection(COLLECTION).document(userId + "-" + 2).get().addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful()) {
                            DocumentSnapshot document2 = task2.getResult();
                            assert document2 != null;
                            // exists
                            if (document2.exists()) {
                                Log.d(TAG, "Document exists!");
                                db.collection(COLLECTION).document(userId + "-" + 3).get().addOnCompleteListener(task3 -> {
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
                    uploadImage(i);
                    Utility.showToast(getContext(), "Property submitted");
                })
                .addOnFailureListener(e -> {
                    Utility.showToast(getContext(), "Error: Submission failed");
                    Log.w(TAG, "Error writing document", e);
                });
    }

    //Downloads the photo from firebase storage when editing
    private void downloadImages(Property property) {
        Picasso.get().load(property.getImageFile1())
                .placeholder(R.drawable.ic_camera)
                .error(R.drawable.ic_camera)
                .priority(Picasso.Priority.HIGH)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .centerInside()
                .fit()
                .into(photo1, new Callback() {
                    @Override
                    public void onSuccess() {
                        photo1.setTag("hasImage");
                    }

                    @Override
                    public void onError(Exception e) {
                    }
                });

        Picasso.get().load(property.getImageFile2())
                .placeholder(R.drawable.ic_camera)
                .error(R.drawable.ic_camera)
                .priority(Picasso.Priority.HIGH)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .centerInside()
                .fit()
                .into(photo2, new Callback() {
                    @Override
                    public void onSuccess() {
                        photo2.setTag("hasImage");
                    }

                    @Override
                    public void onError(Exception e) {
                    }
                });

        Picasso.get().load(property.getImageFile3())
                .placeholder(R.drawable.ic_camera)
                .error(R.drawable.ic_camera)
                .priority(Picasso.Priority.HIGH)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .centerInside()
                .fit()
                .into(photo3, new Callback() {
                    @Override
                    public void onSuccess() {
                        photo3.setTag("hasImage");
                    }

                    @Override
                    public void onError(Exception e) {
                    }
                });

        Picasso.get().load(property.getImageFile4())
                .placeholder(R.drawable.ic_camera)
                .error(R.drawable.ic_camera)
                .priority(Picasso.Priority.HIGH)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .centerInside()
                .fit()
                .into(photo4, new Callback() {
                    @Override
                    public void onSuccess() {
                        photo4.setTag("hasImage");
                    }

                    @Override
                    public void onError(Exception e) {
                    }
                });

        Picasso.get().load(property.getImageFile5())
                .placeholder(R.drawable.ic_camera)
                .error(R.drawable.ic_camera)
                .priority(Picasso.Priority.HIGH)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .centerInside()
                .fit()
                .into(photo5, new Callback() {
                    @Override
                    public void onSuccess() {
                        photo5.setTag("hasImage");
                    }

                    @Override
                    public void onError(Exception e) {
                    }
                });
    }

    //Uploads the photo chosen from gallery to firebase storage
    private void uploadImage(int i) {
        //images/User id/property index/image file(s)
        if (hasPickedImage1) {
            StorageReference pathReference = storageRef
                    .child("images")
                    .child(Objects.requireNonNull(mAuth.getUid()))
                    .child("property" + i)
                    .child("image1");
            pathReference.putFile(Utility.compressImage(Objects.requireNonNull(getContext()), imagePath1));
        }
        if (hasPickedImage2) {
            StorageReference pathReference = storageRef
                    .child("images")
                    .child(Objects.requireNonNull(mAuth.getUid()))
                    .child("property" + i)
                    .child("image2");
            pathReference.putFile(Utility.compressImage(Objects.requireNonNull(getContext()), imagePath2));
        }
        if (hasPickedImage3) {
            StorageReference pathReference = storageRef
                    .child("images")
                    .child(Objects.requireNonNull(mAuth.getUid()))
                    .child("property" + i)
                    .child("image3");
            pathReference.putFile(Utility.compressImage(Objects.requireNonNull(getContext()), imagePath3));
        }
        if (hasPickedImage4) {
            StorageReference pathReference = storageRef
                    .child("images")
                    .child(Objects.requireNonNull(mAuth.getUid()))
                    .child("property" + i)
                    .child("image4");
            pathReference.putFile(Utility.compressImage(Objects.requireNonNull(getContext()), imagePath4));
        }
        if (hasPickedImage5) {
            StorageReference pathReference = storageRef
                    .child("images")
                    .child(Objects.requireNonNull(mAuth.getUid()))
                    .child("property" + i)
                    .child("image5");
            pathReference.putFile(Utility.compressImage(Objects.requireNonNull(getContext()), imagePath5));
        }

        assert getFragmentManager() != null;
        getFragmentManager().popBackStack();
    }
}