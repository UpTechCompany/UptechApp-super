package com.example.uptechapp.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.example.uptechapp.dao.MapService;
import com.example.uptechapp.dao.MyViewModel;
import com.example.uptechapp.databinding.FragmentMapBinding;
import com.example.uptechapp.model.Emergency;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment {

    private FragmentMapBinding binding;

    private List<Emergency> myEmergencyList;
    private MapService mapService;
    private GoogleMap mMap;
    private static LatLng person_latLng = null;
    private static LatLng latLngs = null;
    ActivityResultLauncher<String[]> locationPermissionRequest;

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
                        Bitmap bitmap = null;
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
                            if (bitmap.getWidth() > 2000 || bitmap.getHeight() > 2000){
                                double scaleFactor = Math.max(bitmap.getWidth(), bitmap.getHeight()) / 2000.0;
                                int newWidth = (int) (bitmap.getWidth() / scaleFactor);
                                int newHeight = (int) (bitmap.getHeight() / scaleFactor);
                                Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
                                bitmap = newBitmap;
                                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                                String path = MediaStore.Images.Media.insertImage(getContext().getContentResolver(), bitmap, "Title", null);
                                uri =  Uri.parse(path);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        mapService.setImage(uri);
                    }
                });


        mapService = new MapService(getContext(), getActivity(), mGetContent, person_latLng);
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.google_map);
        mapFragment.getMapAsync(mapService);

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

    public void zoom(LatLng latLng, float zoomLevel) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
    }


    boolean checkLoc(){
        return ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}
