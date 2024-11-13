package com.example.spotthedifference.WebSocket;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class SignalRClient {

    private static final String SERVER_URL = "http://203.55.81.18:5195/gameSessionHub";
    private HubConnection hubConnection;
    private Disposable connectionDisposable;
    private static final boolean DEBUG = true;
    private int retryCount = 0;

    private PublishSubject<String> playerJoinedSubject = PublishSubject.create();
    private PublishSubject<Boolean> playerReadyStatusChangedSubject = PublishSubject.create();

    public SignalRClient() {
        hubConnection = HubConnectionBuilder.create(SERVER_URL).build();

        hubConnection.on("PlayerJoined", (playerName) -> {
            playerJoinedSubject.onNext(playerName);
        }, String.class);

        hubConnection.on("PlayerReadyStatusChanged", (playerId, isReady) -> {
            playerReadyStatusChangedSubject.onNext(isReady);
        }, String.class, Boolean.class);
    }

    public void startConnection() {
        disposeConnection(); // Nettoie les connexions précédentes
        connectionDisposable = hubConnection.start()
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> {
                    log("Connexion établie.", null);
                    resetReconnectionAttempts();
                })
                .doOnError(error -> handleError(error, "startConnection"))
                .subscribe();
    }

    private void attemptReconnection() {
        if (!isConnected()) {
            long delay = Math.min((long) Math.pow(2, retryCount), 60);
            log("Tentative de reconnexion dans " + delay + " secondes...", null);

            new Handler(Looper.getMainLooper()).postDelayed(this::startConnection, delay * 1000);
            retryCount++;
        }
    }

    private void resetReconnectionAttempts() {
        retryCount = 0;
    }

    public boolean isConnected() {
        return hubConnection != null && hubConnection.getConnectionState() == HubConnectionState.CONNECTED;
    }

    public void stopConnection() {
        disposeConnection();
        hubConnection.stop()
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> log("Connexion fermée.", null))
                .blockingAwait();
    }

    public void joinSessionGroup(String sessionId) {
        if (isConnected()) {
            hubConnection.send("JoinSessionGroup", sessionId);
        } else {
            log("Connexion inactive, impossible de rejoindre le groupe.", null);
            attemptReconnection();
        }
    }

    public void notifyPlayerJoined(String sessionId, String playerName) {
        if (isConnected()) {
            hubConnection.send("PlayerJoined", sessionId, playerName);
        } else {
            log("Connexion inactive, impossible d'envoyer la notification.", null);
            attemptReconnection();
        }
    }

    public void sendReadyStatusUpdate(String sessionId, String playerId, boolean isReady) {
        if (isConnected()) {
            hubConnection.send("SetPlayerReadyStatus", sessionId, playerId, isReady);
        } else {
            log("Connexion inactive, impossible d'envoyer le statut de préparation.", null);
            attemptReconnection();
        }
    }

    private void handleError(Throwable error, String context) {
        log("Erreur dans " + context + ": " + error.getMessage(), error);
        attemptReconnection();
    }

    private void log(String message, Throwable t) {
        if (DEBUG) {
            if (t != null) {
                Log.e("SignalRClient", message, t);
            } else {
                Log.d("SignalRClient", message);
            }
        }
    }

    public PublishSubject<String> getPlayerJoinedObservable() {
        return playerJoinedSubject;
    }

    public PublishSubject<Boolean> getPlayerReadyStatusChangedObservable() {
        return playerReadyStatusChangedSubject;
    }

    private void disposeConnection() {
        if (connectionDisposable != null && !connectionDisposable.isDisposed()) {
            connectionDisposable.dispose();
        }
    }

}