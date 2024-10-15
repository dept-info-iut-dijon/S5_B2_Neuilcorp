package com.example.spotthedifference;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/// <summary>
/// Classe principale de l'application.
/// Gère l'affichage principal, la détection des clics, la validation des différences, et l'affichage d'un cercle rouge.
/// </summary>
public class MainActivity extends AppCompatActivity {

    private int clickX;
    private int clickY;
    private ImageView circleImageView;
    //private List<Coordonnees> listeCoordonnees;
    private Coordonnees coordTemp;
    private Button validerButton;



    /// <summary>
    /// Méthode appelée à la création de l'activité.
    /// </summary>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageView = findViewById(R.id.imageView);
        FrameLayout layout = findViewById(R.id.frameLayout);
        validerButton = findViewById(R.id.validateButton);

        //listeCoordonnees = new ArrayList<>();
        validerButton.setEnabled(false);



        circleImageView = new ImageView(this);
        circleImageView.setImageResource(R.drawable.cercle_rouge);
        circleImageView.setVisibility(View.INVISIBLE);
        layout.addView(circleImageView, new FrameLayout.LayoutParams(50, 50));

        // Création de l'intercepteur de logs
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);  // Afficher tout le corps des requêtes/réponses

// Ajout de l'intercepteur au client OkHttp
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)  // Ajout du logging interceptor
                .build();

// Configuration de Retrofit avec OkHttp et Gson converter
        Retrofit retrofit = RetrofitClient.getUnsafeRetrofit();
        apiService = retrofit.create(ApiService.class);



        imageView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                int clickX = (int) event.getX();  // Conversion en int
                int clickY = (int) event.getY();  // Conversion en int
                coordTemp = new Coordonnees(clickX, clickY);

                circleImageView.setX(clickX - 25);
                circleImageView.setY(clickY - 25);
                circleImageView.setVisibility(View.VISIBLE);

                validerButton.setEnabled(true);

                Toast.makeText(MainActivity.this, "Clic détecté, prêt à valider", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        validerButton.setOnClickListener(v -> {
            if (coordTemp != null) {
                //listeCoordonnees.add(coordTemp);

                Toast.makeText(MainActivity.this, "Coordonnées validées et ajoutées à la liste", Toast.LENGTH_SHORT).show();

                validerButton.setEnabled(false);
                imageView.setEnabled(false);
                circleImageView.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void loadImage(int imageId) {
        Call<byte[]> call = apiService.getImage(imageId);
        call.enqueue(new Callback<byte[]>() {
            @Override
            public void onResponse(Call<byte[]> call, Response<byte[]> response) {
                if (response.isSuccessful() && response.body() != null) {
                    byte[] imageBytes = response.body();
                    Bitmap bitmap = ImageConverter.convertBytesToBitmap(imageBytes);
                    ImageDisplayer.displayImage(imageView, bitmap);
                } else {
                    Toast.makeText(MainActivity.this, "1", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<byte[]> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Échec de la connexion : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void EnvoieCoordonneesAServeur(Coordonnees coordonnees) {

        Call<Boolean> call = apiService.sendCoordinates(coordonnees);
        call.enqueue(new Callback<Boolean>() {

            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean result = response.body();
                    if (result) {
                        Toast.makeText(MainActivity.this, "Coordonnée envoyée avec succès", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "1", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "2", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Erreur : " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}

