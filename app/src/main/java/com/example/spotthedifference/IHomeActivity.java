package com.example.spotthedifference;

import android.content.Intent;
import retrofit2.Response;

public interface IHomeActivity {
    void showCreateGameDialog();
    void showJoinGameDialog();
    void createGameSession(String playerName);
    void joinGameSession(String sessionId, String playerName);
    void handleApiError(Response<?> response);
    void startActivity(Intent intent);
}
