package com.example.spotthedifference;

import java.util.List;

public interface IGameSession {
    String getSessionId();
    void setSessionId(String sessionId);
    List<Player> getPlayers();
}
