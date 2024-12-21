package com.minh.bloodlife.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.minh.bloodlife.R;
import com.minh.bloodlife.model.DonationSite;

import java.util.List;


public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final String TAG = "MapsFragment";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final float DEFAULT_ZOOM = 15f;

    private MapView mapView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Location lastKnownLocation;
    private FirebaseFirestore db;
    private ProgressBar loadingIndicator;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                if (permissions.get(Manifest.permission.ACCESS_FINE_LOCATION) && permissions.get(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    Log.d(TAG, "Location permissions granted");
                    enableMyLocation();
                    getMyLastLocation();
                } else {
                    showPermissionRationaleDialog();
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        loadingIndicator = view.findViewById(R.id.loadingIndicator);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        db = FirebaseFirestore.getInstance();

        return view;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        updateLocationUI();

        getMyLastLocation();

        fetchDonationSites();

        googleMap.setOnMarkerClickListener(this);
    }
    public Location getLastKnownLocation() {
        return lastKnownLocation;
    }

    private void requestLocationPermissions() {
        requestPermissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void getMyLastLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            lastKnownLocation = location;
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching location: " + e.getMessage());
                        Toast.makeText(getContext(), "Failed to fetch location.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            requestLocationPermissions();
        }
    }

    private void updateLocationUI() {
        if (googleMap == null) {
            return;
        }
        try {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                googleMap.setMyLocationEnabled(false);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                requestLocationPermissions();
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Error updating location UI", e);
        }
    }

    private void enableMyLocation() {
        if (googleMap == null) {
            return;
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            requestLocationPermissions();
        }
    }

    private void fetchDonationSites() {
        loadingIndicator.setVisibility(View.VISIBLE);
        db.collection("donationSites")
                .get()
                .addOnCompleteListener(task -> {
                    loadingIndicator.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            DonationSite site = document.toObject(DonationSite.class);
                            String siteId = document.getId();
                            site.setSiteId(siteId);

                            if (site.getLocation() != null) {
                                LatLng siteLatLng = new LatLng(site.getLocation().getLatitude(), site.getLocation().getLongitude());
                                Marker marker = googleMap.addMarker(new MarkerOptions()
                                        .position(siteLatLng)
                                        .title(site.getSiteName())
                                        .snippet(site.getAddress())
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_pin)));
                                if (marker != null) {
                                    marker.setTag(siteId);
                                }
                            }
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to load donation sites", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error fetching donation sites", task.getException());
                    }
                });
    }

    private void showPermissionRationaleDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Location Permission Needed")
                .setMessage("This app requires location permissions to show your current location on the map.")
                .setPositiveButton("OK", (dialog, which) -> requestLocationPermissions())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        String siteId = (String) marker.getTag();
        if (siteId != null) {
            SiteDetailsFragment siteDetailsFragment = SiteDetailsFragment.newInstance(siteId);
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentContainer, siteDetailsFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
