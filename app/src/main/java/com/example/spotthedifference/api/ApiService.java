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
import retrofit2.http.Query;

/**
 * Interface représentant les services de communication avec l'API backend de l'application.
 *
 * Cette interface définit toutes les opérations disponibles pour interagir avec le serveur backend via HTTP.
 * Elle utilise Retrofit pour faciliter la mise en œuvre des appels RESTful.
 * Les méthodes définies ici couvrent les fonctionnalités liées aux sessions de jeu,
 * à la gestion des joueurs, à la vérification des différences dans les images,
 * et au contrôle des paires d'images.
 *
 * Chaque méthode correspond à un appel HTTP spécifique et retourne un objet `Call`
 * permettant de gérer les réponses et les erreurs.
 */
public interface ApiService {

    /**
     * Envoie les coordonnées pour vérifier les différences.
     *
     * @param coordonnees Les coordonnées à envoyer.
     * @param sessionId   L'identifiant de la session.
     * @param playerId    L'identifiant du joueur.
     * @param imagePairId L'identifiant de la paire d'images.
     * @return Un appel Retrofit indiquant si les coordonnées sont correctes.
     */
    @POST("api/Differance/check")
    Call<Void> sendCoordinates(@Body Coordonnees coordonnees, @Query("sessionId") String sessionId, @Query("playerId") String playerId, @Query("imageId") String imagePairId);

    /**
     * Récupère une image à partir de son ID.
     *
     * @param imageId L'ID de l'image.
     * @return Un appel Retrofit renvoyant l'image sous forme de corps de réponse.
     */
    @GET("ImageControlleur/{id}")
    Call<ResponseBody> getImage(@Path("id") int imageId);

    /**
     * Crée une nouvelle session de jeu.
     *
     * @param gameSession Les informations de la session à créer.
     * @return Un appel Retrofit renvoyant l'objet GameSession créé.
     */
    @POST("api/GameSession/CreateSession")
    Call<GameSession> createSession(@Body GameSession gameSession);

    /**
     * Permet à un joueur de rejoindre une session existante.
     *
     * @param sessionId L'identifiant de la session.
     * @param player    L'objet représentant le joueur à ajouter à la session.
     * @return Un appel Retrofit renvoyant une réponse vide.
     */
    @POST("api/GameSession/{sessionId}/join")
    Call<Void> joinSession(@Path("sessionId") String sessionId, @Body Player player);

    /**
     * Récupère toutes les sessions de jeu disponibles.
     *
     * @return Un appel Retrofit renvoyant une liste d'objets GameSession.
     */
    @GET("api/GameSession/all")
    Call<List<GameSession>> getAllSessions();

    /**
     * Récupère une session de jeu à partir de son ID.
     *
     * @param sessionId L'identifiant de la session.
     * @return Un appel Retrofit renvoyant l'objet GameSession correspondant.
     */
    @GET("api/GameSession/{sessionId}")
    Call<GameSession> getSessionById(@Path("sessionId") String sessionId);

    /**
     * Supprime une session de jeu existante.
     *
     * @param sessionId L'identifiant de la session à supprimer.
     * @return Un appel Retrofit renvoyant une réponse vide.
     */
    @DELETE("api/GameSession/{sessionId}")
    Call<Void> destructiondeSession(@Path("sessionId") String sessionId);

    /**
     * Récupère toutes les images avec leurs paires associées.
     *
     * @return Un appel Retrofit renvoyant une liste d'objets ImageWithPair.
     */
    @GET("ImageControlleur/allImagesWithPairs")
    Call<List<ImageWithPair>> getAllImagesWithPairs();

    /**
     * Sélectionne une paire d'images pour une session donnée.
     *
     * @param sessionId   L'identifiant de la session.
     * @param imagePairId L'identifiant de la paire d'images.
     * @return Un appel Retrofit renvoyant une réponse vide.
     */
    @POST("ImageControlleur/{sessionId}/selectImagePair")
    Call<Void> selectImagePair(@Path("sessionId") String sessionId, @Body int imagePairId);

    /**
     * Gère le départ d'un joueur ou la suppression d'une session si l'hôte quitte
     * @param sessionId : l'identifiant de la session
     * @param playerId : l'identifiant du joueur partant
     * @return : un appel qui renvoie une réponse vide
     */
    @DELETE("api/GameSession/{sessionId}/player/{playerId}/remove")
    Call<Void> removePlayerFromSession(@Path("sessionId") String sessionId, @Path("playerId") String playerId);
}