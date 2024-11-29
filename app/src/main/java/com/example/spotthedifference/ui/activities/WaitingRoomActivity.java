package com.example.spotthedifference.ui.activities;

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

import com.example.spotthedifference.R;
import com.example.spotthedifference.WebSocket.SignalRClient;
import com.example.spotthedifference.api.ApiService;
import com.example.spotthedifference.api.IRetrofitClient;
import com.example.spotthedifference.api.RetrofitClient;
import com.example.spotthedifference.models.GameSession;
import com.example.spotthedifference.models.Player;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.android.schedulers.AndroidSchedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * La classe WaitingRoomActivity représente l'activité de la salle d'attente
 * dans l'application. Elle permet aux joueurs de se connecter à une session
 * de jeu en attendant les autres participants avant de démarrer la partie.
 */
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
    private List<Player> players = new ArrayList<>();

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
        signalRClient.requestSync(sessionId);

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
        Button chooseImageButton = findViewById(R.id.chooseImageButton);

        chooseImageButton.setOnClickListener(v -> {
            if (isHost()) {
                Intent intent = new Intent(WaitingRoomActivity.this, ImagesActivity.class);
                intent.putExtra("sessionId", sessionId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Seul l'hôte peut sélectionner une image.", Toast.LENGTH_SHORT).show();
            }
        });

        exitButton.setOnClickListener(v -> deleteSessionAndExit());
        readyButton.setOnClickListener(v -> toggleReadyStatus());
        copyButton.setOnClickListener(v -> copyToClipboard(sessionId));

        // Gestion des événements de synchronisation
        disposables.add(signalRClient.getSyncSessionStateObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe(session -> displayPlayers(session.getPlayers()),
                        throwable -> Log.e("WaitingRoomActivity", "Erreur SyncSessionState", throwable)));

        // Gestion des événements PlayerJoined
        disposables.add(signalRClient.getPlayerJoinedObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe(playerName -> loadSessionDetails(sessionId),
                        throwable -> Log.e("WaitingRoomActivity", "Erreur PlayerJoined observable", throwable)));

        // Gestion des événements PlayerReadyStatusChanged
        disposables.add(signalRClient.getPlayerReadyStatusChangedObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(player -> {
                    Log.d("WaitingRoomActivity", "Statut de préparation de " + player.getPlayerId() + " mis à jour : " + player.isReady());
                    loadSessionDetails(sessionId);
                }, throwable -> Log.e("WaitingRoomActivity", "Erreur PlayerReadyStatusChanged observable", throwable)));

        disposables.add(signalRClient.getSessionClosedObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(sessionId -> {
                    Toast.makeText(this, "La session a été fermée par l'hôte.", Toast.LENGTH_SHORT).show();
                    redirectToHome();
                }, throwable -> Log.e("WaitingRoomActivity", "Erreur SessionClosed observable", throwable)));

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

            if (playerNameView.getTag().equals(playerId)) {
                TextView playerStatusView = playerView.findViewById(R.id.playerStatus);
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
                    players = response.body().getPlayers();
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
            playerNameView.setTag(player.getPlayerId()); // Associe l'ID du joueur à la vue
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
     * Gère le départ d'un joueur ou la suppression d'une session si l'hôte quitte.
     */
    public void deleteSessionAndExit() {
        if (isHost()) {
            // L'hôte quitte, supprimer la session
            apiService.removePlayerFromSession(sessionId, playerId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        // Notifie tous les joueurs via SignalR que la session est supprimée
                        signalRClient.notifySessionClosed(sessionId);
                        redirectToHomeForAllPlayers();
                    } else {
                        Log.e("WaitingRoom", "Échec de la suppression de la session : " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("WaitingRoom", "Échec de la requête : " + t.getMessage());
                }
            });
        } else {
            // Un joueur non-hôte quitte, retirer seulement ce joueur
            apiService.removePlayerFromSession(sessionId, playerId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        redirectToHome();
                    } else {
                        Log.e("WaitingRoom", "Échec du retrait du joueur : " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("WaitingRoom", "Échec de la requête : " + t.getMessage());
                }
            });
        }
    }

    /**
     * Redirige vers l'écran d'accueil pour tous les joueurs (SignalR).
     */
    private void redirectToHomeForAllPlayers() {
        // Notifie tous les joueurs qu'ils doivent revenir à l'accueil
        signalRClient.notifyAllPlayersToExit(sessionId);
        redirectToHome();
    }


    private boolean isHost() {
        return !players.isEmpty() && playerId.equals(players.get(0).getPlayerId());
    }

    /**
     * Redirige vers l'activité HomeActivity.
     */
    private void redirectToHome() {
        Intent intent = new Intent(WaitingRoomActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}