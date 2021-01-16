package com.example.occupines;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirstFragment firstFragment = new FirstFragment();
        SecondFragment secondFragment = new SecondFragment();
        ThirdFragment thirdFragment = new ThirdFragment();
        FourthFragment fourthFragment = new FourthFragment();
        FifthFragment fifthFragment = new FifthFragment();

        setCurrentFragment(firstFragment);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    setCurrentFragment(firstFragment);
                    return true;
                case R.id.likes:
                    setCurrentFragment(secondFragment);
                    return true;
                case R.id.rentals:
                    setCurrentFragment(thirdFragment);
                    return true;
                case R.id.calendar:
                    setCurrentFragment(fourthFragment);
                    return true;
                case R.id.notifications:
                    setCurrentFragment(fifthFragment);
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
}
