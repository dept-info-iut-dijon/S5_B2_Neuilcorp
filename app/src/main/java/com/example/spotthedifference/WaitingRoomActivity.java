package com.example.spotthedifference;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class WaitingRoomActivity extends AppCompatActivity implements IWaitingRoomActivity {

    private TextView sessionCodeTextView;
    private TextView partyNameTextView;
    private TextView playerNameTextView;
    private LinearLayout playersContainer;
    private ApiService apiService;
    private String sessionId;
    private String playerName;
    private String hostName;
    private String playerId;
    private boolean isReady = false;
    private Handler handler = new Handler();
    private Runnable refreshPlayerListRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_room);

        sessionCodeTextView = findViewById(R.id.sessionCode);
        partyNameTextView = findViewById(R.id.partyName);
        playerNameTextView = findViewById(R.id.playerName);
        playersContainer = findViewById(R.id.playersContainer);

        IRetrofitClient client = new RetrofitClient();
        Retrofit retrofit = client.getUnsafeRetrofit();
        apiService = retrofit.create(ApiService.class);

        sessionId = getIntent().getStringExtra("sessionId");
        playerName = getIntent().getStringExtra("playerName");
        hostName = getIntent().getStringExtra("hostName");
        playerId = getIntent().getStringExtra("playerId");

        loadSessionDetails(sessionId);

        Button exitButton = findViewById(R.id.exitButton);
        Button readyButton = findViewById(R.id.readyButton);
        Button copyButton = findViewById(R.id.copyButton);

        exitButton.setOnClickListener(v -> deleteSessionAndExit());
        readyButton.setOnClickListener(v -> {
            isReady = !isReady;
            updatePlayerReadyStatus(playerId, playerName, isReady);
        });
        copyButton.setOnClickListener(v -> copyToClipboard(sessionId));

        startPeriodicPlayerListRefresh();
    }

    @Override
    public void loadSessionDetails(String sessionId) {
        apiService.getSessionById(sessionId).enqueue(new Callback<GameSession>() {
            @Override
            public void onResponse(Call<GameSession> call, Response<GameSession> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GameSession session = response.body();

                    sessionCodeTextView.setText("Code de session : " + session.getSessionId());
                    partyNameTextView.setText("Partie de : " + hostName);
                    playerNameTextView.setText("Votre nom : " + playerName);

                    displayPlayers(session.getPlayers());
                } else {
                    Log.e("WaitingRoom", "Erreur lors de la récupération des détails de la session : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GameSession> call, Throwable t) {
                Log.e("WaitingRoom", "Échec de la requête : " + t.getMessage());
            }
        });
    }

    @Override
    public void updatePlayerReadyStatus(String playerId, String playerName, boolean isReady) {
        Player player = new Player(playerId, playerName);
        player.setReady(isReady);

        apiService.setPlayerReadyStatus(sessionId, playerId, player).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    loadSessionDetails(sessionId);
                } else {
                    Log.e("WaitingRoom", "Erreur lors de la mise à jour du statut : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("WaitingRoom", "Échec de la requête : " + t.getMessage());
            }
        });
    }

    @Override
    public void displayPlayers(List<Player> players) {
        playersContainer.removeAllViews();
        for (Player player : players) {
            View playerView = LayoutInflater.from(this).inflate(R.layout.player_item, playersContainer, false);
            TextView playerNameView = playerView.findViewById(R.id.playerName);
            TextView playerStatusView = playerView.findViewById(R.id.playerStatus);

            playerNameView.setText(player.getName());
            playerStatusView.setText(player.isReady() ? "Prêt" : "Pas prêt");
            playersContainer.addView(playerView);
        }
    }

    @Override
    public void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Session ID", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Code de session copié", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void deleteSessionAndExit() {
        apiService.destructiondeSession(sessionId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    startActivity(new Intent(WaitingRoomActivity.this, HomeActivity.class));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(WaitingRoomActivity.this, "Erreur de suppression", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPeriodicPlayerListRefresh();
    }

    private void startPeriodicPlayerListRefresh() {
        refreshPlayerListRunnable = () -> {
            loadSessionDetails(sessionId);
            handler.postDelayed(refreshPlayerListRunnable, 5000);
        };
        handler.post(refreshPlayerListRunnable);
    }

    private void stopPeriodicPlayerListRefresh() {
        handler.removeCallbacks(refreshPlayerListRunnable);
    }
}
