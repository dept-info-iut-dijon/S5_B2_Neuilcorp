package com.example.spotthedifference;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/// <summary>
/// Classe principale de l'application.
/// Gère l'affichage principal, la détection des clics, et l'affichage d'un cercle rouge.
/// </summary>
public class MainActivity extends AppCompatActivity {

    private float clickX;
    private float clickY;
    private ImageView circleImageView;
    private ApiService apiService;

    /// <summary>
    /// Méthode appelée à la création de l'activité.
    /// </summary>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageView = findViewById(R.id.imageView);
        FrameLayout layout = findViewById(R.id.frameLayout);


        circleImageView = new ImageView(this);
        circleImageView.setImageResource(R.drawable.cercle_rouge);
        circleImageView.setVisibility(View.INVISIBLE);
        layout.addView(circleImageView, new FrameLayout.LayoutParams(50, 50));

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://localhost:7176/api/Differance/check?x=160&y=160") // Remplacer l'url
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);


        imageView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                clickX = event.getX();
                clickY = event.getY();

                circleImageView.setX(clickX - 25);
                circleImageView.setY(clickY - 25);
                circleImageView.setVisibility(View.VISIBLE);

                v.performClick();

                Toast.makeText(MainActivity.this, "Clic détecté : X=" + clickX + ", Y=" + clickY, Toast.LENGTH_SHORT).show();
                Coordonnees coordonnees = new Coordonnees(clickX, clickY);
                EnvoieCoordonneesAServeur(coordonnees);
                return true;
            }
            return false;
        });
    }

    private void EnvoieCoordonneesAServeur(float clickX, float clickY) {
         Coordonnees coordonnees = new Coordonnees(clickX, clickY);

        Call<Boolean> call = apiService.sendCoordinates(coordonnees);
        call.enqueue(new Callback<Boolean>() {

            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean result = response.body();
                    if (result) {
                        Toast.makeText(MainActivity.this, "Coordonnée envoyée avec succès", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Erreur dans l'envoi de la coordonnée", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Erreur lors de l'envoi de la coordonnée", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Erreur lors de l'envoi des coordonnées", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

