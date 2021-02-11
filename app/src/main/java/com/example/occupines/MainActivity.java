package com.example.occupines;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.occupines.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ActivityMainBinding binding;

    public static File localFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Connect to Firebase
        mAuth = FirebaseAuth.getInstance();

        //Initialize fragments
        FirstFragment firstFragment = new FirstFragment();
        SecondFragment secondFragment = new SecondFragment();
        ThirdFragment thirdFragment = new ThirdFragment();
        FourthFragment fourthFragment = new FourthFragment();
        FifthFragment fifthFragment = new FifthFragment();

        //Set first fragment on load
        downloadImage(firstFragment);

        //Add badge on notification
        BottomNavigationView bottomNav = binding.bottomNavigationView;

        //Show each fragment on each menu item click
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            getSupportFragmentManager().popBackStackImmediate();

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

    private void downloadImage(Fragment firstFragment) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference pathReference = storageRef.child("images").child(Objects.requireNonNull(mAuth.getUid())).child("profile");
        try {
            localFile = File.createTempFile("profile", "jpg");
            pathReference.getFile(localFile).addOnCompleteListener(v ->
                    getSupportFragmentManager().beginTransaction().
                            add(R.id.flFragment, firstFragment).
                            commitAllowingStateLoss());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setCurrentFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fm.beginTransaction()
                .replace(R.id.flFragment, fragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        //If back button is pressed and we are not at home page
        //Then go back 1 page
        //Else ask if going to sign out
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            super.onBackPressed();
        } else {
            signOut();
        }
    }

    private void signOut() {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    mAuth.signOut();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    finish();
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Sign out?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    @Override
    protected void onDestroy() {
        binding = null;
        super.onDestroy();
    }

}
