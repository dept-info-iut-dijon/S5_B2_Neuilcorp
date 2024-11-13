package com.example.spotthedifference;

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

        loadImage(1);

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

    @Override
    public void loadImage(int imageId) {
        Call<ResponseBody> call = apiService.getImage(imageId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        byte[] imageBytes = response.body().bytes();
                        Bitmap bitmap = ImageConverter.Companion.convertBytesToBitmap(imageBytes);
                        displayImage(imageView, bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Erreur lors du chargement de l'image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void displayImage(ImageView imageView, Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }

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
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Erreur : " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void showWaitingDialog() {
        AlertDialog waitingDialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Joueur en attente")
                .setMessage("En attente de la sélection des autres joueurs...")
                .setCancelable(false)
                .create();
        waitingDialog.show();
    }

    @Override
    public void hideWaitingDialog() {
        // Code pour cacher la boîte de dialogue
    }

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
