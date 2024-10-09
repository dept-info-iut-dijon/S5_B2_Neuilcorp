package com.example.spotthedifference;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("coordonnees") // Chemin de l'endpoint du controleur
    Call<Boolean> sendCoordinates(@Body Coordonnees coordonnees);
}
