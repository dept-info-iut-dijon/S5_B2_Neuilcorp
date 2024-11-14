package com.example.spotthedifference.models;

import java.util.List;

/**
 * Interface représentant une session de jeu, avec des méthodes pour gérer
 * l'identifiant de la session, la liste des joueurs, et les états du jeu.
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
     * Définit la liste des joueurs participant à la session de jeu.
     *
     * @param players Liste des joueurs.
     */
    void setPlayers(List<Player> players);

    /**
     * Supprime un joueur de la session de jeu.
     *
     * @param player Le joueur à supprimer.
     */
    void removePlayer(Player player);

    /**
     * Vérifie si la session de jeu est terminée.
     *
     * @return true si le jeu est terminé, sinon false.
     */
    boolean isGameCompleted();

    /**
     * Définit l'état de complétion de la session de jeu.
     *
     * @param gameCompleted true si le jeu est terminé, sinon false.
     */
    void setGameCompleted(boolean gameCompleted);

    /**
     * Obtient l'état du minuteur de la session de jeu.
     *
     * @return true si le minuteur est activé, sinon false.
     */
    boolean isGameTimer();

    /**
     * Définit l'état du minuteur de la session de jeu.
     *
     * @param gameTimer true si le minuteur est activé, sinon false.
     */
    void setGameTimer(boolean gameTimer);
}
