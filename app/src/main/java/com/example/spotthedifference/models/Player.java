package com.example.spotthedifference.models;

public class Player implements IPlayer {
    private String playerId;
    private String name;
    private boolean isReady;

    // Constructeur par défaut nécessaire pour la sérialisation
    public Player() {
    }

    // Constructeur avec paramètres
    public Player(String playerId, String name) {
        this.playerId = java.util.UUID.randomUUID().toString();
        this.name = name;
        this.isReady = false;
    }

    @Override
    public String getPlayerId() {
        return playerId;
    }

    @Override
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
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
