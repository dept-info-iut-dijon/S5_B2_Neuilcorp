package com.example.spotthedifference;

import java.util.List;

public class GameSession implements IGameSession {
    private String sessionId; // Cela peut être laissé nul au départ
    private List<Player> players;

    public GameSession(List<Player> players) {
        this.players = players;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public List<Player> getPlayers() {
        return players;
    }
}
