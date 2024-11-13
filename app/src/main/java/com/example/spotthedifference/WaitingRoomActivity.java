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

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
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
    private CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_room);

        // Initialisation des éléments de l'interface utilisateur
        sessionCodeTextView = findViewById(R.id.sessionCode);
        partyNameTextView = findViewById(R.id.partyName);
        playerNameTextView = findViewById(R.id.playerName);
        playersContainer = findViewById(R.id.playersContainer);

        // Configuration de Retrofit pour les appels API
        IRetrofitClient client = new RetrofitClient();
        Retrofit retrofit = client.getUnsafeRetrofit();
        apiService = retrofit.create(ApiService.class);

        // Récupération des informations passées via Intent
        sessionId = getIntent().getStringExtra("sessionId");
        playerName = getIntent().getStringExtra("playerName");
        playerId = getIntent().getStringExtra("playerId");

        // Initialisation du client SignalR et démarrage de la connexion
        signalRClient = new SignalRClient();
        signalRClient.startConnection();

        // Rejoindre le groupe de session SignalR
        signalRClient.joinSessionGroup(sessionId);

        // Affichage du code de session
        if (sessionId != null) {
            sessionCodeTextView.setText(getString(R.string.code_de_session) + " " + sessionId);
        }

        // Affichage du nom du joueur
        playerNameTextView.setText(getString(R.string.nom_du_joueur) + " " + playerName);

        // Chargement des détails de la session
        loadSessionDetails(sessionId);

        // Configuration des boutons
        Button exitButton = findViewById(R.id.exitButton);
        Button readyButton = findViewById(R.id.readyButton);
        Button copyButton = findViewById(R.id.copyButton);

        exitButton.setOnClickListener(v -> deleteSessionAndExit());
        readyButton.setOnClickListener(v -> toggleReadyStatus());
        copyButton.setOnClickListener(v -> copyToClipboard(sessionId));

        // Gestion des observables de SignalR pour suivre les événements
        disposables.add(signalRClient.getPlayerJoinedObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(playerName -> {
                    Log.d("WaitingRoomActivity", playerName + " a rejoint la session");
                    runOnUiThread(() -> loadSessionDetails(sessionId));
                }, throwable -> Log.e("WaitingRoomActivity", "Erreur PlayerJoined observable", throwable)));

        disposables.add(signalRClient.getPlayerReadyStatusChangedObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(isReady -> {
                    Log.d("WaitingRoomActivity", "Statut de préparation mis à jour : " + isReady);
                    runOnUiThread(() -> updateReadyStatusUI(playerId, isReady));
                }, throwable -> Log.e("WaitingRoomActivity", "Erreur ReadyStatus observable", throwable)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();  // Libère les observables quand l'activité est détruite
        signalRClient.stopConnection();
    }

    /**
     * Bascule le statut de préparation du joueur local et envoie l'information via SignalR.
     */
    private void toggleReadyStatus() {
        isReady = !isReady;
        signalRClient.sendReadyStatusUpdate(sessionId, playerId, isReady);
        updateReadyStatusUI(playerId, isReady);
    }

    /**
     * Met à jour l'interface utilisateur pour afficher le statut de préparation d'un joueur.
     *
     * @param playerId L'ID du joueur à mettre à jour.
     * @param isReady  Le nouveau statut de préparation.
     */
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

    /**
     * Charge les détails de la session, y compris la liste des joueurs.
     *
     * @param sessionId Identifiant de la session.
     */
    public void loadSessionDetails(String sessionId) {
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

    /**
     * Affiche la liste des joueurs dans l'interface utilisateur.
     *
     * @param players Liste des joueurs à afficher.
     */
    public void displayPlayers(List<Player> players) {
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

    /**
     * Copie le texte dans le presse-papiers du téléphone.
     *
     * @param text Texte à copier.
     */
    public void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Session ID", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Code de session copié", Toast.LENGTH_SHORT).show();
    }

    /**
     * Supprime la session actuelle et retourne à l'écran d'accueil.
     */
    public void deleteSessionAndExit() {
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