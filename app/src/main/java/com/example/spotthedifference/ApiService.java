package com.example.spotthedifference;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    @POST("api/Differance/check") // Chemin de l'endpoint du controleur
    Call<Boolean> sendCoordinates(@Body Coordonnees coordonnees);

    @GET("ImageControlleur/{id}")
    Call<ResponseBody> getImage(@Path("id") int imageId);
}
