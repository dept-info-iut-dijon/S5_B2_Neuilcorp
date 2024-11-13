package com.example.spotthedifference;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.spotthedifference.WebSocket.SignalRClient;
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
    private String playerId;
    private boolean isReady = false;
    private SignalRClient signalRClient;

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
        playerId = getIntent().getStringExtra("playerId");

        signalRClient = new SignalRClient();
        signalRClient.startConnection();

        signalRClient.joinSessionGroup(sessionId);

        if (sessionId != null) {
            sessionCodeTextView.setText(getString(R.string.code_de_session) + " " + sessionId);
        }

        playerNameTextView.setText(getString(R.string.nom_du_joueur) + " " + playerName);

        loadSessionDetails(sessionId);

        Button exitButton = findViewById(R.id.exitButton);
        Button readyButton = findViewById(R.id.readyButton);
        Button copyButton = findViewById(R.id.copyButton);

        exitButton.setOnClickListener(v -> deleteSessionAndExit());

        readyButton.setOnClickListener(v -> toggleReadyStatus());

        copyButton.setOnClickListener(v -> copyToClipboard(sessionId));

        // Écouter les changements de statut de préparation via SignalR
        signalRClient.getConnection().on("PlayerReadyStatusChanged", (changedPlayerId, newReadyStatus) -> {
            Log.d("WaitingRoomActivity", "Statut de préparation changé pour " + changedPlayerId + ": " + newReadyStatus);
            runOnUiThread(() -> updateReadyStatusUI(changedPlayerId, newReadyStatus));
        }, String.class, Boolean.class);

        // Écouter les nouveaux joueurs qui rejoignent via SignalR
        signalRClient.getConnection().on("PlayerJoined", (newPlayerName) -> {
            Log.d("WaitingRoomActivity", newPlayerName + " a rejoint la session");
            runOnUiThread(() -> loadSessionDetails(sessionId));  // Recharge les détails pour inclure le nouveau joueur
        }, String.class);
    }

    /// <summary>
    /// Bascule le statut de préparation du joueur local et envoie l'information au serveur via SignalR.
    /// </summary>
    private void toggleReadyStatus() {
        isReady = !isReady;
        Log.d("WaitingRoomActivity", "Bouton Prêt cliqué");
        Log.d("WaitingRoomActivity", "Session ID: " + sessionId);
        Log.d("WaitingRoomActivity", "Player ID: " + playerId);
        Log.d("WaitingRoomActivity", "Nouvel état de préparation: " + isReady);

        signalRClient.sendReadyStatusUpdate(sessionId, playerId, isReady);
        updateReadyStatusUI(playerId, isReady);
    }

    /// <summary>
    /// Met à jour l'interface utilisateur pour afficher le statut de préparation d'un joueur.
    /// </summary>
    /// <param name="playerId">L'ID du joueur à mettre à jour.</param>
    /// <param name="isReady">Le nouveau statut de préparation.</param>
    private void updateReadyStatusUI(String playerId, boolean isReady) {
        for (int i = 0; i < playersContainer.getChildCount(); i++) {
            View playerView = playersContainer.getChildAt(i);
            TextView playerNameView = playerView.findViewById(R.id.playerName);
            TextView playerStatusView = playerView.findViewById(R.id.playerStatus);

            if (playerNameView.getTag().equals(playerId)) {
                playerStatusView.setText(isReady ? R.string.pret : R.string.pas_pret);
                playerStatusView.setTextColor(isReady ? getResources().getColor(R.color.success_color) : getResources().getColor(R.color.error_color));
                break;
            }
        }
    }

    /// <summary>
    /// Charge les détails de la session, y compris les joueurs, et les affiche dans l'interface.
    /// </summary>
    private void loadSessionDetails(String sessionId) {
        apiService.getSessionById(sessionId).enqueue(new Callback<GameSession>() {
            @Override
            public void onResponse(Call<GameSession> call, Response<GameSession> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Player> players = response.body().getPlayers();
                    displayPlayers(players);
                    if (!players.isEmpty()) {
                        String hostName = players.get(0).getName();
                        String fullTextParty = getString(R.string.nom_de_la_partie_nom) + " " + hostName;
                        partyNameTextView.setText(fullTextParty);
                    }
                }
                else {
                    Log.e("WaitingRoom", "Erreur lors de la récupération des détails de la session : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GameSession> call, Throwable t) {
                Log.e("WaitingRoom", "Échec de la requête : " + t.getMessage());
            }
        });
    }

    /// <summary>
    /// Affiche la liste des joueurs dans le conteneur d'interface utilisateur.
    /// </summary>
    private void displayPlayers(List<Player> players) {
        playersContainer.removeAllViews();
        for (Player player : players) {
            View playerView = LayoutInflater.from(this).inflate(R.layout.player_item, playersContainer, false);
            TextView playerNameView = playerView.findViewById(R.id.playerName);
            TextView playerStatusView = playerView.findViewById(R.id.playerStatus);

            playerNameView.setText(player.getName());

            playerNameView.setTag(player.getPlayerId());
            playerStatusView.setText(player.isReady() ? R.string.pret : R.string.pas_pret);
            playerStatusView.setTextColor(player.isReady() ? getResources().getColor(R.color.success_color) : getResources().getColor(R.color.error_color));

            playersContainer.addView(playerView);
        }
    }


    /// <summary>
    /// Copie le texte fourni dans le presse-papiers du téléphone.
    /// </summary>
    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Session ID", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Code de session copié", Toast.LENGTH_SHORT).show();
    }

    /// <summary>
    /// Supprime la session et retourne à l'écran d'accueil.
    /// </summary>
    private void deleteSessionAndExit() {
        apiService.destructiondeSession(sessionId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    startActivity(new Intent(WaitingRoomActivity.this, HomeActivity.class));
                } else {
                    Toast.makeText(WaitingRoomActivity.this, "Erreur lors de la suppression de la session", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(WaitingRoomActivity.this, "Échec de la requête : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
