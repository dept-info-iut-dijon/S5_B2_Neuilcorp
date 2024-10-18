package com.example.spotthedifference;
import java.util.List;

public class GameSession {
    private String sessionId; // Cela peut être laissé nul au départ
    private List<Player> players;

    public GameSession(List<Player> players) {
        this.players = players;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public List<Player> getPlayers() {
        return players;
    }
}
