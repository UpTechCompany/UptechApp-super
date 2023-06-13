package com.example.uptechapp.dao;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.uptechapp.R;
import com.example.uptechapp.activity.MainActivityFragments;
import com.example.uptechapp.model.Emergency;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class EmergencyAdapter extends RecyclerView.Adapter<EmergencyAdapter.ViewHolder> {

    public static MainActivityFragments mActivity;
    private final List<Emergency> emergenciesList;
    static final String TAG = "AdapterEmergency";
    private final Context context;
    private static Fragment fragment;
    private static NavController  navController;

    TimeZone userTimeZone = TimeZone.getDefault();

    public EmergencyAdapter(List<Emergency> emergenciesList, Context context, NavController navController) {
        Log.d(TAG, "EmergencyAdapter: " + emergenciesList);
        this.emergenciesList = emergenciesList;
        this.context = context;
        EmergencyAdapter.navController = navController;
    }
    @NonNull
    @Override
    public EmergencyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emergency, parent, false);
        return new EmergencyAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmergencyAdapter.ViewHolder holder, int position) {
        Emergency emergency = emergenciesList.get(position);

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
            return;
        }
        String fullAddress = " ";
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            fullAddress = address.getAddressLine(0);
        }

        holder.setData(
                emergency.getTitle(),
                emergency.getDescription(),
                formattedDate,
                emergency.getPhotoUrl(),
                fullAddress,
                loc
        );
    }

    @Override
    public int getItemCount() {
        return emergenciesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView emergencyTitle, emergencyDescription, emergencyTime;

        private final ImageView emergencyPhoto;
        private final Button emergencyMapButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            emergencyTitle = itemView.findViewById(R.id.textLabel);
            emergencyDescription = itemView.findViewById(R.id.textDescription);
            emergencyTime = itemView.findViewById(R.id.textDate);
            emergencyPhoto = itemView.findViewById(R.id.imageView);
            emergencyPhoto.setClipToOutline(true);
            emergencyMapButton = itemView.findViewById(R.id.emergency_map_button);

        }

        private void setData(String title, String description, String time, String photo, String address, LatLng loc) {

        try {
            emergencyTime.setText(time);
            emergencyTitle.setText(title);
            emergencyDescription.setText(description);
            emergencyMapButton.setText(address);
            emergencyMapButton.setOnClickListener(v -> {
                MyViewModel.getInstance().getLatLng().postValue(loc);
                goToFragment();
            });
            Glide.with(context).load(photo).into(emergencyPhoto);
         } catch (Exception e) {
                Log.i(TAG, e.getMessage());
            }
        }
    }
    public static void goToFragment() {
        navController.navigate(R.id.fragment_map);
    }
}
