package com.example.spotthedifference.ui.activities;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.spotthedifference.R;
import com.example.spotthedifference.api.ApiService;
import com.example.spotthedifference.api.RetrofitClient;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImagesActivity extends AppCompatActivity {

    private GridLayout imageGrid;
    private Button confirmButton;
    private String selectedImageUrl = null;
    private ApiService apiService;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);

        imageGrid = findViewById(R.id.image_grid);
        confirmButton = findViewById(R.id.confirmButton);

        RetrofitClient retrofitClient = new RetrofitClient();
        apiService = retrofitClient.getUnsafeRetrofit().create(ApiService.class);

        fetchAllImages();

        confirmButton.setOnClickListener(v -> {
            if (selectedImageUrl != null) {
                Toast.makeText(ImagesActivity.this, "Image confirmée avec URL: " + selectedImageUrl, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ImagesActivity.this, "Aucune image sélectionnée", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAllImages() {
        Call<List<String>> call = apiService.getAllImages();
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> imageUrls = response.body();
                    // Log to check if URLs are correct
                    for (String url : imageUrls) {
                        Log.d("ImagesActivity", "Image URL: " + url);
                    }
                    displayImages(imageUrls);
                } else {
                    Toast.makeText(ImagesActivity.this, "Failed to load images", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Toast.makeText(ImagesActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayImages(List<String> imageUrls) {
        // Vider la grille au cas où elle contient déjà des images
        imageGrid.removeAllViews();

        for (String imageUrl : imageUrls) {
            FrameLayout frameLayout = new FrameLayout(this);
            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
            layoutParams.width = GridLayout.LayoutParams.MATCH_PARENT;
            layoutParams.height = GridLayout.LayoutParams.MATCH_PARENT;
            frameLayout.setLayoutParams(layoutParams);
            frameLayout.setPadding(8, 8, 8, 8);

            // Création de l'ImageView pour afficher l'image
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(new FrameLayout.LayoutParams(100, 150));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            // Télécharger l'image avec AsyncTask
            new DownloadImageTask(imageView).execute(imageUrl);

            // Détection du clic sur l'image
            imageView.setOnClickListener(v -> {
                selectedImageUrl = imageUrl;
                highlightSelectedImage(imageView);
            });

            // Ajouter l'ImageView dans le FrameLayout
            frameLayout.addView(imageView);

            // Ajouter le FrameLayout dans le GridLayout
            imageGrid.addView(frameLayout);
        }
    }

    private void highlightSelectedImage(ImageView selectedImageView) {
        for (int i = 0; i < imageGrid.getChildCount(); i++) {
            FrameLayout frameLayout = (FrameLayout) imageGrid.getChildAt(i);
            ImageView imageView = (ImageView) frameLayout.getChildAt(0);
            if (imageView == selectedImageView) {
                imageView.setBackgroundResource(R.drawable.selected_border);
            } else {
                imageView.setBackgroundResource(0);
            }
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        public DownloadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String urlDisplay = urls[0];
            Bitmap bitmap = null;
            try {
                InputStream in = new URL(urlDisplay).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                imageView.setImageBitmap(result);
            } else {
                Toast.makeText(ImagesActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }
}