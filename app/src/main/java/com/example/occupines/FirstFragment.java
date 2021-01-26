package com.example.occupines;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class FirstFragment extends Fragment {

    private FirebaseAuth mAuth;

    public FirstFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_first, container, false);

        ImageView userImage = view.findViewById(R.id.user);
        userImage.setOnClickListener(v -> setCurrentFragment(new ProfileFragment(), userImage));

        setImage(userImage);

        TextView text = view.findViewById(R.id.textView);
        String hello = "Hello " + Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName();
        text.setText(hello);

        return view;
    }

    private void setCurrentFragment(Fragment fragment, ImageView userImage) {
        getFragmentManager()
                .beginTransaction()
                .addSharedElement(userImage, "profile")
                .addToBackStack("FirstFragment")
                .replace(R.id.flFragment, fragment)
                .commit();
    }

    private void setImage(ImageView userImage) {
        Picasso.get().load(MainActivity.localFile)
                .noPlaceholder()
                .error(R.drawable.ic_user)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .centerCrop()
                .fit()
                .into(userImage);
    }

}