package com.example.spotthedifference.models;

import java.util.List;

/**
 * Interface représentant une session de jeu, avec des méthodes pour gérer
 * l'identifiant de la session et la liste des joueurs.
 */
public interface IGameSession {

    /**
     * Obtient l'identifiant de la session de jeu.
     *
     * @return L'ID de la session.
     */
    String getSessionId();

    /**
     * Définit l'identifiant de la session de jeu.
     *
     * @param sessionId L'ID de la session à définir.
     */
    void setSessionId(String sessionId);

    /**
     * Obtient la liste des joueurs participant à la session de jeu.
     *
     * @return Liste des joueurs.
     */
    List<Player> getPlayers();

    /**
     * Supprime un joueur de la session de jeu. (en prévision de la suppression d'un joueur par l'hôte)
     *
     * @param player Le joueur à supprimer.
     */
    void removePlayer(Player player);
}
