package com.example.spotthedifference.models;

import java.util.UUID;

/**
 * Représente un joueur participant à une session de jeu.
 */
public class Player implements IPlayer {
    private final String playerId; // Généré automatiquement
    private String name;
    private boolean isReady;

    /**
     * Constructeur par défaut nécessaire pour la sérialisation.
     */
    public Player() {
        this.playerId = UUID.randomUUID().toString();
        this.isReady = false;
    }

    /**
     * Constructeur avec nom du joueur.
     *
     * @param name Nom du joueur.
     */
    public Player(String name) {
        this.playerId = UUID.randomUUID().toString();
        this.name = name;
        this.isReady = false;
    }

    /**
     * Constructeur complet (utilisé en interne uniquement si nécessaire).
     *
     * @param name    Nom du joueur.
     * @param isReady Statut de préparation du joueur.
     */
    public Player(String name, boolean isReady) {
        this.playerId = UUID.randomUUID().toString();
        this.name = name;
        this.isReady = isReady;
    }

    @Override
    public String getPlayerId() {
        return playerId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isReady() {
        return isReady;
    }

    @Override
    public void setReady(boolean ready) {
        isReady = ready;
    }
}