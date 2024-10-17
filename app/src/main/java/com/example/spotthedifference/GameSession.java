package com.example.spotthedifference;
import java.util.List;

public class GameSession
{
    public String SessionId;
    public List<Player> Players;
    public boolean GameCompleted;
    public boolean GameTimer;

    public GameSession(List<Player> players){
        this.Players = players;
        this.GameCompleted = false;
        this.GameTimer = true;
    }
}

