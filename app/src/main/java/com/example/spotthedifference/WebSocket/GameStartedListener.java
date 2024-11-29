package com.example.spotthedifference.WebSocket;

public interface GameStartedListener {
    void onGameStarted(byte[] imageData, Integer imagePairId);
}