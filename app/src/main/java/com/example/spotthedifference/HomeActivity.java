package com.example.spotthedifference;

import android.content.DialogInterface;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class HomeActivity extends AppCompatActivity {

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Retrofit retrofit = RetrofitClient.getUnsafeRetrofit();
        apiService = retrofit.create(ApiService.class);

        Button createGameButton = findViewById(R.id.createGameButton);
        Button joinGameButton = findViewById(R.id.joinGameButton);

        createGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateGameDialog();
            }
        });

        joinGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showJoinGameDialog();
            }
        });
    }

    private void showCreateGameDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_create_game, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(dialogView);

        final EditText playerNameInput = dialogView.findViewById(R.id.editPlayerName);

        dialogBuilder
                .setTitle("Créer une partie")
                .setPositiveButton("Créer", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String playerName = playerNameInput.getText().toString().trim();

                        if (playerName.isEmpty()) {
                            Toast.makeText(HomeActivity.this, "Veuillez entrer un nom de joueur.", Toast.LENGTH_SHORT).show();
                        } else {
                            createGameSession(playerName);
                        }
                    }
                })
                .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
        playerNameInput.requestFocus();
    }

    private void showJoinGameDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_join_game, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(dialogView);

        final EditText sessionCodeInput = dialogView.findViewById(R.id.sessionCodeEditText);
        final EditText playerNameInput = dialogView.findViewById(R.id.playerNameEditText);

        dialogBuilder
                .setTitle("Rejoindre une partie")
                .setPositiveButton("Rejoindre", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String sessionId = sessionCodeInput.getText().toString().trim();
                        String playerName = playerNameInput.getText().toString().trim();

                        if (sessionId.isEmpty() || playerName.isEmpty()) {
                            Toast.makeText(HomeActivity.this, "Veuillez entrer un code de session et un nom de joueur.", Toast.LENGTH_SHORT).show();
                        } else {
                            joinGameSession(sessionId, playerName);
                        }
                    }
                })
                .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
        sessionCodeInput.requestFocus();
    }

    private void createGameSession(String playerName) {
        Player hostPlayer = new Player(playerName, playerName);
        List<Player> players = new ArrayList<>();
        players.add(hostPlayer);

        GameSession newGameSession = new GameSession(players);
        newGameSession.setSessionId(UUID.randomUUID().toString());

        Call<GameSession> call = apiService.createSession(newGameSession);
        call.enqueue(new Callback<GameSession>() {
            @Override
            public void onResponse(Call<GameSession> call, Response<GameSession> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Intent intent = new Intent(HomeActivity.this, WaitingRoomActivity.class);
                    intent.putExtra("sessionId", response.body().getSessionId());
                    intent.putExtra("playerName", hostPlayer.getName());
                    intent.putExtra("partyName", hostPlayer.getName());
                    startActivity(intent);
                } else {
                    handleApiError(response);
                }
            }

            @Override
            public void onFailure(Call<GameSession> call, Throwable t) {
                Log.e("API Failure", t.getMessage(), t);
                Toast.makeText(HomeActivity.this, "Erreur de connexion : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void joinGameSession(String sessionId, String playerName) {
        String playerId = UUID.randomUUID().toString();
        Player player = new Player(playerId, playerName);

        Call<Void> call = apiService.joinSession(sessionId, player);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    apiService.getSessionById(sessionId).enqueue(new Callback<GameSession>() {
                        @Override
                        public void onResponse(Call<GameSession> call, Response<GameSession> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                String hostName = response.body().getPlayers().get(0).getName();
                                Intent intent = new Intent(HomeActivity.this, WaitingRoomActivity.class);
                                intent.putExtra("sessionId", sessionId);
                                intent.putExtra("playerName", playerName);
                                intent.putExtra("hostName", hostName);
                                startActivity(intent);
                            } else {
                                Log.e("JoinSession", "Erreur lors de la récupération de la session : " + response.code());
                                Toast.makeText(HomeActivity.this, "Erreur lors de la récupération de la session", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<GameSession> call, Throwable t) {
                            Log.e("JoinSession", "Échec de la requête : " + t.getMessage());
                            Toast.makeText(HomeActivity.this, "Échec de la requête : " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.e("JoinSession", "Erreur lors de l'ajout à la session : " + response.code());
                    Toast.makeText(HomeActivity.this, "Erreur lors de l'ajout à la session", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("JoinSession", "Échec de la requête : " + t.getMessage());
                Toast.makeText(HomeActivity.this, "Échec de la requête : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleApiError(Response<?> response) {
        Log.e("API Error", "Code: " + response.code() + ", Message: " + response.message());
        try {
            if (response.errorBody() != null) {
                Log.e("API Error Body", response.errorBody().string());
            }
        } catch (IOException e) {
            Log.e("IOException", e.getMessage(), e);
        }
        Toast.makeText(HomeActivity.this, "Erreur lors de la création de la partie.", Toast.LENGTH_SHORT).show();
    }
}
