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
import androidx.appcompat.app.AlertDialog;

import com.example.spotthedifference.R;
import com.example.spotthedifference.WebSocket.GameStartedListener;
import com.example.spotthedifference.WebSocket.SignalRClient;
import com.example.spotthedifference.api.ApiService;
import com.example.spotthedifference.api.IRetrofitClient;
import com.example.spotthedifference.api.RetrofitClient;
import com.example.spotthedifference.models.GameSession;
import com.example.spotthedifference.models.Player;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
public class WaitingRoomActivity extends AppCompatActivity implements IWaitingRoomActivity, GameStartedListener{

    /**
     * TextView affichant le code de la session.
     */
    private TextView sessionCodeTextView;

    /**
     * TextView affichant le nom de la partie.
     */
    private TextView partyNameTextView;

    /**
     * TextView affichant le nom du joueur.
     */
    private TextView playerNameTextView;

    /**
     * Conteneur pour la liste des joueurs.
     */
    private LinearLayout playersContainer;

    /**
     * Service pour les appels API.
     */
    private ApiService apiService;

    /**
     * Identifiant unique de la session.
     */
    private String sessionId;

    /**
     * Nom du joueur.
     */
    private String playerName;

    /**
     * ID du joueur.
     */
    private String playerId;

    /**
     * Statut de préparation du joueur.
     */
    private boolean isReady = false;

    /**
     * Client pour la communication en temps réel.
     */
    private SignalRClient signalRClient;

    /**
     * Gestionnaire des souscriptions aux observables.
     */
    private CompositeDisposable disposables = new CompositeDisposable();

