package com.example.uptechapp.activity;

import android.Manifest;
import android.app.Activity;
import android.app.LocaleManager;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.example.uptechapp.R;
import com.example.uptechapp.api.CompleteListener;
import com.example.uptechapp.dao.Database;
import com.example.uptechapp.dao.LocationTracker;
import com.example.uptechapp.dao.MapService;
import com.example.uptechapp.dao.MyViewModel;
import com.example.uptechapp.databinding.FragmentMapBinding;
import com.example.uptechapp.model.Emergency;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.internal.IMapFragmentDelegate;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapFragment extends Fragment {

    private FragmentMapBinding binding;

    private List<Emergency> myEmergencyList;
    private MapService mapService;
    GoogleMap mMap;
    private static LatLng person_latLng = null;
    ActivityResultLauncher<String[]> locationPermissionRequest;

    private static LatLng latLngs = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentMapBinding.inflate(getLayoutInflater());
        locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {
                            Boolean fineLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_FINE_LOCATION, false);
                            Boolean coarseLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_COARSE_LOCATION, false);
                            if (fineLocationGranted != null && fineLocationGranted) {
                                // Precise location access granted.
                                Toast.makeText(getContext(), "Precise location access granted", Toast.LENGTH_SHORT).show();
                            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                //
                                Toast.makeText(getContext(), "Only approximate location access granted.", Toast.LENGTH_SHORT).show();
                            } else {
                                //
                                Toast.makeText(getContext(), "No location access granted. Denied", Toast.LENGTH_SHORT).show();
                            }
                        }
                );

        if (!checkLoc()) {
            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        checkLoc();

        if (!checkLoc()) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            person_latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        }
                    }
                });


        myEmergencyList = new ArrayList<Emergency>();
        Database.loadEmergencies(new CompleteListener() {
            @Override
            public void OnSuccess() {
            }

            @Override
            public void OnFailure() {
            }
        });

        final Observer<List<Emergency>> myObserver = new Observer<List<Emergency>>() {
            @Override
            public void onChanged(List<Emergency> emergencies) {
                Log.d("NIKITA", "INOF");
                Log.d("NIKITA", String.valueOf(emergencies.size()));
                myEmergencyList.clear();
                myEmergencyList.addAll(emergencies);
            }
        };
        final Observer<LatLng> myObserver1 = new Observer<LatLng>() {
            @Override
            public void onChanged(LatLng latLng) {
                latLngs = latLng;
            }
        };
        MyViewModel.getInstance().getEmergencyLiveData().observe(this, myObserver);
        MyViewModel.getInstance().getLatLng().observe(this, myObserver1);

        ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        mapService.setImage(uri);
                    }
                });

        mapService = new MapService(getContext(), getActivity(), mGetContent);
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.google_map);
        mapFragment.getMapAsync(mapService);

        if (latLngs != null){
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    zoom(latLngs, 18);
                }
            });
            latLngs = null;
    } else if (person_latLng != null){
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    zoom(person_latLng, 18);
                }
            });
        }


    }

    public void zoom(LatLng latLng, float zoomLevel) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
    }

//    public void setLatLng(LatLng latLng) {
//        this.latLng = latLng;
//    }
//
//    public void setZoomLevel(float zoomLevel) {
//        zoomLevel = zoomLevel;
//    }
    boolean checkLoc(){
        return ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
