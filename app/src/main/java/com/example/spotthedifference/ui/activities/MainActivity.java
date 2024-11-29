package com.example.spotthedifference.ui.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
import android.view.MotionEvent;

import androidx.appcompat.app.AppCompatActivity;

import com.example.spotthedifference.ui.utils.ImageConverter;
import com.example.spotthedifference.ui.utils.ImageDisplayer;
import com.example.spotthedifference.R;
import com.example.spotthedifference.api.ApiService;
import com.example.spotthedifference.api.IRetrofitClient;
import com.example.spotthedifference.api.RetrofitClient;
import com.example.spotthedifference.models.Coordonnees;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity implements IMainActivity {

    private ImageView imageView;
    private Button validerButton;
    private Coordonnees coordTemp;
    private ImageView circleImageView;
    private ApiService apiService;
    private AlertDialog waitingDialog;
    private ImageDisplayer imageDisplayer = new ImageDisplayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        FrameLayout layout = findViewById(R.id.frameLayout);
        validerButton = findViewById(R.id.validateButton);
        Button exitButton = findViewById(R.id.exitButton);

        exitButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
        });

        validerButton.setEnabled(false);
        circleImageView = new ImageView(this);
        circleImageView.setImageResource(R.drawable.cercle_rouge);
        circleImageView.setVisibility(View.INVISIBLE);
        layout.addView(circleImageView, new FrameLayout.LayoutParams(50, 50));

        IRetrofitClient client = new RetrofitClient();
        Retrofit retrofit = client.getUnsafeRetrofit();
        apiService = retrofit.create(ApiService.class);

        // Récupération et affichage de l'image reçue via l'Intent
        byte[] imageData = getIntent().getByteArrayExtra("imageData");
        if (imageData != null) {
            Bitmap bitmap = ImageConverter.instance.convertBytesToBitmap(imageData);
            imageView.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, "Aucune image reçue", Toast.LENGTH_SHORT).show();
        }

        imageView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                coordTemp = new Coordonnees((int) event.getX(), (int) event.getY());
                circleImageView.setX(coordTemp.getX() - 25);
                circleImageView.setY(coordTemp.getY() - 25);
                circleImageView.setVisibility(View.VISIBLE);
                validerButton.setEnabled(true);
                return true;
            }
            return false;
        });

        validerButton.setOnClickListener(v -> {
            if (coordTemp != null) {
                showWaitingDialog();
                sendCoordinatesToServer(coordTemp);
                validerButton.setEnabled(false);
                imageView.setEnabled(false);
                circleImageView.setVisibility(View.INVISIBLE);
            }
        });
    }

    /**
     * Méthode pour charger une image depuis le serveur en utilisant l'ID de l'image.
     * Si la requête est réussie, l'image est affichée dans l'ImageView.
     * @param imageId Identifiant de l'image à charger.
     */
    @Override
    public void loadImage(int imageId) {
        Call<ResponseBody> call = apiService.getImage(imageId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        byte[] imageBytes = response.body().bytes();
                        Bitmap bitmap = ImageConverter.instance.convertBytesToBitmap(imageBytes);
                        imageDisplayer.displayImage(imageView, bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Erreur lors de la conversion de l'image", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Erreur lors du chargement de l'image", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Erreur lors du chargement de l'image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * méthode pour envoyer les coordonnées au serveur.
     * Affiche un message selon le résultat de la requête.
     * @param coordonnees Coordonnées à envoyer.
     */
    @Override
    public void sendCoordinatesToServer(Coordonnees coordonnees) {
        Call<Boolean> call = apiService.sendCoordinates(coordonnees);
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean result = response.body();
                    showResultDialog(result);
                } else {
                    showResultDialog(false);
                }
                hideWaitingDialog();
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Erreur : " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Méthode pour afficher une boite de dialogue indiquant que l'on attend les autres joueurs.
     */
    @Override
    public void showWaitingDialog() {
        if (waitingDialog == null) {
            waitingDialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Joueur en attente")
                    .setMessage("En attente de la sélection des autres joueurs...")
                    .setCancelable(false)
                    .create();
        }
        waitingDialog.show();
    }

    /**
     * Méthode pour masquer la boite de dialogue d'attente.
     */
    @Override
    public void hideWaitingDialog() {
        if (waitingDialog != null && waitingDialog.isShowing()) {
            waitingDialog.dismiss();
        }
    }

    /**
     * Méthode pour afficher un message indiquant si le joueur a trouvé une différence ou non.
     * @param isSuccess Vrai si le résultat est un succès, sinon faux.
     */
    @Override
    public void showResultDialog(boolean isSuccess) {
        String message = isSuccess ? "Bravo vous avez trouvé une différence !" : "Aie.. Il semblerait qu'au moins un joueur se soit trompé.";
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    validerButton.setEnabled(false);
                    circleImageView.setVisibility(View.INVISIBLE);
                    coordTemp = null;
                    imageView.setEnabled(true);
                })
                .show();
    }
}