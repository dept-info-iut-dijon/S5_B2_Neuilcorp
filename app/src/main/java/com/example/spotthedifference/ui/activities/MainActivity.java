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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.spotthedifference.R;
import com.example.spotthedifference.WebSocket.GameEndedListener;
import com.example.spotthedifference.WebSocket.SignalRClient;
import com.example.spotthedifference.api.ApiService;
import com.example.spotthedifference.api.IRetrofitClient;
import com.example.spotthedifference.api.RetrofitClient;
import com.example.spotthedifference.models.Coordonnees;


import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity implements IMainActivity , GameEndedListener {

    private static final String TAG = "MainActivity";
    private SignalRClient signalRClient;
    private ImageView imageView;
    private Button validerButton;
    private ImageView circleImageView;
    private Coordonnees coordTemp;
    private ApiService apiService;
    private AlertDialog waitingDialog;
    private String sessionId;
    private String playerId;
    private Button exitButton;
    private int timerDuration;
    private TextView timerTextView;
    private Handler timerHandler = new Handler();
    private CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "MainActivity démarrée !");
        setContentView(R.layout.activity_main);

        // Étapes d'initialisation
        initializeIntentData();
        initializeUIComponents();
        initializeRetrofitClient();
        initializeSignalRClient();
        loadImageFromPath();
        setupListeners();
        setupDisposables();
        startTimerCountdown(timerDuration);

        Log.d(TAG, "MainActivity initialisée avec succès.");
    }

    /**
     * Récupère les données transmises via l'intent.
     */
    private void initializeIntentData() {
        Intent intent = getIntent();
        sessionId = intent.getStringExtra("sessionId");
        playerId = intent.getStringExtra("playerId");
        timerDuration = intent.getIntExtra("timerDuration", 0); // Récupération du TimerDuration

        String imagePath = intent.getStringExtra("imagePath");
        String imagePairIdString = intent.getStringExtra("imagePairId");

        if (sessionId == null || playerId == null || imagePath == null) {
            Toast.makeText(this, "Données manquantes, retour à l'accueil.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Log.d(TAG, "Session ID : " + sessionId + ", Player ID : " + playerId + ", Image Pair ID : " + imagePairIdString);
        Log.d(TAG, "Timer Duration : " + timerDuration + " secondes.");
    }

    /**
     * Initialise les composants de l'interface utilisateur.
     */
    private void initializeUIComponents() {
        imageView = findViewById(R.id.imageView);
        validerButton = findViewById(R.id.validateButton);
        validerButton.setEnabled(false);
        timerTextView = findViewById(R.id.timerTextView);
        if (timerDuration == 0) {timerTextView.setVisibility(View.GONE);} //on cache le compteur si la durée du timer est a zero
        circleImageView = new ImageView(this);
        circleImageView.setImageResource(R.drawable.cercle_rouge);
        circleImageView.setVisibility(View.INVISIBLE);
      
        FrameLayout layout = findViewById(R.id.frameLayout);
        layout.addView(circleImageView, new FrameLayout.LayoutParams(75, 75));

        exitButton = findViewById(R.id.exitButton);
    }

    /**
     * Initialise Retrofit pour les appels API.
     */
    private void initializeRetrofitClient() {
        IRetrofitClient client = new RetrofitClient();
        Retrofit retrofit = client.getUnsafeRetrofit();
        apiService = retrofit.create(ApiService.class);
    }

    /**
     * Configure SignalR pour la communication en temps réel.
     */
    private void initializeSignalRClient() {
        signalRClient = new SignalRClient(playerId);
        signalRClient.startConnection();
        signalRClient.joinSessionGroup(sessionId);
        signalRClient.requestSync(sessionId);
        signalRClient.setGameEndedListener(this);

        signalRClient.getHubConnection().on("ResultNotification", (result) -> {
            runOnUiThread(() -> {
                Log.d(TAG, "Notification SignalR reçue : " + result);
                hideWaitingDialog();
                showResultDialog((Boolean) result);
            });
        }, Boolean.class);

        signalRClient.getHubConnection().on("TimerExpired", (result) -> {
            runOnUiThread(() -> {
                Log.d(TAG, "TimerExpired SignalR reçue : " + result);
                hideWaitingDialog();
                showExpiredTimerDialog((Integer) result);
            });
        }, Integer.class);

        signalRClient.getGameStatisticsObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(statistics -> {
                    int attempts = statistics[0];
                    int missedAttempts = statistics[1];
                    int timersExpired = statistics[2];

                    Log.d(TAG, "Statistiques mises à jour : Tentatives = " + attempts +
                            ", Ratés = " + missedAttempts + ", Timers expirés = " + timersExpired);

                    runOnUiThread(() -> {
                        Toast.makeText(this, "Statistiques : Tentatives = " + attempts +
                                ", Ratés = " + missedAttempts +
                                ", Timers expirés = " + timersExpired, Toast.LENGTH_SHORT).show();
                    });
                }, throwable -> Log.e(TAG, "Erreur GameStatisticsUpdated observable", throwable));

        Log.d(TAG, "SignalRClient configuré pour le joueur : " + playerId);
    }

    /**
     * Charge l'image depuis le chemin fourni et l'affiche dans l'interface utilisateur.
     */
    private void loadImageFromPath() {
        String imagePath = getIntent().getStringExtra("imagePath");
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        if (bitmap != null) {
            Log.d(TAG, "Image chargée avec succès depuis le chemin : " + imagePath);
            imageView.setImageBitmap(bitmap);
        } else {
            Log.e(TAG, "Erreur : Impossible de charger l'image depuis le chemin fourni.");
            Toast.makeText(this, "Impossible de charger l'image.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * Configure les écouteurs pour les interactions utilisateur.
     */
    private void setupListeners() {
        imageView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                coordTemp = new Coordonnees((int) event.getX(), (int) event.getY());
                circleImageView.setX(coordTemp.getX() - 37);
                circleImageView.setY(coordTemp.getY() - 37);
                circleImageView.setVisibility(View.VISIBLE);
                validerButton.setEnabled(true);
                return true;
            }
            return false;
        });

        validerButton.setOnClickListener(v -> {
            if (coordTemp != null) {
                Log.d(TAG, "Coordonnées valides : " + coordTemp.getX() + ", " + coordTemp.getY());
                showWaitingDialog();
                sendCoordinatesToServer(coordTemp, sessionId, playerId, getIntent().getStringExtra("imagePairId"));
                validerButton.setEnabled(false);
                imageView.setEnabled(false);
                circleImageView.setVisibility(View.INVISIBLE);
            }
        });

        exitButton.setOnClickListener(v -> deleteSessionAndExit(sessionId, playerId));
    }


    /**
     * Configure les observables SignalR pour gérer les événements.
     */
    private void setupDisposables() {
        disposables.add(signalRClient.getSessionDeletedObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(closedSessionId -> {
                    if (closedSessionId.equals(sessionId)) {
                        Toast.makeText(this, "Un joueur a quitté la session, la partie est terminée.", Toast.LENGTH_SHORT).show();
                        redirectToHome();
                    }
                }, throwable -> Log.e(TAG, "Erreur SessionClosed observable", throwable)));
    }

    /**
     * supprime la session peut importe le joueur qui quitte
     */
    private void deleteSessionAndExit(String sessionId, String playerId) {
        apiService.removePlayerFromSession(sessionId, playerId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    signalRClient.notifySessionDeleted(sessionId);
                    redirectToHome();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Échec de la suppression de session", t);
            }
        });
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
     * Méthode appelée lorsque l'activité est détruite.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (signalRClient != null) {
            signalRClient.notifySessionDeleted(sessionId);
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
        String message = isSuccess ? "Bravo vous avez trouvé une différence !" : "Aïe... Au moins un des joueur semble s'être trompé.";
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> resetUI())
                .show();
    }

    /**
     * Méthode pour afficher un message indiquant que le timer à expirer
     * @param NombreExpiration le nombre d'expiration du timer
     */
    @Override
    public void showExpiredTimerDialog(Integer NombreExpiration) {
        Log.d("MainActivity", "showExpiredTimerDialog called with NombreExpiration: " + NombreExpiration);

        String message = "Le Timer a expiré avant que tout les joueurs ne soumettent de différence, c'est la "+ NombreExpiration + "eme fois";
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) ->{
                    resetUI();
                    resetTimerCountdown(timerDuration);
                })
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

    @Override
    public void onGameEnded(int Attempts, int MissedAttempts) {
        Log.d("MainActivity", "GameEnded reçu");
        runOnUiThread(() -> {
            // Affiche un message Toast pour informer l'utilisateur
            showGameEndedPopup(Attempts,MissedAttempts);

            // Notifie SignalR de la suppression de la session
            signalRClient.notifySessionDeleted(sessionId);

            // Crée un intent pour passer à HomeActivity
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra("ATTEMPTS", Attempts); // Ajoute le nombre de tentatives
            intent.putExtra("MISSED_ATTEMPTS", MissedAttempts); // Ajoute le nombre de tentatives ratées
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

            // Démarre l'activité et termine la current activity
            startActivity(intent);
            finish();
        });
    }

    private void resetTimerCountdown(int duration) {
        timerHandler.removeCallbacksAndMessages(null); // Arrête toutes les tâches en cours
        startTimerCountdown(duration); // Redémarre le timer avec la nouvelle durée
        Log.d(TAG, "TimerCountdown réinitialisé avec une durée de " + duration + " secondes.");
    }

    private void startTimerCountdown(int duration) {
        timerHandler.postDelayed(new Runnable() {
            int timeRemaining = duration;

            @Override
            public void run() {
                if (timeRemaining >= 0) {
                    timerTextView.setText("Temps restant : " + timeRemaining + "s");
                    timeRemaining--;
                    timerHandler.postDelayed(this, 1000);
                } else {
                    timerTextView.setText("Temps écoulé !");
                }
            }
        },0);
    }
    private void showGameEndedPopup(int attempts, int missedAttempts) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Partie terminée");
        builder.setMessage("Tentatives réussies : " + attempts + "\nTentatives ratées : " + missedAttempts);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
}