package com.example.spotthedifference.ui.activities;

import android.content.Intent;
import retrofit2.Response;

/**
 * Interface contenant toutes les méthodes de HomeActivity.
 */
public interface IHomeActivity {

    /**
     * Affiche une boîte de dialogue pour créer une nouvelle partie.
     */
    void showCreateGameDialog();

    /**
     * Affiche une boîte de dialogue pour rejoindre une partie existante.
     */
    void showJoinGameDialog();

    /**
     * Crée une session de jeu avec le nom du joueur.
     *
     * @param playerName Nom du joueur qui crée la session.
     * @return true si la création de session est initiée avec succès, false sinon.
     */
    void createGameSession(String playerName);

    /**
     * Rejoint une session de jeu existante avec l'ID de session et le nom du joueur.
     *
     * @param sessionId  Identifiant de la session à rejoindre.
     * @param playerName Nom du joueur qui rejoint la session.
     * @return true si la tentative de rejoindre est initiée avec succès, false sinon.
     */
    void joinGameSession(String sessionId, String playerName);

    /**
     * Gère les erreurs d'API en fonction de la réponse reçue.
     *
     * @param response Réponse reçue de l'appel API.
     */
    void handleApiError(Response<?> response);

    /**
     * Démarre une nouvelle activité avec l'intention fournie.
     *
     * @param intent Intent pour lancer l'activité.
     */
    void startActivity(Intent intent);

    /**
     * Affiche un message Toast pour informer l'utilisateur.
     *
     * @param message Le message à afficher.
     */
    void showToast(String message);

    void showErrorToast(String message);

}