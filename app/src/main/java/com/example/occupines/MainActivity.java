package com.example.occupines;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.occupines.databinding.ActivityMainBinding;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Connect to Firebase
        mAuth = FirebaseAuth.getInstance();

        FirstFragment firstFragment = new FirstFragment();
        SecondFragment secondFragment = new SecondFragment();
        ThirdFragment thirdFragment = new ThirdFragment();
        FourthFragment fourthFragment = new FourthFragment();
        FifthFragment fifthFragment = new FifthFragment();

        //Set first fragment on load
        setCurrentFragment(new FirstFragment());

        //Add badge on notification
        BottomNavigationView bottomNav = binding.bottomNavigationView;
        BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.notifications);
        //Number of notifications
        int number = 26;
        setupBadge(badge, number);

        //Show each fragment on each menu item click
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                setCurrentFragment(firstFragment);
                return true;
            } else if (itemId == R.id.likes) {
                setCurrentFragment(secondFragment);
                return true;
            } else if (itemId == R.id.rentals) {
                setCurrentFragment(thirdFragment);
                return true;
            } else if (itemId == R.id.calendar) {
                setCurrentFragment(fourthFragment);
                return true;
            } else if (itemId == R.id.notifications) {
                setCurrentFragment(fifthFragment);
                //Remove badge on notification click
                destroyBadge(badge);
                return true;
            }
            return false;
        });
    }

    private void setCurrentFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().
                replace(R.id.flFragment, fragment).
                commit();
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {
        if (keycode == KeyEvent.KEYCODE_BACK) {
            signOut();
            return true;
        }
        return super.onKeyDown(keycode, e);
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
        super.onDestroy();
        binding = null;
    }

}
