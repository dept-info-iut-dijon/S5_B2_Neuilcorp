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

public class WaitingRoomActivity extends AppCompatActivity {

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

        Retrofit retrofit = RetrofitClient.getUnsafeRetrofit();
        apiService = retrofit.create(ApiService.class);

        sessionId = getIntent().getStringExtra("sessionId");
        playerName = getIntent().getStringExtra("playerName");
        hostName = getIntent().getStringExtra("hostName");
        playerId = getIntent().getStringExtra("playerId");

        if (sessionId != null) {
            String fullText = getString(R.string.code_de_session) + " " + sessionId;
            sessionCodeTextView.setText(fullText);
        } else {
            sessionCodeTextView.setText(R.string.error_no_session_id);
        }

        if (hostName != null) {
            String fullTextHost = getString(R.string.nom_joueur_hote) + " " + hostName;
            playerNameTextView.setText(fullTextHost);
        } else {
            playerNameTextView.setText(R.string.error_no_host_name);
        }

        if (playerName != null) {
            String fullTextName = getString(R.string.nom_du_joueur) + " " + playerName;
            playerNameTextView.setText(fullTextName);
        } else {
            playerNameTextView.setText(R.string.error_no_host_name);
        }

        loadSessionDetails(sessionId);

        Button exitButton = findViewById(R.id.exitButton);
        Button readyButton = findViewById(R.id.readyButton);
        Button copyButton = findViewById(R.id.copyButton);

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSessionAndExit();
            }
        });

        readyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isReady = !isReady;
                Log.d("WaitingRoom", "Ready button clicked, new state: " + isReady);
                updatePlayerReadyStatus(playerId, playerName, isReady);
            }
        });

        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard(sessionId);
            }
        });

        startPeriodicPlayerListRefresh();
    }

    private void updatePlayerReadyStatus(String playerId, String playerName, boolean isReady) {
        Log.d("WaitingRoom", "Sending ready status: " + isReady + " for playerId: " + playerId);
        Player player = new Player(playerId, playerName);
        player.setReady(isReady);

        apiService.setPlayerReadyStatus(sessionId, playerId, player).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("WaitingRoom", "Statut de préparation mis à jour avec succès");
                    loadSessionDetails(sessionId);
                } else {
                    Log.e("WaitingRoom", "Erreur lors de la mise à jour du statut : " + response.code());
                    Log.e("WaitingRoom", "Message: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("WaitingRoom", "Échec de la requête : " + t.getMessage());
            }
        });
    }


    private void updatePlayerStatusUI(Player player) {
        for (int i = 0; i < playersContainer.getChildCount(); i++) {
            View playerView = playersContainer.getChildAt(i);
            TextView playerNameView = playerView.findViewById(R.id.playerName);
            TextView playerStatusView = playerView.findViewById(R.id.playerStatus);

            if (playerNameView.getText().toString().equals(player.getName())) {
                playerStatusView.setText(player.isReady() ? "Prêt" : "Pas prêt");
                playerStatusView.setTextColor(player.isReady() ?
                        getResources().getColor(R.color.success_color) :
                        getResources().getColor(R.color.error_color));
            }
        }
    }

    private void loadSessionDetails(String sessionId) {
        apiService.getSessionById(sessionId).enqueue(new Callback<GameSession>() {
            @Override
            public void onResponse(Call<GameSession> call, Response<GameSession> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GameSession session = response.body();
                    List<Player> players = session.getPlayers();
                    displayPlayers(players);

                    if (!players.isEmpty()) {
                        String hostName = players.get(0).getName();
                        String fullTextParty = getString(R.string.nom_de_la_partie_nom) + " " + hostName;
                        partyNameTextView.setText(fullTextParty);
                    }
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

    private void displayPlayers(List<Player> players) {
        playersContainer.removeAllViews();

        for (Player player : players) {
            View playerView = LayoutInflater.from(this).inflate(R.layout.player_item, playersContainer, false);
            TextView playerNameView = playerView.findViewById(R.id.playerName);
            TextView playerStatusView = playerView.findViewById(R.id.playerStatus);

            playerNameView.setText(player.getName());
            playerStatusView.setText(player.isReady() ? "Prêt" : "Pas prêt");
            playerStatusView.setTextColor(player.isReady() ?
                    getResources().getColor(R.color.success_color) :
                    getResources().getColor(R.color.error_color));

            playersContainer.addView(playerView);
        }
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Session ID", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Code de session copié dans le presse-papier", Toast.LENGTH_SHORT).show();
    }

    private void deleteSessionAndExit() {
        apiService.destructiondeSession(sessionId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("WaitingRoom", "Session deleted successfully");
                    Intent intent = new Intent(WaitingRoomActivity.this, HomeActivity.class);
                    startActivity(intent);
                } else {
                    Log.e("WaitingRoom", "Error deleting session: " + response.code() + " " + response.message());
                    Toast.makeText(WaitingRoomActivity.this, "Erreur lors de la suppression de la session", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("WaitingRoom", "Request failed: " + t.getMessage());
                Toast.makeText(WaitingRoomActivity.this, "Échec de la requête : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPeriodicPlayerListRefresh();
    }

    private void startPeriodicPlayerListRefresh() {
        refreshPlayerListRunnable = new Runnable() {
            @Override
            public void run() {
                loadSessionDetails(sessionId);
                handler.postDelayed(this, 5000);
            }
        };
        handler.post(refreshPlayerListRunnable);
    }

    private void stopPeriodicPlayerListRefresh() {
        if (refreshPlayerListRunnable != null) {
            handler.removeCallbacks(refreshPlayerListRunnable);
        }
    }
}
