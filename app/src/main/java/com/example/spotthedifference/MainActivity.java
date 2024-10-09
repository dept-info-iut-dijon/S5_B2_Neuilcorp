package com.example.spotthedifference;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

/// <summary>
/// Classe principale de l'application.
/// Gère l'affichage principal, la détection des clics, et l'affichage d'un cercle rouge.
/// </summary>
public class MainActivity extends AppCompatActivity {

    private float clickX;
    private float clickY;
    private ImageView circleImageView;

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

        imageView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                clickX = event.getX();
                clickY = event.getY();

                circleImageView.setX(clickX - 25);
                circleImageView.setY(clickY - 25);
                circleImageView.setVisibility(View.VISIBLE);

                v.performClick();

                Toast.makeText(MainActivity.this, "Clic détecté : X=" + clickX + ", Y=" + clickY, Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }
}
