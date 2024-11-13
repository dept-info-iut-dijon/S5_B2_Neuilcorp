package com.example.spotthedifference;

import android.content.Intent;
import retrofit2.Response;

/// <summary>
/// Interface contenant toutes les méthodes de HomeActivity.
/// </summary>
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
     */
    void createGameSession(String playerName);

    /**
     * Rejoint une session de jeu existante avec l'ID de session et le nom du joueur.
     *
     * @param sessionId  Identifiant de la session à rejoindre.
     * @param playerName Nom du joueur qui rejoint la session.
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
}
