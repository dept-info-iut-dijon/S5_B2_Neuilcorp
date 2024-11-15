package com.example.spotthedifference.ui.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.spotthedifference.R;
import com.example.spotthedifference.api.ApiService;
import com.example.spotthedifference.api.RetrofitClient;
import com.example.spotthedifference.models.ImageWithPair;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Base64;
import android.graphics.BitmapFactory;

import java.util.List;
import android.util.Log;
import java.util.HashSet;
import java.util.Set;

/**
 * Activité permettant aux utilisateurs de sélectionner une image parmi une grille d'images
 * récupérées depuis le serveur. Après sélection, l'image choisie est envoyée au serveur pour
 * confirmation et l'utilisateur est redirigé vers la salle d'attente.
 */
public class ImagesActivity extends AppCompatActivity {

    private GridLayout imageGrid;
    private Button confirmButton;
    private ApiService apiService;
    private int selectedImagePairId = -1;
    private String sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);

        imageGrid = findViewById(R.id.image_grid);
        confirmButton = findViewById(R.id.confirmButton);
        sessionId = getIntent().getStringExtra("sessionId");

        RetrofitClient retrofitClient = new RetrofitClient();
        apiService = retrofitClient.getUnsafeRetrofit().create(ApiService.class);

        fetchImagesWithPairs();

        confirmButton.setOnClickListener(v -> {
            if (selectedImagePairId != -1) {
                Call<Void> call = apiService.selectImagePair(sessionId, selectedImagePairId);
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(ImagesActivity.this, "Image confirmée", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(ImagesActivity.this, "Erreur de confirmation d'image", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(ImagesActivity.this, "Échec de connexion au serveur", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(ImagesActivity.this, "Aucune image sélectionnée", Toast.LENGTH_SHORT).show();
            }
        });

        Button returnButton = findViewById(R.id.backButton);
        returnButton.setOnClickListener(v -> {
            finish();
        });
    }

    /**
     * Récupère les paires d'images du serveur et les affiche dans la grille.
     */
    private void fetchImagesWithPairs() {
        Call<List<ImageWithPair>> call = apiService.getAllImagesWithPairs();
        call.enqueue(new Callback<List<ImageWithPair>>() {
            @Override
            public void onResponse(Call<List<ImageWithPair>> call, Response<List<ImageWithPair>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayImages(response.body());
                } else {
                    Toast.makeText(ImagesActivity.this, "Impossible de charger les images", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ImageWithPair>> call, Throwable t) {
                Toast.makeText(ImagesActivity.this, "Erreur : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Affiche les images reçues dans la grille et s'assure que seule une image par paire est affichée.
     *
     * @param imageList Liste des objets ImageWithPair contenant les informations des images.
     */
    private void displayImages(List<ImageWithPair> imageList) {
        imageGrid.removeAllViews();
        Set<Integer> displayedPairs = new HashSet<>();

        // Calcul de la largeur et de la hauteur des images pour l'affichage en trois colonnes
        int imageWidth = getResources().getDisplayMetrics().widthPixels / 3 - 50;
        int imageHeight = (int) (imageWidth * 1.5);

        for (ImageWithPair imageDto : imageList) {
            Log.d("ImagesActivity", "Image ID: " + imageDto.getImageId());
            Log.d("ImagesActivity", "Image Pair ID: " + imageDto.getImagePairId());
            Log.d("ImagesActivity", "Base64 Image Length: " + (imageDto.getBase64Image() != null ? imageDto.getBase64Image().length() : "null"));

            if (displayedPairs.contains(imageDto.getImagePairId())) {
                continue;
            }
            displayedPairs.add(imageDto.getImagePairId());

            Bitmap bitmap = convertBase64ToBitmap(imageDto.getBase64Image());
            if (bitmap == null) {
                Log.e("ImagesActivity", "Bitmap is null for Image ID: " + imageDto.getImageId());
                continue;
            }

            FrameLayout frameLayout = new FrameLayout(this);
            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
            layoutParams.width = imageWidth;
            layoutParams.height = imageHeight;
            layoutParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            frameLayout.setLayoutParams(layoutParams);

            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(new FrameLayout.LayoutParams(imageWidth, imageHeight));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageBitmap(bitmap);

            imageView.setOnClickListener(v -> {
                selectedImagePairId = imageDto.getImagePairId();
                highlightSelectedImage(imageView);
            });

            frameLayout.addView(imageView);
            imageGrid.addView(frameLayout);
        }
    }

    /**
     * Convertit une chaîne Base64 en objet Bitmap pour affichage.
     *
     * @param base64String Chaîne représentant l'image encodée en Base64.
     * @return Bitmap de l'image, ou null si la conversion échoue.
     */
    private Bitmap convertBase64ToBitmap(String base64String) {
        byte[] imageBytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    /**
     * Met en évidence l'image sélectionnée en ajoutant une bordure de sélection.
     *
     * @param selectedImageView L'ImageView représentant l'image sélectionnée.
     */
    private void highlightSelectedImage(ImageView selectedImageView) {
        for (int i = 0; i < imageGrid.getChildCount(); i++) {
            FrameLayout frameLayout = (FrameLayout) imageGrid.getChildAt(i);
            ImageView imageView = (ImageView) frameLayout.getChildAt(0);

            if (frameLayout.getChildCount() > 1) {
                frameLayout.removeViewAt(1);
            }

            if (imageView == selectedImageView) {
                View selectedOverlay = getLayoutInflater().inflate(R.layout.selected_border_layout, frameLayout, false);
                frameLayout.addView(selectedOverlay);
            }
        }
    }
}
