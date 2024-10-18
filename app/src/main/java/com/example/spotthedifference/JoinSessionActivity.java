// JoinSessionActivity.java
package com.example.spotthedifference;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class JoinSessionActivity extends AppCompatActivity {

    private EditText sessionCodeEditText;
    private EditText playerNameEditText;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_session);

        sessionCodeEditText = findViewById(R.id.sessionCodeEditText);
        playerNameEditText = findViewById(R.id.playerNameEditText);
        Button joinSessionButton = findViewById(R.id.joinSessionButton);

        Retrofit retrofit = RetrofitClient.getUnsafeRetrofit();
        apiService = retrofit.create(ApiService.class);

        joinSessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sessionId = sessionCodeEditText.getText().toString().trim();
                String playerName = playerNameEditText.getText().toString().trim();

                if (!sessionId.isEmpty() && !playerName.isEmpty()) {
                    joinSession(sessionId, playerName);
                } else {
                    Toast.makeText(JoinSessionActivity.this, "Veuillez entrer un code de session et un nom de joueur", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void joinSession(String sessionId, String playerName) {
        String playerId = UUID.randomUUID().toString();
        Player player = new Player(playerId, playerName);

        Call<Void> call = apiService.joinSession(sessionId, player);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("JoinSession", "Joueur ajouté à la session");

                    // Retrieve the session to get the host's name
                    apiService.getSessionById(sessionId).enqueue(new Callback<GameSession>() {
                        @Override
                        public void onResponse(Call<GameSession> call, Response<GameSession> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                String hostName = response.body().getPlayers().get(0).getName(); // Assuming the host is the first player
                                Intent intent = new Intent(JoinSessionActivity.this, WaitingRoomActivity.class);
                                intent.putExtra("sessionId", sessionId);
                                intent.putExtra("playerName", playerName);
                                intent.putExtra("hostName", hostName);
                                startActivity(intent);
                            } else {
                                Log.e("JoinSession", "Erreur lors de la récupération de la session : " + response.code());
                                Toast.makeText(JoinSessionActivity.this, "Erreur lors de la récupération de la session", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<GameSession> call, Throwable t) {
                            Log.e("JoinSession", "Échec de la requête : " + t.getMessage());
                            Toast.makeText(JoinSessionActivity.this, "Échec de la requête : " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.e("JoinSession", "Erreur lors de l'ajout à la session : " + response.code());
                    Toast.makeText(JoinSessionActivity.this, "Erreur lors de l'ajout à la session", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("JoinSession", "Échec de la requête : " + t.getMessage());
                Toast.makeText(JoinSessionActivity.this, "Échec de la requête : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}