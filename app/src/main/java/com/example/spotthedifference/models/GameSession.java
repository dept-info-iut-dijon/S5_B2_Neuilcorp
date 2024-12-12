package com.example.spotthedifference.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Représente une session de jeu avec une gestion centralisée de ses propriétés
 * et de son comportement. Cette classe implémente IGameSession pour garantir
 * une interface cohérente pour toutes les sessions de jeu.
 */
public class GameSession implements IGameSession {
    
    /**
     * Identifiant unique de la session, généré automatiquement et non modifiable.
     */
    private final String sessionId;
    
    /**
     * Liste des joueurs participant à la session.
     * Cette liste est immuable pour éviter les modifications externes non contrôlées.
     */
    private final List<Player> players;
    
    /**
     * État indiquant si la partie est terminée.
     */
    private boolean gameCompleted;
    
    /**
     * État du minuteur de la partie.
     */
    private boolean gameTimer;

    /**
     * Constructeur par défaut pour créer une session avec un ID généré automatiquement.
     */
    public GameSession() {
        this.sessionId = UUID.randomUUID().toString();
        this.players = new ArrayList<>();
        this.gameCompleted = false;
        this.gameTimer = false;
    }

    /**
     * Constructeur avec une liste initiale de joueurs.
     *
     * @param players Liste des joueurs participant à la session de jeu.
     *                Si null, une liste vide sera créée.
     */
    public GameSession(List<Player> players) {
        this.sessionId = UUID.randomUUID().toString();
        this.players = players != null ? new ArrayList<>(players) : new ArrayList<>();
        this.gameCompleted = false;
        this.gameTimer = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSessionId() {
        return sessionId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    /**
     * Ajoute un joueur à la session si celui-ci n'est pas déjà présent.
     *
     * @param player Le joueur à ajouter.
     * @throws IllegalArgumentException si le joueur est null.
     */
    public void addPlayer(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Le joueur ne peut pas être null");
        }
        if (!players.contains(player)) {
            players.add(player);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removePlayer(Player player) {
        players.remove(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGameCompleted() {
        return gameCompleted;
    }

    /**
     * Marque la session comme terminée.
     * Cette méthode est à usage interne uniquement.
     */
    public void completeGame() {
        this.gameCompleted = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGameTimer() {
        return gameTimer;
    }

    /**
     * Active ou désactive le minuteur de la session.
     * Cette méthode est à usage interne uniquement.
     *
     * @param isActive true pour activer le minuteur, false pour le désactiver.
     */
    public void setGameTimer(boolean isActive) {
        this.gameTimer = isActive;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("GameSession{sessionId='%s', players=%s, gameCompleted=%b, gameTimer=%b}",
                sessionId, players, gameCompleted, gameTimer);
    }
}