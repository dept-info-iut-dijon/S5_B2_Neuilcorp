package com.example.spotthedifference;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
    private TextView playerName1TextView;
    private TextView playerName2TextView;
    private ApiService apiService;
    private SignalRClient signalRClient;
    private String sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_room);

        sessionCodeTextView = findViewById(R.id.sessionCode);
        partyNameTextView = findViewById(R.id.partyName);
        playerNameTextView = findViewById(R.id.playerName);
        playerName1TextView = findViewById(R.id.playerName1);
        playerName2TextView = findViewById(R.id.playerName2);

        Retrofit retrofit = RetrofitClient.getUnsafeRetrofit();
        apiService = retrofit.create(ApiService.class);

        sessionId = getIntent().getStringExtra("sessionId");
        String playerName = getIntent().getStringExtra("playerName");
        String hostName = getIntent().getStringExtra("hostName");

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
                Intent intent = new Intent(WaitingRoomActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard(sessionId);
            }
        });
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
        if (players.size() > 0) {
            playerName1TextView.setText(players.get(0).getName());
        }
        if (players.size() > 1) {
            playerName2TextView.setText(players.get(1).getName());
        }
    }

    private void initializeSignalR(String sessionId) {
        signalRClient = new SignalRClient("https://10.0.2.2:7176/gamehub");
        signalRClient.getHubConnection().on("PlayerJoined", (playerName) -> {
            runOnUiThread(() -> {
                Log.d("WaitingRoom", "Player joined: " + playerName);
                loadSessionDetails(sessionId);
            });
        }, String.class);
        signalRClient.start();
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
        if (signalRClient != null) {
            signalRClient.stop();
        }
    }
}