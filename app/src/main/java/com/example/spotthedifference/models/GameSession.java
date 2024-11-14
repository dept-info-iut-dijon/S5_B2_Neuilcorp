package com.example.spotthedifference.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente une session de jeu, contenant un ID de session, une liste de joueurs, un statut de complétion de jeu et un timer de jeu.
 */
public class GameSession implements IGameSession {
    private String sessionId;
    private List<Player> players;
    private boolean gameCompleted;
    private boolean gameTimer;

    /**
     * Constructeur de la session de jeu avec une liste de joueurs initiale.
     *
     * @param players Liste des joueurs participant à la session de jeu.
     */
    public GameSession(List<Player> players) {
        this.players = players != null ? players : new ArrayList<>();
        this.gameCompleted = false;
        this.gameTimer = false;
    }

    /**
     * Constructeur vide pour créer une session de jeu sans joueurs initialisés.
     */
    public GameSession() {
        this.players = new ArrayList<>();
        this.gameCompleted = false;
        this.gameTimer = false;
    }

    /**
     * Obtient l'ID de la session de jeu.
     *
     * @return L'ID de la session.
     */
    @Override
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Définit l'ID de la session de jeu.
     *
     * @param sessionId L'ID de la session.
     */
    @Override
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Obtient la liste des joueurs participant à la session de jeu.
     *
     * @return Liste des joueurs.
     */
    @Override
    public List<Player> getPlayers() {
        return players;
    }

    /**
     * Définit la liste des joueurs participant à la session de jeu.
     *
     * @param players Liste des joueurs.
     */
    @Override
    public void setPlayers(List<Player> players) {
        this.players = players != null ? players : new ArrayList<>();
    }

    /**
     * Vérifie si le jeu est terminé.
     *
     * @return True si le jeu est terminé, sinon false.
     */
    public boolean isGameCompleted() {
        return gameCompleted;
    }

    /**
     * Définit le statut de complétion du jeu.
     *
     * @param gameCompleted True si le jeu est terminé, sinon false.
     */
    public void setGameCompleted(boolean gameCompleted) {
        this.gameCompleted = gameCompleted;
    }

    /**
     * Vérifie si le timer de jeu est actif.
     *
     * @return True si le timer est actif, sinon false.
     */
    public boolean isGameTimer() {
        return gameTimer;
    }

    /**
     * Définit le statut du timer de jeu.
     *
     * @param gameTimer True pour activer le timer, sinon false.
     */
    public void setGameTimer(boolean gameTimer) {
        this.gameTimer = gameTimer;
    }

    /**
     * Supprime un joueur de la session.
     *
     * @param player Le joueur à supprimer.
     */
    @Override
    public void removePlayer(Player player) {
        players.remove(player);
    }

    /**
     * Représentation sous forme de chaîne de la session de jeu pour le débogage.
     *
     * @return Chaîne représentant l'ID de la session, le statut et les joueurs.
     */
    @Override
    public String toString() {
        return "GameSession{" +
                "sessionId='" + sessionId + '\'' +
                ", players=" + players +
                ", gameCompleted=" + gameCompleted +
                ", gameTimer=" + gameTimer +
                '}';
    }
}
