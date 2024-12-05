package com.example.spotthedifference.ui.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.spotthedifference.R;
import com.example.spotthedifference.WebSocket.SignalRClient;
import com.example.spotthedifference.api.ApiResponse;
import com.example.spotthedifference.api.ApiService;
import com.example.spotthedifference.api.IRetrofitClient;
import com.example.spotthedifference.api.RetrofitClient;
import com.example.spotthedifference.models.Coordonnees;
import com.example.spotthedifference.ui.utils.ImageDisplayer;

import java.io.IOException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity implements IMainActivity {

    private static final String TAG = "MainActivity";
    private SignalRClient signalRClient;
    private ImageView imageView;
    private Button validerButton;
    private ImageView circleImageView;
    private Coordonnees coordTemp;
    private ApiService apiService;
    private AlertDialog waitingDialog;
    private static final int TIMEOUT_DURATION = 30000;
    private Handler timeoutHandler = new Handler();
    private Runnable timeoutRunnable = () -> {
        Toast.makeText(MainActivity.this, "Temps écoulé. Veuillez réessayer.", Toast.LENGTH_LONG).show();
        resetUI();
    };
    private void startTimeout() {
        timeoutHandler.postDelayed(timeoutRunnable, TIMEOUT_DURATION);
    }
    private void stopTimeout() {
        timeoutHandler.removeCallbacks(timeoutRunnable);
    }
    private String sessionId;
    private String playerId;
    private Button exitButton;
    private CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MainActivity", "MainActivity démarrée !");
        setContentView(R.layout.activity_main);

        // Récupération des données transmises via l'intent
        Intent intent = getIntent();
        String sessionId = intent.getStringExtra("sessionId");
        String playerId = intent.getStringExtra("playerId");

        String imagePath = intent.getStringExtra("imagePath");
        String imagePairIdString = intent.getStringExtra("imagePairId");

        signalRClient = new SignalRClient(playerId);
        signalRClient.startConnection();
        signalRClient.joinSessionGroup(sessionId);
        signalRClient.requestSync(sessionId);

        Log.d("MainActivity", "SignalRClient démarré pour le joueur : " + playerId);
        signalRClient.getHubConnection().on("ResultNotification", (result) -> {
            runOnUiThread(() -> {
                Log.d("MainActivity", "Notification SignalR reçue : " + result);
                hideWaitingDialog();
                showResultDialog((Boolean) result);
            });
        }, Boolean.class);


        // Validation des données reçues
        if (sessionId == null || playerId == null || imagePath == null) {
            Toast.makeText(this, "Données manquantes, retour à l'accueil.", Toast.LENGTH_LONG).show();
            finish(); // Ferme l'activité si les données essentielles manquent
            return;
        }

        Log.d("MainActivity", "Session ID : " + sessionId + ", Player ID : " + playerId + ", Image Pair ID : " + imagePairIdString);

        // Initialisation des composants de l'interface utilisateur
        imageView = findViewById(R.id.imageView);
        FrameLayout layout = findViewById(R.id.frameLayout);
        validerButton = findViewById(R.id.validateButton);
        validerButton.setEnabled(false);

        circleImageView = new ImageView(this);
        circleImageView.setImageResource(R.drawable.cercle_rouge);
        circleImageView.setVisibility(View.INVISIBLE);
        layout.addView(circleImageView, new FrameLayout.LayoutParams(50, 50));

        // Chargement et affichage de l'image depuis le chemin fourni
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        if (bitmap != null) {
            Log.d("MainActivity", "Image chargée avec succès depuis le chemin : " + imagePath);
            imageView.setImageBitmap(bitmap);
        } else {
            Log.e("MainActivity", "Erreur : Impossible de charger l'image depuis le chemin fourni.");
            Toast.makeText(this, "Impossible de charger l'image.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialisation de l'API pour l'envoi des coordonnées
        IRetrofitClient client = new RetrofitClient();
        Retrofit retrofit = client.getUnsafeRetrofit();
        apiService = retrofit.create(ApiService.class);

        // Configuration des écouteurs
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
                Log.d("MainActivity", "Coordonnées valides : " + coordTemp.getX() + ", " + coordTemp.getY());
                showWaitingDialog();
                startTimeout();
                sendCoordinatesToServer(coordTemp, sessionId, playerId, imagePairIdString);
                validerButton.setEnabled(false);
                imageView.setEnabled(false);
                circleImageView.setVisibility(View.INVISIBLE);
            }
        });

        exitButton = findViewById(R.id.exitButton);
        exitButton.setOnClickListener(v -> deleteSessionAndExit());

        disposables.add(signalRClient.getSessionClosedObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(closedSessionId -> {
                if (closedSessionId.equals(sessionId)) {
                    Toast.makeText(this, "La session a été fermée par l'hôte.", Toast.LENGTH_SHORT).show();
                    redirectToHome();
                }
            }, throwable -> Log.e(TAG, "Erreur SessionClosed observable", throwable)));

        disposables.add(signalRClient.getPlayerLeftObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(leftPlayerId -> {
                if (!isHost() && !leftPlayerId.equals(playerId)) {
                    redirectToWaitingRoom();
                }
            }, throwable -> Log.e(TAG, "Erreur PlayerLeft observable", throwable)));
    }

    /**
     * Gère le départ d'un joueur ou la suppression d'une session si l'hôte quitte.
     */
    private void deleteSessionAndExit() {
        if (isHost()) {
            // L'hôte quitte, supprimer la session
            apiService.removePlayerFromSession(sessionId, playerId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        signalRClient.notifySessionClosed(sessionId);
                        redirectToHome();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "Échec de la suppression de session", t);
                }
            });
        } else {
            // Un joueur non-hôte quitte
            apiService.removePlayerFromSession(sessionId, playerId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        signalRClient.notifyPlayerLeft(sessionId, playerId);
                        redirectToHome();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "Échec du retrait du joueur", t);
                }
            });
        }
    }

    /**
     * Vérifie si le joueur actuel est hôte ou non.
     * @return true si le joueur est hôte, false s'il ne l'est pas.
     */
    private boolean isHost() {
        return false; // temporaire
    }

    /**
     * Redirige vers l'activité HomeActivity.
     */
    private void redirectToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Redirige vers l'activité WaitingRoomActivity.
     */
    private void redirectToWaitingRoom() {
        Intent intent = new Intent(this, WaitingRoomActivity.class);
        intent.putExtra("sessionId", sessionId);
        intent.putExtra("playerId", playerId);
        startActivity(intent);
        finish();
    }

    /**
     * Méthode appelée lorsque l'activité est détruite.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (signalRClient != null) {
            signalRClient.stopConnection(sessionId, playerId);
        }
    }

    @Override
    public void sendCoordinatesToServer(Coordonnees coordonnees, String sessionId, String playerId, String imagePairIdString) {
        Log.d("MainActivity", "Envoi des coordonnées au serveur...");
        Log.d("MainActivity", "imagePairId : " + imagePairIdString);

        Call<Void> call = apiService.sendCoordinates(coordonnees, sessionId, playerId, imagePairIdString);
        Log.d("MainActivity", "Appel de l'API : " + call.request().url());
        call.enqueue(new Callback<Void>() {

            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("MainActivity", "Coordonnées envoyées avec succès.");
                } else {
                    Log.e("MainActivity", "Erreur dans la réponse HTTP : code=" + response.code());
                    Toast.makeText(MainActivity.this, "Erreur de communication avec le serveur.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("MainActivity", "Erreur lors de l'envoi des coordonnées : " + t.getMessage());
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
        stopTimeout();
        String message = isSuccess ? "Bravo vous avez trouvé une différence !" : "Aïe... Au moins un des joueur semble s'être trompé.";
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> resetUI())
                .show();
    }

    /**
     * Réinitialise l'interface utilisateur après un résultat.
     */
    private void resetUI() {
        validerButton.setEnabled(false);
        circleImageView.setVisibility(View.INVISIBLE);
        coordTemp = null;
        imageView.setEnabled(true);
    }
}