package com.example.spotthedifference.models;

public class Player implements IPlayer {
    private String playerId;
    private String name;
    private boolean isReady;

    // Constructeur par défaut nécessaire pour la sérialisation
    public Player() {
    }

    // Constructeur avec paramètres sauf état
    public Player(String playerId, String name) {
        this.playerId = playerId;
        this.name = name;
        this.isReady = false;
    }

    // Constructeur avec paramètres sauf nom
    public Player(String playerId, boolean isReady) {
        this.playerId = playerId;
        this.isReady = isReady;
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
