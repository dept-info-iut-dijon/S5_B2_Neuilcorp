package com.example.spotthedifference.ui.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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
import com.example.spotthedifference.api.ApiService;
import com.example.spotthedifference.api.IRetrofitClient;
import com.example.spotthedifference.api.RetrofitClient;
import com.example.spotthedifference.models.Coordonnees;


import java.io.IOException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Activité principale du jeu permettant aux joueurs de trouver les différences
 * entre deux images. Gère les interactions utilisateur et la communication
 * avec le serveur en temps réel.
 */
public class MainActivity extends AppCompatActivity implements IMainActivity {

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

        Log.d(TAG, "MainActivity initialisée avec succès.");
    }

    /**
     * Récupère les données transmises via l'intent.
     */
    private void initializeIntentData() {
        Intent intent = getIntent();
        sessionId = intent.getStringExtra("sessionId");
        playerId = intent.getStringExtra("playerId");

        String imagePath = intent.getStringExtra("imagePath");
        String imagePairIdString = intent.getStringExtra("imagePairId");

        if (sessionId == null || playerId == null || imagePath == null) {
            Toast.makeText(this, "Données manquantes, retour à l'accueil.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Log.d(TAG, "Session ID : " + sessionId + ", Player ID : " + playerId + ", Image Pair ID : " + imagePairIdString);
    }

    /**
     * Initialise les composants de l'interface utilisateur.
     */
    private void initializeUIComponents() {
        imageView = findViewById(R.id.imageView);
        validerButton = findViewById(R.id.validateButton);
        validerButton.setEnabled(false);

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

        signalRClient.getHubConnection().on("ResultNotification", (result) -> {
            runOnUiThread(() -> {
                Log.d(TAG, "Notification SignalR reçue : " + result);
                hideWaitingDialog();
                showResultDialog((Boolean) result);
            });
        }, Boolean.class);

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

        disposables.add(signalRClient.getPlayerRemovedObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(removedPlayerId -> {
                    if (!isHost() && !removedPlayerId.equals(playerId)) {
                        redirectToWaitingRoom();
                    }
                }, throwable -> Log.e(TAG, "Erreur PlayerRemoved observable", throwable)));
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
     * Vérifie si le joueur actuel est l'hôte de la session.
     * TODO: Implémenter la vérification réelle du statut d'hôte
     *
     * @return true si le joueur est l'hôte, false sinon
     */
    private boolean isHost() {
        // TODO: Implémenter la logique de vérification d'hôte
        return false;
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
     * Réinitialise l'interface utilisateur après un résultat.
     */
    private void resetUI() {
        validerButton.setEnabled(false);
        circleImageView.setVisibility(View.INVISIBLE);
        coordTemp = null;
        imageView.setEnabled(true);
    }
}