// WaitingRoomActivity.java
package com.example.spotthedifference;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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

        String sessionId = getIntent().getStringExtra("sessionId");
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

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WaitingRoomActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        readyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WaitingRoomActivity.this, MainActivity.class);
                startActivity(intent);
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

                    // Set the party name and host name
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
        // Add more TextViews or handle more players as needed
    }

    private void initializeSignalR(String sessionId) {
        signalRClient = new SignalRClient("https://10.0.2.2:7176/gamehub");
        signalRClient.getHubConnection().on("PlayerJoined", (playerName) -> {
            runOnUiThread(() -> {
                Log.d("WaitingRoom", "Player joined: " + playerName);
                // Update the UI when a new player joins
                loadSessionDetails(sessionId);
            });
        }, String.class);
        signalRClient.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (signalRClient != null) {
            signalRClient.stop();
        }
    }
}