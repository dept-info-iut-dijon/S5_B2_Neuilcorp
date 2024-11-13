package com.example.spotthedifference;

import java.util.List;

public interface IWaitingRoomActivity {

    /**
     * Charge les détails de la session.
     *
     * @param sessionId Identifiant de la session à charger.
     */
    void loadSessionDetails(String sessionId);

    /**
     * Met à jour le statut de préparation d'un joueur.
     *
     * @param playerId   Identifiant du joueur.
     * @param playerName Nom du joueur.
     * @param isReady    Statut de préparation du joueur.
     */
    void updatePlayerReadyStatus(String playerId, String playerName, boolean isReady);

    /**
     * Affiche la liste des joueurs.
     *
     * @param players Liste des joueurs à afficher.
     */
    void displayPlayers(List<Player> players);

    /**
     * Copie le texte fourni dans le presse-papiers.
     *
     * @param text Texte à copier.
     */
    void copyToClipboard(String text);

    /**
     * Supprime la session actuelle et retourne à l'activité d'accueil.
     */
    void deleteSessionAndExit();
}
