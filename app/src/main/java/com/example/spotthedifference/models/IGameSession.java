package com.example.spotthedifference.models;

import java.util.List;

/**
 * Interface représentant une session de jeu avec des méthodes pour gérer
 * les joueurs et les états de la session.
 */
public interface IGameSession {

    /**
     * Obtient l'identifiant unique de la session de jeu.
     *
     * @return L'ID de la session.
     */
    String getSessionId();

    /**
     * Retourne la liste des joueurs participant à la session.
     *
     * @return Liste immuable des joueurs.
     */
    List<Player> getPlayers();

    /**
     * Supprime un joueur de la session.
     *
     * @param player Le joueur à supprimer.
     */
    void removePlayer(Player player);

    /**
     * Vérifie si le jeu est terminé.
     *
     * @return true si le jeu est terminé, sinon false.
     */
    boolean isGameCompleted();

    /**
     * Vérifie si le minuteur de la session est actif.
     *
     * @return true si le minuteur est actif, sinon false.
     */
    boolean isGameTimer();
}