package com.example.occupines;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.occupines.databinding.ActivityMainBinding;
import com.example.occupines.fragments.FifthFragment;
import com.example.occupines.fragments.FirstFragment;
import com.example.occupines.fragments.FourthFragment;
import com.example.occupines.fragments.SecondFragment;
import com.example.occupines.fragments.ThirdFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    //Setup global variables
    private FirebaseAuth mAuth;
    private ActivityMainBinding binding;

    public static File localFile;

    //MainActivity starts here
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        //Sets data binding for bottomNavigationView
        setContentView(binding.getRoot());

        //Connect to Firebase
        mAuth = FirebaseAuth.getInstance();

        //Initialize fragments
        FirstFragment firstFragment = new FirstFragment();
        SecondFragment secondFragment = new SecondFragment();
        ThirdFragment thirdFragment = new ThirdFragment();
        FourthFragment fourthFragment = new FourthFragment();
        FifthFragment fifthFragment = new FifthFragment();

        //Download profile image then load first fragment
        downloadImage().addOnCompleteListener(v -> setCurrentFragment(firstFragment));

        //Get bottomNav reference
        BottomNavigationView bottomNav = binding.bottomNavigationView;

        //Show each fragment on each menu item click
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            //Clear fragment stack
            getSupportFragmentManager().popBackStackImmediate();
            //Set current fragment on bottomNav item click
            if (itemId == R.id.home) {
                //1st page
                setCurrentFragment(firstFragment);
                return true;
            } else if (itemId == R.id.likes) {
                //2nd page
                setCurrentFragment(secondFragment);
                return true;
            } else if (itemId == R.id.messages) {
                //3rd page
                setCurrentFragment(thirdFragment);
                return true;
            } else if (itemId == R.id.calendar) {
                //4th page
                setCurrentFragment(fourthFragment);
                return true;
            } else if (itemId == R.id.notifications) {
                //5th page
                setCurrentFragment(fifthFragment);
                return true;
            }
            return false;
        });
    }

    //Download image from firebase storage if user uploaded a profile image
    private FileDownloadTask downloadImage() {
        //Instantiate FirebaseStorage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference pathReference = storageRef.child("images").child(Objects.requireNonNull(mAuth.getUid())).child("profile");
        try {
            //Create temporary file for image data
            localFile = File.createTempFile("profile", "jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Download the image data and put it in the temporary file
        return pathReference.getFile(localFile);
    }

    //Sets the current fragment
    private void setCurrentFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        //Clear fragment stack
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        //Replace current fragment
        fm.beginTransaction()
                .replace(R.id.flFragment, fragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        //If back button is pressed and fragment stack is not clear
        //Then go back 1 fragment
        //Else ask if going to sign out
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            super.onBackPressed();
        } else {
            Utility.signOut(getParent(), mAuth);
        }
    }

    @Override
    protected void onDestroy() {
        //Set values to null to prevent memory leak
        binding = null;
        super.onDestroy();
    }

}