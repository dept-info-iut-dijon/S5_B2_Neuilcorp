package com.example.spotthedifference;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

/// <summary>
/// Classe principale de l'application.
/// Gère l'affichage principal, la détection des clics, la validation des différences, et l'affichage d'un cercle rouge.
/// </summary>
public class MainActivity extends AppCompatActivity {

    private float clickX;
    private float clickY;
    private ImageView circleImageView;
    private List<Coordonnees> listeCoordonnees;
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

        listeCoordonnees = new ArrayList<>();
        validerButton.setEnabled(false);

        circleImageView = new ImageView(this);
        circleImageView.setImageResource(R.drawable.cercle_rouge);
        circleImageView.setVisibility(View.INVISIBLE);
        layout.addView(circleImageView, new FrameLayout.LayoutParams(50, 50));

        imageView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                clickX = event.getX();
                clickY = event.getY();
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
                listeCoordonnees.add(coordTemp);

                Toast.makeText(MainActivity.this, "Coordonnées validées et ajoutées à la liste", Toast.LENGTH_SHORT).show();

                validerButton.setEnabled(false);
                imageView.setEnabled(false);
                circleImageView.setVisibility(View.INVISIBLE);
            }
        });
    }
}