    /**
     * Liste des joueurs.
     */
    private List<Player> players = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_room);

        // Initialisation des composants
        initializeUI();
        initializeRetrofit();
        initializeSignalRClient();

        // Charger les détails de la session et configurer les boutons
        loadSessionDetails(sessionId);
        setupButtons();

        // Gérer les observables pour SignalR
        setupSignalREvents();
    }

    /**
     * Initialise les composants de l'interface utilisateur et récupère les données transmises via Intent.
     * Configure les textes pour afficher le code de session et le nom du joueur dans l'interface.
     */
    private void initializeUI() {
        sessionCodeTextView = findViewById(R.id.sessionCode);
        partyNameTextView = findViewById(R.id.partyName);
        playerNameTextView = findViewById(R.id.playerName);
        playersContainer = findViewById(R.id.playersContainer);

        sessionId = getIntent().getStringExtra("sessionId");
        playerName = getIntent().getStringExtra("playerName");
        playerId = getIntent().getStringExtra("playerId");

        if (sessionId != null) {
            sessionCodeTextView.setText(getString(R.string.Home_code_de_session) + " " + sessionId);
        }

        playerNameTextView.setText(getString(R.string.Waiting_nom_du_joueur) + " " + playerName);
    }

    /**
     * Initialise la configuration Retrofit pour les appels API.
     * Crée une instance de Retrofit via un client sécurisé ou non sécurisé
     * et configure le service API.
     */
    private void initializeRetrofit() {
        IRetrofitClient client = new RetrofitClient();
        Retrofit retrofit = client.getUnsafeRetrofit();
        apiService = retrofit.create(ApiService.class);
    }

    /**
     * Initialise le client SignalR pour gérer la communication en temps réel.
     * Configure les événements nécessaires et démarre la connexion au serveur.
     * Rejoint le groupe de session et demande une synchronisation de l'état.
     */
    private void initializeSignalRClient() {
        signalRClient = new SignalRClient(playerId);
        signalRClient.setGameStartedListener(this);
        signalRClient.startConnection();
        signalRClient.joinSessionGroup(sessionId);
        signalRClient.requestSync(sessionId);
    }

    /**
     * Configure les observables SignalR pour gérer les événements en temps réel.
     * S'abonne aux différents événements tels que les changements d'état de session,
     * les joueurs qui rejoignent/quittent, les messages de notification, etc.
     * Met à jour l'interface utilisateur en fonction des événements reçus.
     */
    private void setupSignalREvents() {
        disposables.add(signalRClient.getSyncSessionStateObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(session -> displayPlayers(session.getPlayers()),
                        throwable -> Log.e("WaitingRoomActivity", "Erreur SyncSessionState", throwable)));

        disposables.add(signalRClient.getPlayerJoinedObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(playerName -> loadSessionDetails(sessionId),
                        throwable -> Log.e("WaitingRoomActivity", "Erreur PlayerJoined observable", throwable)));

        disposables.add(signalRClient.getPlayerRemovedObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(playerName -> loadSessionDetails(sessionId),
                        throwable -> Log.e("WaitingRoomActivity", "Erreur PlayerRemoved observable", throwable)));

        disposables.add(signalRClient.getPlayerReadyStatusChangedObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(player -> {
                    Log.d("WaitingRoomActivity", "Statut de préparation de " + player.getPlayerId() + " mis à jour : " + player.isReady());
                    loadSessionDetails(sessionId);
                }, throwable -> Log.e("WaitingRoomActivity", "Erreur PlayerReadyStatusChanged observable", throwable)));

        disposables.add(signalRClient.getSessionDeletedObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(deletedSessionId -> {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "La session a été fermée par l'hôte.", Toast.LENGTH_SHORT).show();
                        redirectToHome();
                    });
                }, throwable -> Log.e("WaitingRoomActivity", "Erreur SessionDeleted observable", throwable)));

        disposables.add(signalRClient.getReadyNotAllowedObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(message -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Attention")
                            .setMessage(message)
                            .setPositiveButton("OK", null)
                            .show();
                }, throwable -> Log.e("WaitingRoomActivity", "Erreur lors de la gestion de ReadyNotAllowed", throwable)));

        disposables.add(signalRClient.getNotifyMessageObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(message -> {
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                }, throwable -> Log.e("WaitingRoomActivity", "Erreur lors de la gestion de NotifyMessage", throwable)));
    }

    /**
     * Configure les boutons de l'interface utilisateur et leurs actions associées.
     * - `chooseImageButton` : Permet à l'hôte de sélectionner une image.
     * - `readyButton` : Permet de basculer le statut de préparation du joueur.
     * - `copyButton` : Copie le code de session dans le presse-papiers.
     * - `exitButton` : Permet de quitter la session ou de la supprimer si l'utilisateur est l'hôte.
     */
    private void setupButtons() {
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
                Toast.makeText(this, R.string.Waiting_Toast_NonHoteSelectImage, Toast.LENGTH_SHORT).show();
            }
        });

        readyButton.setOnClickListener(v -> toggleReadyStatus());
        copyButton.setOnClickListener(v -> copyToClipboard(sessionId));
        exitButton.setOnClickListener(v -> deleteSessionAndExit());
    }



    /**
     * Méthode appelée lorsque le jeu commence, elle reçoit les données de l'image
     * L'image est ensuite envoyée à l'activité principale via un intent
     * l'ID de session est également passé à l'activité pour référence
     * @param imageData
     */
    @Override
    public void onGameStarted(byte[] imageData, Integer imagePairId) {
        Log.d("WaitingRoomActivity", "Image reçue via GameStartedCallback. Taille : " + imageData.length + " imagePairId : " + imagePairId);

        File imageFile = new File(getCacheDir(), "tempImage.jpg");

        try {
            if (!imageFile.getParentFile().exists()) {
                boolean dirCreated = imageFile.getParentFile().mkdirs();
                if (!dirCreated) {
                    Log.e("WaitingRoomActivity", "Impossible de créer le répertoire parent pour le fichier temporaire.");
                    Toast.makeText(this, R.string.Waiting_Toast_ErreurCreationImageTemp, Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                fos.write(imageData);
            }

            Intent intent = new Intent(WaitingRoomActivity.this, MainActivity.class);
            intent.putExtra("imagePath", imageFile.getAbsolutePath());
            intent.putExtra("sessionId", sessionId);
            intent.putExtra("playerId", playerId);
            intent.putExtra("imagePairId", imagePairId.toString());
            startActivity(intent);
            finish();
        } catch (IOException e) {
            Log.e("WaitingRoomActivity", "Erreur lors de l'écriture ou de l'accès au fichier temporaire : " + e.getMessage(), e);
            Toast.makeText(this, R.string.Waiting_Toast_ErreurSauvegardeImageTemp, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Méthode appelée lorsque l'activité est détruite.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();  // Libère les observables quand l'activité est détruite
        signalRClient.stopConnection(sessionId, playerId);
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
                playerStatusView.setText(isReady ? R.string.Waiting_pret : R.string.Waiting_pas_pret);
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
                        String fullTextParty = getString(R.string.Waiting_nom_de_la_partie_nom) + " " + hostName;
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
            playerStatusView.setText(player.isReady() ? R.string.Waiting_pret : R.string.Waiting_pas_pret);
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
        Toast.makeText(this, R.string.Waiting_Toast_SessionCodeCopie, Toast.LENGTH_SHORT).show();
    }

    /**
     * Gère le départ d'un joueur ou la suppression d'une session si l'hôte quitte.
     */
    public void deleteSessionAndExit() {
        if (isHost()) {
            apiService.removePlayerFromSession(sessionId, playerId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        redirectToHome();
                    } else {
                        Log.e("WaitingRoom", "Erreur lors de la suppression de la session : " 
                                + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("WaitingRoom", "Échec de la requête de suppression de la session : " 
                            + t.getMessage());
                }
            });
        } else {
            apiService.removePlayerFromSession(sessionId, playerId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        redirectToHome();
                    } else {
                        Log.e("WaitingRoom", "Erreur lors du retrait du joueur (code: " 
                                + response.code() + ") - playerId: " + playerId);
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("WaitingRoom", "Échec de la requête de retrait du joueur : " 
                            + t.getMessage());
                }
            });
        }
    }

    /**
     * Vérifie si le joueur actuel est hôte ou non.
     * @return true si le joueur est hôte, false s'il ne l'est pas.
     */
    private boolean isHost() {
        return !players.isEmpty() && playerId.equals(players.get(0).getPlayerId());
    }

    /**
     * Redirige vers l'activité HomeActivity.
     */
    private void redirectToHome() {
        Intent intent = new Intent(WaitingRoomActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}