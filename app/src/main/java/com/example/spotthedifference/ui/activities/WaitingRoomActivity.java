package com.example.spotthedifference.ui.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.example.spotthedifference.models.Player;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class WaitingRoomActivity extends AppCompatActivity {

    private TextView sessionCodeTextView;
    private TextView partyNameTextView;
    private TextView playerNameTextView;
    private LinearLayout playersContainer;
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
        sessionCodeTextView.setText(getString(R.string.code_de_session) + " " + sessionId);
        playerNameTextView.setText(getString(R.string.nom_du_joueur) + " " + playerName);

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

// Création d'un Handler pour le thread principal
        Handler mainThreadHandler = new Handler(Looper.getMainLooper());

// Gestion des observables de SignalR
        disposables.add(signalRClient.getPlayerListObservable()
                .subscribeOn(Schedulers.io())
                .subscribe(players -> mainThreadHandler.post(() -> displayPlayers(players)),
                        throwable -> Log.e("WaitingRoomActivity", "Erreur PlayerList observable", throwable)));

        disposables.add(signalRClient.getAlertObservable()
                .subscribeOn(Schedulers.io())
                .subscribe(alertMessage -> mainThreadHandler.post(() ->
                                Toast.makeText(this, alertMessage, Toast.LENGTH_LONG).show()),
                        throwable -> Log.e("WaitingRoomActivity", "Erreur Alert observable", throwable)));

        disposables.add(signalRClient.getImageObservable()
                .subscribeOn(Schedulers.io())
                .subscribe(imageBytes -> mainThreadHandler.post(() ->
                                redirectToMainActivity(imageBytes)),
                        throwable -> Log.e("WaitingRoomActivity", "Erreur ReceiveImage observable", throwable)));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();  // Libère les observables
        signalRClient.stopConnection();
    }

    /**
     * Bascule le statut de préparation du joueur local et envoie l'information via SignalR.
     */
    private void toggleReadyStatus() {
        isReady = !isReady;
        signalRClient.sendReadyStatusUpdate(sessionId, playerId, isReady);
    }

    /**
     * Affiche la liste des joueurs dans l'interface utilisateur.
     *
     * @param players Liste des joueurs mise à jour.
     */
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

    /**
     * Redirige vers MainActivity lorsque le joueur reçoit une image.
     *
     * @param imageBytes Données de l'image reçue.
     */
    private void redirectToMainActivity(byte[] imageBytes) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("imageBytes", imageBytes);
        startActivity(intent);
        finish();
    }

    /**
     * Copie le texte dans le presse-papiers du téléphone.
     *
     * @param text Texte à copier.
     */
    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Session ID", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Code de session copié", Toast.LENGTH_SHORT).show();
    }

    /**
     * Supprime la session actuelle et retourne à l'écran d'accueil.
     */
    private void deleteSessionAndExit() {
        signalRClient.stopConnection(); // Arrête la connexion SignalR
        Toast.makeText(this, "Session terminée.", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(WaitingRoomActivity.this, HomeActivity.class));
        finish();
    }

    private boolean isHost() {
        return true;
    }
}