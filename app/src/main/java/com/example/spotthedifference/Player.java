package com.example.spotthedifference;

public class Player {
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

    // Getters et setters
    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }
}
