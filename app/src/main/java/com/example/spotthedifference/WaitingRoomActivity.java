package com.example.spotthedifference;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class WaitingRoomActivity extends AppCompatActivity {

    private TextView sessionCodeTextView;
    private ApiService apiService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_room);

        sessionCodeTextView = findViewById(R.id.sessionCode);
        TextView partyNameTextView = findViewById(R.id.partyName);
        TextView playerNameTextView = findViewById(R.id.playerName);

        Retrofit retrofit = RetrofitClient.getUnsafeRetrofit();
        apiService = retrofit.create(ApiService.class);

        String sessionId = getIntent().getStringExtra("sessionId");
        String playerName = getIntent().getStringExtra("playerName");


        if (sessionId != null) {
            String fullText = getString(R.string.code_de_session) + " " + sessionId;
            sessionCodeTextView.setText(fullText);
        } else {
            sessionCodeTextView.setText(R.string.error_no_session_id);
        }

        if (playerName != null) {
            String fullTextName = getString(R.string.nom_de_la_partie_nom) + " " + playerName;
            partyNameTextView.setText(fullTextName);
        } else {
            partyNameTextView.setText(R.string.error_no_party_name);
        }


        if (playerName != null) {
            String fullTextName = getString(R.string.nom_joueur_hote) + " " + playerName;
            playerNameTextView.setText(fullTextName);
        } else {
            playerNameTextView.setText(R.string.error_no_party_name);
        }





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
}
