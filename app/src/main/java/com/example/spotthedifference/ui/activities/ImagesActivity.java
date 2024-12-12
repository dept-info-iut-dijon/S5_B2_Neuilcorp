package com.example.spotthedifference.ui.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.spotthedifference.R;
import com.example.spotthedifference.api.ApiService;
import com.example.spotthedifference.api.RetrofitClient;
import com.example.spotthedifference.models.ImageWithPair;
import com.example.spotthedifference.ui.utils.ScreenUtils;

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

    /**
     * Grille d'affichage des images.
     */
    private GridLayout imageGrid;

    /**
     * Bouton de confirmation de la sélection.
     */
    private Button confirmButton;

    /**
     * Barre de progression du chargement des images
     */
    private ProgressBar progressBar;
  
     /**
     * Service pour les appels API.
     */
    private ApiService apiService;

    /**
     * ID de la paire d'images sélectionnée (-1 si aucune sélection).
     */
    private int selectedImagePairId = -1;

    /**
     * ID de la session de jeu en cours.
     */
    private String sessionId;

    /**
     * Nombre de colonnes dans la grille d'images.
     */
    private static final int GRID_COLUMN_COUNT = 3;

    /**
     * Espacement entre les images en pixels.
     */
    private static final int IMAGE_PADDING = 50;

    /**
     * Ratio hauteur/largeur des images.
     */
    private static final double IMAGE_RATIO = 1.5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);

        // Initialisation des éléments de l'interface utilisateur
        initializeUI();

        // Initialisation du client API
        initializeApiClient();

        // Chargement des images depuis le serveur
        fetchImagesWithPairs();

        // Gestion des clics sur les boutons
        setupButtonListeners();
    }

    /**
     * Initialise les éléments de l'interface utilisateur.
     */
    private void initializeUI() {
        imageGrid = findViewById(R.id.image_grid);
        confirmButton = findViewById(R.id.confirmButton);
        progressBar = findViewById(R.id.progressBar);
        sessionId = getIntent().getStringExtra("sessionId");
    }

    /**
     * Initialise le client API pour communiquer avec le serveur.
     */
    private void initializeApiClient() {
        RetrofitClient retrofitClient = new RetrofitClient();
        apiService = retrofitClient.getUnsafeRetrofit().create(ApiService.class);
    }

    /**
     * Configure les gestionnaires de clics pour les boutons.
     */
    private void setupButtonListeners() {
        confirmButton.setOnClickListener(v -> handleConfirmButtonClick());

        Button returnButton = findViewById(R.id.backButton);
        returnButton.setOnClickListener(v -> finish());
    }

    /**
     * Gère le clic sur le bouton de confirmation.
     */
    private void handleConfirmButtonClick() {
        if (selectedImagePairId != -1) {
            confirmImageSelection();
        } else {
            showToast(getString(R.string.Images_Toast_AucuneImageSelectionnee));
        }
    }

    /**
     * Envoie la sélection de l'image au serveur.
     */
    private void confirmImageSelection() {
        Call<Void> call = apiService.selectImagePair(sessionId, selectedImagePairId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    showToast(getString(R.string.Images_Toast_ImageConfirmee));
                    finish();
                } else {
                    showToast(getString(R.string.Images_Toast_ErreurConfirmation));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast(getString(R.string.Images_Toast_EchecConnexion));
            }
        });
    }

    /**
     * Affiche un message Toast avec le texte donné.
     *
     * @param message Message à afficher.
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Récupère les paires d'images du serveur et les affiche dans la grille.
     */
    private void fetchImagesWithPairs() {
        progressBar.setVisibility(View.VISIBLE);
        Call<List<ImageWithPair>> call = apiService.getAllImagesWithPairs();
        call.enqueue(new Callback<List<ImageWithPair>>() {
            @Override
            public void onResponse(Call<List<ImageWithPair>> call, Response<List<ImageWithPair>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    displayImages(response.body());
                } else {
                    Toast.makeText(ImagesActivity.this, getString(R.string.Images_Toast_AucuneImageSelectionnee), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ImageWithPair>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ImagesActivity.this, getString(R.string.Images_Toast_ErreurChargementImages) + t.getMessage(), Toast.LENGTH_SHORT).show();
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
        int imageWidth = ScreenUtils.calculateImageWidth(this, GRID_COLUMN_COUNT, IMAGE_PADDING);
        int imageHeight = ScreenUtils.calculateImageHeight(imageWidth, IMAGE_RATIO);

        for (ImageWithPair imageDto : imageList) {
            Log.d("ImagesActivity", "Image ID: " + imageDto.getImageId());
            Log.d("ImagesActivity", "Image Pair ID: " + imageDto.getImagePairId());

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
