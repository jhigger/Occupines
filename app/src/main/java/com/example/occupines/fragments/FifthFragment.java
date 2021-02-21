package com.example.occupines.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.occupines.GetNearbyPlacesData;
import com.example.occupines.LoadingDialog;
import com.example.occupines.R;
import com.example.occupines.Utility;
import com.example.occupines.models.Property;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class FifthFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "FifthFragment";
    private static final int REQUEST_CODE = 101;
    final int PROXIMITY_RADIUS = 10000;
    GoogleMap map;
    SupportMapFragment mapFragment;
    SearchView searchView;
    Geocoder geocoder;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    double latitude, longitude;

    private FirebaseFirestore db;
    private LoadingDialog loadingDialog;

    public FifthFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        geocoder = new Geocoder(getContext(), Locale.getDefault());
        db = FirebaseFirestore.getInstance();
        loadingDialog = new LoadingDialog(getActivity());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fifth, container, false);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(getActivity()));
        fetchLastLocation();

        searchView = view.findViewById(R.id.sv_location);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = searchView.getQuery().toString();
                List<Address> geoResults = null;

                try {
                    geoResults = geocoder.getFromLocationName(location, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (geoResults != null && geoResults.size() > 0) {
                    Address address = geoResults.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
                } else {
                    Utility.showToast(getContext(), "Place not found");
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        Button showRentals = view.findViewById(R.id.b_show);
        showRentals.setOnClickListener(v -> {
            Object[] dataTransfer = new Object[2];
            GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();

            String hospital = "apartment";
            String url = getUrl(latitude, longitude, hospital);
            dataTransfer[0] = map;
            dataTransfer[1] = url;

            getNearbyPlacesData.execute(dataTransfer);

            getDocuments();
        });

        return view;
    }

    private void getDocuments() {
        loadingDialog.start();

        db.collection("properties")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            if (document.exists()) {
                                String documentId = document.getId();

                                Property propertyPost = new Property(
                                        document.getString("type"),
                                        Objects.requireNonNull(document.getDouble("price")),
                                        document.getString("location"),
                                        document.getString("owner"),
                                        document.getString("info"),
                                        documentId);

                                String location = propertyPost.getLocation();
                                List<Address> geoResults = new ArrayList<>();

                                try {
                                    geoResults = geocoder.getFromLocationName(location, 1);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                if (geoResults.size() > 0) {
                                    Address address = geoResults.get(0);
                                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                                    map.addMarker(new MarkerOptions().position(latLng).title(propertyPost.getLocation()));
                                }
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        }
                        //Show whole Baguio City
                        LatLng latLng = new LatLng(16.402148394057043, 120.59555516012183);
                        map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12)); //could be 2 - 21
                        Utility.showToast(getContext(), "Showing nearby rentals");
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                    loadingDialog.dismiss();
                });
    }

    private void fetchLastLocation() {
        if (ActivityCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        Task<Location> getLastLocation = fusedLocationProviderClient.getLastLocation();

        getLastLocation.addOnSuccessListener(location -> {
            if (location != null) {
                currentLocation = location;
                Utility.showToast(getContext(), currentLocation.getLatitude()
                        + " " + currentLocation.getLongitude());
                assert getFragmentManager() != null;
                // Getting reference to the SupportMapFragment
                mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
                assert mapFragment != null;
                // Getting GoogleMap object from the fragment
                mapFragment.getMapAsync(FifthFragment.this);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        //get lat long for corners for specified place
        LatLng one = new LatLng(16.3833911236084, 120.57546615600587);
        LatLng two = new LatLng(16.4316396660511, 120.62464714050293);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        //add them to builder
        builder.include(one);
        builder.include(two);

        LatLngBounds bounds = builder.build();

        //get width and height to current display screen
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;

        // 20% padding
        int padding = (int) (width * 0.20);

        //set lat long bounds
        map.setLatLngBoundsForCameraTarget(bounds);

        //move camera to fill the bound to screen
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding));

        //set zoom to level to current so that you won't be able to zoom out viz. move outside bounds
        map.setMinZoomPreference(map.getCameraPosition().zoom);

        //Set map view to display a mixture of normal and satellite views
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        map.setOnMarkerClickListener(marker -> {
            map.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 18));
            marker.showInfoWindow();
            return true;
        });

        //Show current location
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18)); //could be 2 - 21
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLastLocation();
            }
        }
    }

    private String getUrl(double latitude, double longitude, String nearbyPlace) {

        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location=").append(latitude).append(",").append(longitude);
        googlePlaceUrl.append("&radius=").append(PROXIMITY_RADIUS);
        googlePlaceUrl.append("&type=").append(nearbyPlace);
        googlePlaceUrl.append("&sensor=true");
        googlePlaceUrl.append("&key=" + R.string.google_api_key);

        Log.d(TAG, "url = " + googlePlaceUrl.toString());

        return googlePlaceUrl.toString();
    }
}