package com.example.uptechapp.dao;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.uptechapp.R;
import com.example.uptechapp.api.EmergencyApiService;
import com.example.uptechapp.model.Emergency;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapService implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener{
    private static final String TAG = "MapService";
    private final Context context;
    private final Activity activity;
    private final List<Emergency> myEmergencyList;
    TimeZone userTimeZone = TimeZone.getDefault();
    private Uri uriImage;
    private final StorageReference storageReference;
    private final ActivityResultLauncher<String> mGetContent;
    private Dialog dialog;
    private LatLng location;

    private TextView editTextLabel;
    private Button btnChoose;
    private TextView editTextDesc;
    private Button btnShare;
    private ImageView emergencyImg;
    private int id;
    private final LatLng latLngs;
    private final LatLng person_latLng;

    public MapService(Context context, Activity activity, ActivityResultLauncher<String> mGetContent, LatLng person_latLng) {
        this.context = context;
        this.activity = activity;
        this.mGetContent = mGetContent;
        storageReference = FirebaseStorage.getInstance().getReference("Emergency");
        myEmergencyList = MyViewModel.getInstance().getEmergencyLiveData().getValue();
        this.person_latLng = person_latLng;
        latLngs = MyViewModel.getInstance().getLatLng().getValue();
    }


    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        location = latLng;

        dialog = new Dialog(context);
        dialog.setContentView(R.layout.fragment_create_emergency);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(R.drawable.round_view));
        dialog.show();

        editTextLabel = dialog.getWindow().findViewById(R.id.editTextLabel);
        btnChoose = dialog.getWindow().findViewById(R.id.btnChoosePicture);
        editTextDesc = dialog.getWindow().findViewById(R.id.editTextDescription);
        btnShare = dialog.getWindow().findViewById(R.id.btnShare);
        emergencyImg = dialog.getWindow().findViewById(R.id.emergencyImg);

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareEmergency();
            }
        });
    }

    private void openFileChooser() {
        mGetContent.launch("image/*");
    }

    public void setImage(Uri uri) {
        uriImage = uri;
        emergencyImg.setImageURI(uriImage);
    }

    private String getFileExtension(Uri uriImage) {
        ContentResolver contentResolver = activity.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uriImage));
    }

    private void shareEmergency() {
        btnShare.setEnabled(false);
        if (uriImage != null) {

            id = 1;
            if (myEmergencyList.size() != 0) {
                id = Integer.parseInt(myEmergencyList.get(myEmergencyList.size() - 1).getId()) + 1;
            }


            StorageReference fileReference = storageReference.child(id + "/Photo." + getFileExtension(uriImage));

            fileReference.putFile(uriImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUri = uri;

                            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

                            String url = downloadUri.toString();
                            String[] time = new String[0];
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                time = LocalDateTime.now(ZoneOffset.UTC).toString().substring(0, 16).split("T");
                            }
                            String emTime = time[1] + " " + time[0].substring(8) + "." + time[0].substring(5,7) + "." + time[0].substring(0,4);
                            Emergency emergency = null;
                            emergency = new Emergency(
                                    "-1",
                                    sharedPref.getString("email", "none"),
                                    editTextLabel.getText().toString(),
                                    editTextDesc.getText().toString(),
                                    emTime,
                                    url,
                                    location.latitude,
                                    location.longitude
                            );


                            EmergencyApiService.getInstance().postJson(emergency).enqueue(new Callback<Emergency>() {
                                @Override
                                public void onResponse(@NonNull Call<Emergency> call, @NonNull Response<Emergency> response) {
                                    Log.i(TAG, "Response - " + call);
                                }

                                @Override
                                public void onFailure(@NonNull Call<Emergency> call, @NonNull Throwable t) {
                                    Log.i(TAG, "FAIL - " + t.getMessage());
                                }
                            });
                            btnShare.setEnabled(true);
                        }
                    });
                    Navigation.findNavController(activity, R.id.mainFragmentContainer).navigate(R.id.fragment_emergency_feed);
                    dialog.hide();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "server off, try later", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(context, "File was not selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        googleMap.setOnMapLongClickListener(this);
        Log.i(TAG, "shareEmergency: " + myEmergencyList);

        if (latLngs != null){
            zoom(latLngs, 18, googleMap);
        }
        else if (person_latLng != null){
            zoom(person_latLng, 18, googleMap);
        }

        if (myEmergencyList != null) {
            for (Emergency emergency : myEmergencyList) {
                emergency.setLocation(emergency.getLattitude(), emergency.getLongitude());
                googleMap.addMarker(new MarkerOptions().position(emergency.getLocation()).title(emergency.getTitle()));
            }
        }

        googleMap.setOnMarkerClickListener(marker -> {
            Emergency emergency = Database.getEmergencyByTitle(marker.getTitle(), MyViewModel.getInstance().getEmergencyLiveData().getValue());
            LatLng loc = new LatLng(emergency.getLattitude(), emergency.getLongitude());

            String dateStr = emergency.getTime();
            SimpleDateFormat df = new SimpleDateFormat("HH:mm' 'dd.MM.yyyy", Locale.ENGLISH);
            df.setTimeZone(TimeZone.getTimeZone(String.valueOf(userTimeZone)));
            Date date = null;
            try {
                date = df.parse(dateStr);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            df.setTimeZone(TimeZone.getDefault());
            String formattedDate = df.format(date);

            List<Address> addresses = null;
            try {
                Geocoder geocoder = new Geocoder(context);
                addresses = geocoder.getFromLocation(emergency.getLattitude(), emergency.getLongitude(), 1);
            } catch (IOException e) {

            }
            String fullAddress = " ";
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                fullAddress = address.getAddressLine(0);
            }

            Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.dialog_fragment);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setGravity(Gravity.BOTTOM);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();

            TextView tv_name = dialog.getWindow().findViewById(R.id.tv_name);
            TextView tv_time = dialog.getWindow().findViewById(R.id.tv_time);
            TextView tv_address = dialog.getWindow().findViewById(R.id.tv_address);
            TextView tv_info = dialog.getWindow().findViewById(R.id.tv_description);

            tv_name.setText(emergency.getTitle());
            tv_address.setText(fullAddress);
            tv_info.setText(emergency.getDescription());
            tv_time.setText(formattedDate);


            ImageView imageView = dialog.getWindow().findViewById(R.id.iv_image);
            Glide.with(context).load(emergency.getPhotoUrl()).into(imageView);

            return false;
        });

    }
    public void zoom(LatLng latLng, float zoomLevel, GoogleMap googleMap) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
    }
}
