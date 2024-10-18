package com.example.spotthedifference;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    @POST("api/Differance/check") // Chemin de l'endpoint du controleur
    Call<Boolean> sendCoordinates(@Body Coordonnees coordonnees);

    @GET("ImageControlleur/{id}")
    Call<ResponseBody> getImage(@Path("id") int imageId);

    @POST("api/GameSession/CreateSession")
    Call<GameSession> createSession(@Body GameSession gameSession);

    @POST("api/GameSession/{sessionId}/join")
    Call<Void> joinSession(@Path("sessionId") String sessionId, @Body Player player);

    @GET("api/GameSession/all")
    Call<List<GameSession>> getAllSessions();

    @GET("api/GameSession/{sessionId}")
    Call<GameSession> getSessionById(@Path("sessionId") String sessionId);

    @DELETE("api/GameSession/{sessionId}")
    Call<Void> destructiondeSession(@Path("sessionId") String sessionId);

    @POST("api/GameSession/{sessionId}/player/{playerId}/ready")
    Call<Void> setPlayerReadyStatus(@Path("sessionId") String sessionId, @Path("playerId") String playerId, @Body Player player);
}
