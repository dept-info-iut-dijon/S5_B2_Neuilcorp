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

        // Listener pour créer une nouvelle partie
        createGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateGameDialog();
            }
        });

        // Listener pour rejoindre une partie existante
        joinGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, WaitingRoomActivity.class);
                startActivity(intent);
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

        // Afficher le dialog
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
        playerNameInput.requestFocus();
    }

    private void createGameSession(String playerName) {
        Player hostPlayer = new Player(playerName, playerName);
        List<Player> players = new ArrayList<>();
        players.add(hostPlayer);

        GameSession newGameSession = new GameSession(players);
        newGameSession.setSessionId(UUID.randomUUID().toString()); // Generate a unique SessionId

        Call<GameSession> call = apiService.createSession(newGameSession);
        call.enqueue(new Callback<GameSession>() {
            @Override
            public void onResponse(Call<GameSession> call, Response<GameSession> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Intent intent = new Intent(HomeActivity.this, WaitingRoomActivity.class);
                    intent.putExtra("sessionId", response.body().getSessionId());
                    intent.putExtra("playerName", hostPlayer.getName());
                    startActivity(intent);
                } else {
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

            @Override
            public void onFailure(Call<GameSession> call, Throwable t) {
                Log.e("API Failure", t.getMessage(), t);
                Toast.makeText(HomeActivity.this, "Erreur de connexion : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
