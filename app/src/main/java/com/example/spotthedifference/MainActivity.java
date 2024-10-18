package com.example.spotthedifference;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

    private ImageView imageView;
    private ApiService apiService;
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

        imageView = findViewById(R.id.imageView);
        FrameLayout layout = findViewById(R.id.frameLayout);
        validerButton = findViewById(R.id.validateButton);
        Button exitButton = findViewById(R.id.exitButton);

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

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

        int imageId = 1;
        loadImage(imageId);

        imageView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                int clickX = (int) event.getX();  // Conversion en int
                int clickY = (int) event.getY();  // Conversion en int
                coordTemp = new Coordonnees(clickX, clickY);

                circleImageView.setX(clickX - 25);
                circleImageView.setY(clickY - 25);
                circleImageView.setVisibility(View.VISIBLE);

                validerButton.setEnabled(true);

              Toast.makeText(MainActivity.this, "Clic détecté : X=" + clickX + ", Y=" + clickY, Toast.LENGTH_SHORT).show();
                //Coordonnees coordonnees = new Coordonnees(clickX, clickY);
                // EnvoieCoordonneesAServeur(coordonnees);

                return true;
            }
            return false;
        });

        validerButton.setOnClickListener(v -> {
            if (coordTemp != null) {
                //listeCoordonnees.add(coordTemp);

               AlertDialog waitingDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Joueur en attente")
                        .setMessage("En attente de la sélection des autres joueurs...")
                       .setCancelable(false)
                       .create();
               waitingDialog.show();

               EnvoieCoordonneesAServeur(coordTemp, waitingDialog);
               validerButton.setEnabled(false);
               imageView.setEnabled(false);
               circleImageView.setVisibility(View.INVISIBLE);
               imageView.setEnabled(true);

            }
        });
    }

    private void loadImage(int imageId) {
        Call<ResponseBody> call = apiService.getImage(imageId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        byte[] imageBytes = response.body().bytes();
                        Log.d("ImageLoad", "Image size : " + imageBytes.length);
                        Bitmap bitmap = ImageConverter.convertBytesToBitmap(imageBytes);
                        ImageDisplayer.displayImage(imageView, bitmap);
                    }catch(IOException e)
                    {
                        Log.e("ImageLoad", "Erreur lors de la lecture de l'image : " + e.getMessage());
                    }
                } else {
                    Log.e("ImageLoad", "Erreur lors du chargement de l'image : " + response.code());
                    Toast.makeText(MainActivity.this, "Erreur lors du chargement de l'image : " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("ImageLoad", "Échec de la connexion : " + t.getMessage());
                Toast.makeText(MainActivity.this, "Échec de la connexion : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void EnvoieCoordonneesAServeur(Coordonnees coordonnees, AlertDialog waitingDialog) {

        Call<Boolean> call = apiService.sendCoordinates(coordonnees);
        call.enqueue(new Callback<Boolean>() {

            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean result = response.body();
                    String message = result ? "Bravo vous avez trouvé une différence !" : "Aie.. Il semblerait qu'au moins un joueur se soit trompé.";

                    waitingDialog.dismiss();

                    // Afficher un popup avec un AlertDialog
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage(message)
                            .setPositiveButton("OK", (dialog, which) -> {
                                if(validerButton != null){
                                    validerButton.setEnabled(false);
                                }
                               if (imageView != null){
                                   imageView.setEnabled(true);
                                 }
                                if(circleImageView != null){
                                    circleImageView.setVisibility(View.INVISIBLE);
                                }
                                coordTemp = null;
                            })
                            .show();
                } else {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Erreur")
                            .setMessage("Erreur lors de l'envoi de la coordonnée.")
                            .setPositiveButton("OK", null)
                            .show();
                }
            }


            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                waitingDialog.dismiss();
                Toast.makeText(MainActivity.this, "Erreur : " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}

