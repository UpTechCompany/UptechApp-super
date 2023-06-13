package com.example.uptechapp.dao;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.uptechapp.model.Emergency;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class MyViewModel extends ViewModel {

    private static MyViewModel instance;

    public static MyViewModel getInstance(){
        if (instance == null){
            instance = new MyViewModel();
        }
        return instance;
    }
    private MutableLiveData<List<Emergency>> emergencyLiveData;

    public MutableLiveData<List<Emergency>> getEmergencyLiveData(){
        if (emergencyLiveData == null){
            emergencyLiveData = new MutableLiveData<List<Emergency>>();
        }
        return emergencyLiveData;
    }

    private MutableLiveData<LatLng> latLng;

    public MutableLiveData<LatLng> getLatLng() {
        if (latLng == null){
            latLng = new MutableLiveData<LatLng>();
        }
        return latLng;
    }
}