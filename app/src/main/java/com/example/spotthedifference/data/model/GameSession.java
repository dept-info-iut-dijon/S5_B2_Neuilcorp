package com.example.spotthedifference.data.model;
import java.util.List;


/**
 * Classe représentant une session de jeu.
 */
public class GameSession {
    private String sessionId; // Cela peut être laissé nul au départ
    private List<Player> players;

    /**
     * Constructeur pour initialiser une session de jeu avec une liste de joueurs.
     *
     * @param players La liste des joueurs participant à cette session de jeu.
     */
    public GameSession(List<Player> players) {
        this.players = players;
    }

    /**
     * Récupère l'identifiant de la session de jeu.
     *
     * @return L'identifiant de la session de jeu.
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Définit l'identifiant de la session de jeu.
     *
     * @param sessionId L'identifiant de la session de jeu à définir.
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Récupère la liste des joueurs participant à cette session de jeu.
     *
     * @return La liste des joueurs participant à cette session de jeu.
     */
    public List<Player> getPlayers() {
        return players;
    }
}
