package com.example.spotthedifference;

public class Player {
    private String playerId; // ID du joueur
    private String name;      // Nom du joueur

    // Constructeur
    public Player(String playerId, String name) {
        this.playerId = java.util.UUID.randomUUID().toString();
        this.name = name;
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
}
