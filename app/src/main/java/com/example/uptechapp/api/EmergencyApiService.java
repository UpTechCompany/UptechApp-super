package com.example.uptechapp.api;

import com.example.uptechapp.dao.RetrofitService;

public class EmergencyApiService {
    private static EmergencyApi emergencyApi;

    private static EmergencyApi create() {
        return RetrofitService.getInstance().create(EmergencyApi.class);
    }

    public static EmergencyApi getInstance() {
        if (emergencyApi == null) {
            emergencyApi = create();
        }
        return emergencyApi;
    }
}
