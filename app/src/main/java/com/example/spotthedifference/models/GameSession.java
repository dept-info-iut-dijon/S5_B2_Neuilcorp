package com.example.spotthedifference.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Représente une session de jeu avec une gestion centralisée de ses propriétés
 * et de son comportement.
 */
public class GameSession implements IGameSession {
    private final String sessionId; // Défini à l'intérieur, non modifiable
    private final List<Player> players; // Liste immuable pour éviter les modifications extérieures
    private boolean gameCompleted; // Gestion interne
    private boolean gameTimer; // Gestion interne

    /**
     * Constructeur par défaut pour créer une session avec un ID généré automatiquement.
     */
    public GameSession() {
        this.sessionId = UUID.randomUUID().toString(); // Génère un ID unique
        this.players = new ArrayList<>();
        this.gameCompleted = false;
        this.gameTimer = false;
    }

    /**
     * Constructeur avec une liste initiale de joueurs.
     *
     * @param players Liste des joueurs participant à la session de jeu.
     */
    public GameSession(List<Player> players) {
        this.sessionId = UUID.randomUUID().toString(); // Génère un ID unique
        this.players = players != null ? new ArrayList<>(players) : new ArrayList<>();
        this.gameCompleted = false;
        this.gameTimer = false;
    }

    /**
     * Retourne l'ID unique de la session, non modifiable.
     *
     * @return L'ID de la session.
     */
    @Override
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Retourne une liste immuable des joueurs participant à la session.
     *
     * @return Liste immuable des joueurs.
     */
    @Override
    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    /**
     * Ajoute un joueur à la session.
     *
     * @param player Le joueur à ajouter.
     */
    public void addPlayer(Player player) {
        if (player != null && !players.contains(player)) {
            players.add(player);
        }
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
     * Vérifie si le jeu est terminé.
     *
     * @return true si le jeu est terminé, sinon false.
     */
    @Override
    public boolean isGameCompleted() {
        return gameCompleted;
    }

    /**
     * Marque la session comme terminée. Gestion interne uniquement.
     */
    public void completeGame() {
        this.gameCompleted = true;
    }

    /**
     * Vérifie si le minuteur est actif.
     *
     * @return true si le minuteur est actif, sinon false.
     */
    @Override
    public boolean isGameTimer() {
        return gameTimer;
    }

    /**
     * Active ou désactive le minuteur. Gestion interne uniquement.
     *
     * @param isActive true pour activer le minuteur, sinon false.
     */
    public void setGameTimer(boolean isActive) {
        this.gameTimer = isActive;
    }

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