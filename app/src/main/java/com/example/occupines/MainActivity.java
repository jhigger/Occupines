package com.example.occupines;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.occupines.databinding.ActivityMainBinding;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ActivityMainBinding binding;
    public static File localFile;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Connect to Firebase
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        //Set first fragment on load
        downloadImage(new FirstFragment());

        //Add badge on notification
        BottomNavigationView bottomNav = binding.bottomNavigationView;
        BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.notifications);
        //Number of notifications
        int number = 26;
        setupBadge(badge, number);

        //Show each fragment on each menu item click
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            getSupportFragmentManager().popBackStack();

            if (itemId == R.id.home) {
                setCurrentFragment(new FirstFragment());
                return true;
            } else if (itemId == R.id.likes) {
                setCurrentFragment(new SecondFragment());
                return true;
            } else if (itemId == R.id.rentals) {
                setCurrentFragment(new ThirdFragment());
                return true;
            } else if (itemId == R.id.calendar) {
                setCurrentFragment(new FourthFragment());
                return true;
            } else if (itemId == R.id.notifications) {
                setCurrentFragment(new FifthFragment());
                //Remove badge on notification click
                destroyBadge(badge);
                return true;
            }
            return false;
        });
    }

    private void downloadImage(FirstFragment firstFragment) {
        StorageReference pathReference = storageRef.child("kairos.png");

        try {
            localFile = File.createTempFile("profile", "png");
            pathReference.getFile(localFile)
                    .addOnCompleteListener(taskSnapshot -> setCurrentFragment(firstFragment));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setCurrentFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().
                replace(R.id.flFragment, fragment).
                commit();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
            setCurrentFragment(new FirstFragment());
//            super.onBackPressed();
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

    private void setupBadge(BadgeDrawable badge, int number) {
        if (badge != null) {
            if (number != 0) {
                // An icon only badge will be displayed unless a number is set:
                badge.setBackgroundColor(getResources().getColor(R.color.badge_color));
                badge.setBadgeTextColor(Color.WHITE);
                badge.setBadgeGravity(BadgeDrawable.TOP_END);
                badge.setHorizontalOffset(26);
                badge.setMaxCharacterCount(3);
                badge.setNumber(number);
                badge.setVerticalOffset(26);
                badge.setVisible(true);
            } else {
                destroyBadge(badge);
            }
        }
    }

    private void destroyBadge(BadgeDrawable badge) {
        badge.clearNumber();
        badge.setVisible(false);
    }

    @Override
    protected void onDestroy() {
        binding = null;
        super.onDestroy();
    }

}
