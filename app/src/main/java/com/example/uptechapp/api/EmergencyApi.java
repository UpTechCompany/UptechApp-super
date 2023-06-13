package com.example.uptechapp.api;

import com.example.uptechapp.model.Emergency;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface EmergencyApi {

    @POST("emergency")
    Call<Emergency> postJson(@Body Emergency emergency);

    @GET("emergency")
    Call<List<Emergency>> getEmergency();

    @GET("emergency/{id}")
    Call<Emergency> getEmergency(
            @Path("id")
            long id
    );

    @DELETE("emergency/{id}")
    Call<Void> deleteEmergency(
            @Path("id")
            long id
    );
}
