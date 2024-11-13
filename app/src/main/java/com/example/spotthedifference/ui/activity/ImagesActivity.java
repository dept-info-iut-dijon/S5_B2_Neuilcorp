package com.example.spotthedifference.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.spotthedifference.R;

public class ImagesActivity extends AppCompatActivity {

    private GridLayout imageGrid;
    private Button confirmButton;
    private int selectedImageId = -1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);

        imageGrid = findViewById(R.id.image_grid);
        confirmButton = findViewById(R.id.confirmButton);

        final int[] imageIds = {
                R.drawable.test, R.drawable.mehdi02, R.drawable.mehdi01, R.drawable.test,
                R.drawable.iut_dijon_auxerre_cover, R.drawable.batiment_gmp_dijon,
                R.drawable.iut_dijon_auxerre_cover, R.drawable.batiment_gmp_dijon,
                R.drawable.iut_dijon_auxerre_cover, R.drawable.batiment_gmp_dijon
        };

        // Configure each image view inside the grid
        for (int i = 0; i < imageGrid.getChildCount(); i++) {
            View child = imageGrid.getChildAt(i);

            if (child instanceof FrameLayout && i < imageIds.length) {
                FrameLayout frameLayout = (FrameLayout) child;
                ImageView imageView = (ImageView) frameLayout.getChildAt(0); // Get the first child of FrameLayout, which should be ImageView

                imageView.setImageResource(imageIds[i]);
                imageView.setVisibility(View.VISIBLE);

                int imageId = imageIds[i];
                imageView.setOnClickListener(v -> {
                    selectedImageId = imageId;
                    highlightSelectedImage(frameLayout); // Pass the FrameLayout for highlighting
                });
            } else {
                child.setVisibility(View.GONE); // Hide unused views
            }
        }

        confirmButton.setOnClickListener(v -> {
            if (selectedImageId != -1) {
                Toast.makeText(ImagesActivity.this, "Image confirmée avec ID: " + selectedImageId, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ImagesActivity.this, "Aucune image sélectionnée", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void highlightSelectedImage(FrameLayout selectedFrameLayout) {
        // Loop through all the frames in the grid and reset their backgrounds
        for (int i = 0; i < imageGrid.getChildCount(); i++) {
            View child = imageGrid.getChildAt(i);
            if (child instanceof FrameLayout) {
                FrameLayout frameLayout = (FrameLayout) child;
                if (frameLayout == selectedFrameLayout) {
                    // Apply a background for the selected frame
                    frameLayout.setBackgroundResource(R.drawable.selected_border); // Border drawable in resources
                } else {
                    // Reset background for non-selected frames
                    frameLayout.setBackgroundResource(0);
                }
            }
        }
    }
}
