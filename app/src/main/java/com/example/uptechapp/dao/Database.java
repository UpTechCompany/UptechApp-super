package com.example.uptechapp.dao;

import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.uptechapp.api.CompleteListener;
import com.example.uptechapp.api.EmergencyApiService;
import com.example.uptechapp.model.Emergency;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Database {

    public static final String TAG = "Database";
    public static List<Emergency> EMERGENCIES_LIST = new ArrayList<>();

    public static void loadEmergencies (CompleteListener listener) {
        try {
            EmergencyApiService.getInstance().getEmergency().enqueue(new Callback<List<Emergency>>() {
                @Override
                public void onResponse(@NonNull Call<List<Emergency>> call, @NonNull Response<List<Emergency>> response) {
                    EMERGENCIES_LIST.clear();
                    EMERGENCIES_LIST.addAll(response.body());
                    MyViewModel.getInstance().getEmergencyLiveData().postValue(EMERGENCIES_LIST);
                    Log.d("MapService", "1loadEmergencies complete: " + EMERGENCIES_LIST.toString());
                }

                @Override
                public void onFailure(@NonNull Call<List<Emergency>> call, @NonNull Throwable t) {
                    t.printStackTrace();
                }
            });
            listener.OnSuccess();
            Log.d("MapService", "2loadEmergencies complete: " + EMERGENCIES_LIST.toString());
        } catch (Exception e) {
            listener.OnFailure();
        }
    }

    public static Emergency getEmergencyByTitle(String title, List<Emergency> emergencyList) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            return EMERGENCIES_LIST.stream().filter(emergency -> emergency.getTitle().equals(title)).findAny().orElseThrow(() -> new RuntimeException("not found"));
//        }
//        else {
        Log.i(TAG, "getEmergencyByTitle: " + emergencyList);
        for (Emergency emergency:
                emergencyList) {
            if(emergency.getTitle().equals(title)) {
                return emergency;
            }
        }
//        }
    return null;
    }

}
