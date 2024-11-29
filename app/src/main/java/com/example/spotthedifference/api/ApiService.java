package com.example.spotthedifference.api;

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

public interface ApiService {

    /**
     * envoie les coordonnées pour vérifier les différences
     * @param coordonnees : les coordonnées à envoyer
     * @return : un booléen indiquant si les coordonnées sont correctes
     */
    @POST("api/Differance/check") // Chemin de l'endpoint du controleur
    Call<Boolean> sendCoordinates(@Body Coordonnees coordonnees);

    /**
     * Récupère une image à partir de son id
     * @param imageId : l'id de l'image
     * @return : Un appel qui renvoie l'image sous forme de corps de réponse
     */
    @GET("ImageControlleur/{id}")
    Call<ResponseBody> getImage(@Path("id") int imageId);

    /**
     * Crée une session de jeu
     * @param gameSession : les informations de la session à créer
     * @return : un appel qui renvoie l'objet GameSession créé
     */
    @POST("api/GameSession/CreateSession")
    Call<GameSession> createSession(@Body GameSession gameSession);

    /**
     * Permet à un joueur de rejoindre une session
     * @param sessionId : l'identifiant de la session
     * @param player : l'objet représentant le joueur à ajouter à la session
     * @return : un appel qui renvoie une réponse vide
     */
    @POST("api/GameSession/{sessionId}/join")
    Call<Void> joinSession(@Path("sessionId") String sessionId, @Body Player player);

    /**
     * Récupère toutes les session de jeu
     * @return : un appel qui renvoie une liste de GameSession
     */
    @GET("api/GameSession/all")
    Call<List<GameSession>> getAllSessions();

    /**
     * Récupère une session de jeu à partir de son id
     * @param sessionId : l'identifiant de la session
     * @return : un appel qui renvoie l'objet GameSession correspondant
     */
    @GET("api/GameSession/{sessionId}")
    Call<GameSession> getSessionById(@Path("sessionId") String sessionId);

    /**
     * Supprime une session de jeu
     * @param sessionId : l'identifiant de la session à supprimer
     * @return : un appel qui renvoie une réponse vide
     */
    @DELETE("api/GameSession/{sessionId}")
    Call<Void> destructiondeSession(@Path("sessionId") String sessionId);

    /**
     * Mets à jour l'état de préparation d'un joueur dans une session de jeu
     * @param sessionId : l'identifiant de la session
     * @param playerId : l'identifiant du joueur
     * @param player : les informations du joueur à mettre à jour
     * @return : un appel qui renvoie une réponse vide
     */
    @POST("api/GameSession/{sessionId}/player/{playerId}/ready")
    Call<Void> setPlayerReadyStatus(@Path("sessionId") String sessionId, @Path("playerId") String playerId, @Body Player player);

    /**
     * Récupère toutes les images
     * @return : Un appel qui renvoie une liste de chaînes représentant les identifiants des images
     */
    @GET("ImageControlleur/allImage")
    Call<List<String>> getAllImages();

    /**
     * Récupère toutes les images avec leurs paires
     * @return : un appel qui renvoie une liste d'objets ImageWithPair
     */
    @GET("ImageControlleur/allImagesWithPairs")
    Call<List<ImageWithPair>> getAllImagesWithPairs();

    /**
     * Sélectionne une paire d'images
     * @param sessionId : l'identifiant de la session
     * @param imagePairId : l'identifiant de la paire d'images
     * @return : un appel qui renvoie une réponse vide
     */
    @POST("ImageControlleur/{sessionId}/selectImagePair")
    Call<Void> selectImagePair(@Path("sessionId") String sessionId, @Body int imagePairId);

}
