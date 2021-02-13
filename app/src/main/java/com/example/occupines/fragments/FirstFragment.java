package com.example.occupines.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.occupines.MainActivity;
import com.example.occupines.R;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class FirstFragment extends Fragment {

    public FirstFragment() {
        // Required empty public constructor
    }

    //onCreateView starts after onCreate
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_first, container, false);
        //Get userImage reference
        ImageView userImage = view.findViewById(R.id.userImage);
        //Set current fragment to ProfileFragment on userImage click
        userImage.setOnClickListener(v -> setCurrentFragment(new ProfileFragment(), userImage));
        //Set userImage source to downloaded image from MainActivity
        setImage(userImage);

        //Get burgerMenu reference
        ImageView burgerMenu = view.findViewById(R.id.burgerMenu);
        //Show About Us on burgerMenu click
        burgerMenu.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(inflater.inflate(R.layout.about_dialog, container, false))
                    .create()
                    .show();
        });

        return view;
    }

    //onViewCreated starts after onCreateView
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Show list of rental properties
        getChildFragmentManager().beginTransaction().replace(R.id.propertyPosts, new ListFragment()).commitNow();
    }

    //Replaces the current fragment
    private void setCurrentFragment(Fragment fragment, ImageView userImage) {
        assert getFragmentManager() != null;
        getFragmentManager()
                .beginTransaction()
                .addSharedElement(userImage, "profile")
                .addToBackStack("FirstFragment")
                .replace(R.id.flFragment, fragment)
                .commit();
    }

    //Sets the image of an ImageView
    private void setImage(ImageView userImage) {
        Picasso.get().load(MainActivity.localFile)
                .placeholder(R.drawable.ic_user)
                .error(R.drawable.ic_user)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .centerCrop()
                .resize(500, 500)
                .into(userImage);
    }

}