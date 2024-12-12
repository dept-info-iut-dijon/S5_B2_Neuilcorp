package com.example.spotthedifference.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.spotthedifference.R;
import com.example.spotthedifference.api.ApiService;
import com.example.spotthedifference.api.IRetrofitClient;
import com.example.spotthedifference.api.RetrofitClient;
import com.example.spotthedifference.models.GameSession;
import com.example.spotthedifference.models.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Activité principale permettant de créer ou de rejoindre une session de jeu.
 * Gère l'interface utilisateur pour l'initialisation des parties et la navigation
 * vers la salle d'attente.
 */
public class HomeActivity extends AppCompatActivity implements IHomeActivity {

    /**
     * Service API pour les communications avec le serveur.
     */
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        IRetrofitClient client = new RetrofitClient();
        Retrofit retrofit = client.getUnsafeRetrofit();
        apiService = retrofit.create(ApiService.class);

        Button createGameButton = findViewById(R.id.createGameButton);
        Button joinGameButton = findViewById(R.id.joinGameButton);

        createGameButton.setOnClickListener(v -> showCreateGameDialog());
        joinGameButton.setOnClickListener(v -> showJoinGameDialog());
    }

    /**
     * Affiche une boîte de dialogue permettant de créer une nouvelle session de jeu.
     */
    @Override
    public void showCreateGameDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_game, null);
        final EditText playerNameInput = dialogView.findViewById(R.id.editPlayerName);

        new AlertDialog.Builder(this)
                .setTitle(R.string.Home_CreationPartie)
                .setView(dialogView)
                .setPositiveButton(R.string.Home_CreerBouton, (dialog, which) -> {
                    String playerName = playerNameInput.getText().toString().trim();
                    if (playerName.isEmpty()) {
                        showToast(getString(R.string.Home_ToastNomVide));
                    } else {
                        createGameSession(playerName);
                    }
                })
                .setNegativeButton(R.string.Home_Annuler, (dialog, which) -> dialog.dismiss())
                .create()
                .show();

        playerNameInput.requestFocus();
    }

    /**
     * Affiche une boîte de dialogue permettant de rejoindre une session de jeu existante.
     */
    @Override
    public void showJoinGameDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_join_game, null);
        final EditText sessionCodeInput = dialogView.findViewById(R.id.sessionCodeEditText);
        final EditText playerNameInput = dialogView.findViewById(R.id.playerNameEditText);

        new AlertDialog.Builder(this)
                .setTitle(R.string.Home_RejoindrePartie)
                .setView(dialogView)
                .setPositiveButton(R.string.Home_RejoindreBouton, (dialog, which) -> {
                    String sessionId = sessionCodeInput.getText().toString().trim();
                    String playerName = playerNameInput.getText().toString().trim();
                    if (sessionId.isEmpty() || playerName.isEmpty()) {
                        showToast(getString(R.string.Home_ToastCodeNomVide));
                    } else {
                        joinGameSession(sessionId, playerName);
                    }
                })
                .setNegativeButton(R.string.Home_Annuler, (dialog, which) -> dialog.dismiss())
                .create()
                .show();

        sessionCodeInput.requestFocus();
    }

    /**
     * Crée une nouvelle session de jeu avec le nom du joueur hôte.
     *
     * @param playerName Nom du joueur qui crée la session.
     */
    @Override
    public void createGameSession(String playerName) {
        Player hostPlayer = new Player(playerName);
        List<Player> players = new ArrayList<>();
        players.add(hostPlayer);
        GameSession newGameSession = new GameSession(players);
        Log.d("GameSession", newGameSession.toString());

        apiService.createSession(newGameSession).enqueue(new Callback<GameSession>() {
            @Override
            public void onResponse(Call<GameSession> call, Response<GameSession> response) {
                if (response.isSuccessful() && response.body() != null) {
                    navigateToWaitingRoom(response.body(), hostPlayer);
                } else {
                    handleApiError(response);
                }
            }

            @Override
            public void onFailure(Call<GameSession> call, Throwable t) {
                showErrorToast(R.string.Home_ErreurConnexion + t.getMessage());
            }
        });
    }

    /**
     * Rejoint une session de jeu existante avec l'ID de session et le nom du joueur.
     *
     * @param sessionId  ID de la session à rejoindre.
     * @param playerName Nom du joueur rejoignant la session.
     */
    @Override
    public void joinGameSession(String sessionId, String playerName) {
        Player player = new Player(playerName);

        apiService.joinSession(sessionId, player).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    fetchSessionAndNavigate(sessionId, playerName, player.getPlayerId());
                } else {
                    showErrorToast("Erreur lors de l'ajout à la session.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showErrorToast("Échec de la requête : " + t.getMessage());
            }
        });
    }

    /**
     * Navigue vers l'activité de salle d'attente avec les informations de session.
     *
     * @param session Session de jeu créée ou rejointe
     * @param hostPlayer Joueur hôte de la session
     */
    private void navigateToWaitingRoom(GameSession session, Player hostPlayer) {
        Intent intent = new Intent(HomeActivity.this, WaitingRoomActivity.class);
        intent.putExtra("sessionId", session.getSessionId());
        intent.putExtra("playerName", hostPlayer.getName());
        intent.putExtra("playerId", hostPlayer.getPlayerId());
        intent.putExtra("partyName", hostPlayer.getName());
        startActivity(intent);
    }

    /**
     * Récupère les détails de la session et navigue vers la salle d'attente.
     *
     * @param sessionId Identifiant de la session
     * @param playerName Nom du joueur
     * @param playerId Identifiant unique du joueur
     */
    private void fetchSessionAndNavigate(String sessionId, String playerName, String playerId) {
        apiService.getSessionById(sessionId).enqueue(new Callback<GameSession>() {
            @Override
            public void onResponse(Call<GameSession> call, Response<GameSession> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String hostName = response.body().getPlayers().get(0).getName();
                    Intent intent = new Intent(HomeActivity.this, WaitingRoomActivity.class);
                    intent.putExtra("sessionId", sessionId);
                    intent.putExtra("playerName", playerName);
                    intent.putExtra("playerId", playerId);
                    intent.putExtra("hostName", hostName);
                    startActivity(intent);
                } else {
                    showErrorToast("Erreur lors de la récupération de la session.");
                }
            }

            @Override
            public void onFailure(Call<GameSession> call, Throwable t) {
                showErrorToast("Échec de la requête : " + t.getMessage());
            }
        });
    }

    /**
     * Gère les erreurs d'API en fonction de la réponse reçue.
     */
    @Override
    public void handleApiError(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                Log.e("API Error Body", response.errorBody().string());
            }
        } catch (IOException e) {
            Log.e("IOException", e.getMessage(), e);
        }
        showErrorToast("Erreur lors de la création de la partie.");
    }

    /**
     * Affiche un message d'erreur toast pour l'utilisateur.
     *
     * @param message Message d'erreur à afficher.
     */
    public void showErrorToast(String message) {
        Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Affiche un message toast pour l'utilisateur.
     *
     * @param message Message à afficher.
     */
    public void showToast(String message) {
        Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
    }

}