package com.example.spotthedifference.api;

import android.util.Log;

import com.example.spotthedifference.models.Coordonnees;
import com.example.spotthedifference.models.GameSession;
import com.example.spotthedifference.models.Player;
import com.example.spotthedifference.models.ImageWithPair;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("api/Differance/check")
    Call<ApiResponse> sendCoordinates(@Body Coordonnees coordonnees,@Query("sessionId") String sessionId,@Query("playerId") String playerId,@Query("imageId") String imagePairId);

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

    @GET("ImageControlleur/allImage")
    Call<List<String>> getAllImages();

    @GET("ImageControlleur/allImagesWithPairs")
    Call<List<ImageWithPair>> getAllImagesWithPairs();

    @POST("ImageControlleur/{sessionId}/selectImagePair")
    Call<Void> selectImagePair(@Path("sessionId") String sessionId, @Body int imagePairId);

}
