package com.example.spotthedifference;

import java.util.List;

/**
 * Représente une session de jeu, contenant un ID de session et une liste de joueurs.
 */
public class GameSession implements IGameSession {
    private String sessionId;
    private List<Player> players;

    /**
     * Constructeur de la session de jeu avec une liste de joueurs initiale.
     *
     * @param players Liste des joueurs participant à la session de jeu.
     */
    public GameSession(List<Player> players) {
        this.players = players;
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
     * Supprime un joueur de la session
     *
     */
    @Override
    public void removePlayer(Player player) {

    }

    /**
     * Représentation sous forme de chaîne de la session de jeu pour le débogage.
     *
     * @return Chaîne représentant l'ID de la session et les joueurs.
     */
    @Override
    public String toString() {
        return "GameSession{" + "sessionId='" + sessionId + '\'' + ", players=" + players + '}';
    }
}