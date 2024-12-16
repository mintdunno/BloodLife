package com.minh.bloodlife.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                if (permissions.get(Manifest.permission.ACCESS_FINE_LOCATION) && permissions.get(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    // Both permissions are granted
                    Log.d(TAG, "Location permissions granted");
                    enableMyLocation();
                    getMyLastLocation();
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied.
                    Toast.makeText(getContext(), "Location permission is required to show your location on the map", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        db = FirebaseFirestore.getInstance();

        return view;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the map's camera position.
        getMyLastLocation();

        // Fetch and display donation sites
        fetchDonationSites();

        // Set a marker click listener
        googleMap.setOnMarkerClickListener(this);
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
                            Log.d(TAG, "Last known location: " + lastKnownLocation);
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            // Use a default location or handle the null case appropriately
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Exception: " + e.getMessage());
                        // Handle the error, perhaps using a default location or showing an error message
                    });
        } else {
            requestLocationPermissions();
        }
    }

    public Location getLastKnownLocation() {
        return lastKnownLocation;
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
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    private void enableMyLocation() {
        if (googleMap == null) {
            return;
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true); // Enable the button
        } else {
            // Request permission or handle the case where it's not granted
            requestLocationPermissions();
        }
    }

    private void fetchDonationSites() {
        db.collection("donationSites")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            DonationSite site = document.toObject(DonationSite.class);
                            // Set the document ID as the site ID
                            String siteId = document.getId();
                            site.setSiteId(document.getId());

                            // Update the document with the lowercase site name for searching
                            db.collection("donationSites").document(siteId)
                                    .update("searchableName", site.getSiteName().toLowerCase())
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully updated with searchableName!"))
                                    .addOnFailureListener(e -> Log.w(TAG, "Error updating document with searchableName", e));

                            if (site.getLocation() != null) {
                                LatLng siteLatLng = new LatLng(site.getLocation().getLatitude(), site.getLocation().getLongitude());
                                Marker marker = googleMap.addMarker(new MarkerOptions()
                                        .position(siteLatLng)
                                        .title(site.getSiteName())
                                        .snippet(site.getAddress())
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_pin)));
                                if (marker != null) {
                                    marker.setTag(siteId); // Associate the site ID with the marker
                                }
                            }
                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                        Toast.makeText(getContext(), "Failed to load donation sites", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    // Helper method to format the list of blood types for display
    private String formatRequiredBloodTypes(List<String> bloodTypes) {
        if (bloodTypes == null || bloodTypes.isEmpty()) {
            return "No blood types specified";
        }
        return "Required: " + String.join(", ", bloodTypes);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // Retrieve the site ID associated with the marker
        String siteId = (String) marker.getTag();
        if (siteId != null) {
            // Create a new instance of SiteDetailsFragment and pass the site ID
            SiteDetailsFragment siteDetailsFragment = SiteDetailsFragment.newInstance(siteId);

            // Replace the current fragment with SiteDetailsFragment
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentContainer, siteDetailsFragment);
            transaction.addToBackStack(null); // Optional: Add to back stack for navigation
            transaction.commit();
        }

        return true; // Return true to indicate that we have consumed the event
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}