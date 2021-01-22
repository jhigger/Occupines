package com.example.occupines;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView userImage = view.findViewById(R.id.user);
        setImage(userImage);
        userImage.setOnClickListener(v -> setCurrentFragment(new ProfileFragment(), v));

        TextView text = view.findViewById(R.id.textView);
        String hello = "Hello " + Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName();
        text.setText(hello);
    }

    private void setCurrentFragment(Fragment fragment, View view) {
        ImageView imageView = view.findViewById(R.id.user);

        assert getFragmentManager() != null;
        getFragmentManager()
                .beginTransaction()
                .addSharedElement(imageView, "profile")
                .addToBackStack("FirstFragment")
                .replace(R.id.flFragment, fragment)
                .commit();
    }

    private void setImage(ImageView userImage) {
        Picasso.get().load(MainActivity.localFile)
                .noPlaceholder()
                .error(R.drawable.ic_user)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .fit()
                .into(userImage);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }
}