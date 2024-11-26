package com.example.spotthedifference.WebSocket;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.spotthedifference.models.Player;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

import java.util.List;

public class SignalRClient {

    private static final String SERVER_URL = "http://203.55.81.18:5195/gameSessionHub";
    private static final boolean DEBUG = true;
    private static final int MAX_RETRY_COUNT = 5;

    private HubConnection hubConnection;
    private Disposable connectionDisposable;
    private int retryCount = 0;

    private BehaviorSubject<String> playerJoinedSubject = BehaviorSubject.create();
    private BehaviorSubject<Boolean> playerReadyStatusChangedSubject = BehaviorSubject.create();
    private BehaviorSubject<List<Player>> playerListSubject = BehaviorSubject.create();
    private BehaviorSubject<String> alertSubject = BehaviorSubject.create(); // Pour les alertes
    private BehaviorSubject<byte[]> imageSubject = BehaviorSubject.create(); // Pour les images

    /**
     * Constructeur de SignalRClient. Initialise la connexion au serveur SignalR et
     * configure les événements pour écouter les changements de statut des joueurs.
     */
    public SignalRClient() {
        hubConnection = HubConnectionBuilder.create(SERVER_URL).build();

        // Gestion de l'événement "PlayerJoined"
        hubConnection.on("PlayerJoined", playerName -> {
            log("Événement SignalR reçu : PlayerJoined avec le nom du joueur : " + playerName, null);
            playerJoinedSubject.onNext(playerName);
        }, String.class);

        // Gestion de l'événement "PlayerReadyStatusChanged"
        hubConnection.on("PlayerReadyStatusChanged", (playerId, isReady) -> {
            playerReadyStatusChangedSubject.onNext(isReady);
        }, String.class, Boolean.class);

        // Gestion de l'événement "PlayerListUpdated"
        hubConnection.on("PlayerListUpdated", players -> {
            log("Événement SignalR reçu : mise à jour de la liste des joueurs", null);
            playerListSubject.onNext(players); // Diffuse la liste complète des joueurs
        }, List.class);

        // Gestion de l'événement "Alert"
        hubConnection.on("Alert", message -> {
            log("Événement SignalR reçu : Alert - " + message, null);
            alertSubject.onNext(message); // Diffuse le message d'alerte
        }, String.class);

        // Gestion de l'événement "ReceiveImage"
        hubConnection.on("ReceiveImage", imageBytes -> {
            log("Événement SignalR reçu : ReceiveImage", null);
            imageSubject.onNext(imageBytes); // Diffuse l'image reçue
        }, byte[].class);
    }

    /**
     * Démarre la connexion WebSocket au serveur SignalR avec gestion de la reconnexion.
     */
    public void startConnection() {
        disposeConnection();
        connectionDisposable = hubConnection.start()
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> {
                    log("Connexion établie.", null);
                    resetReconnectionAttempts();
                })
                .doOnError(error -> handleError(error, "startConnection"))
                .subscribe();
    }

    /**
     * Tente une reconnexion automatique en cas de déconnexion, avec un délai croissant.
     */
    private void attemptReconnection() {
        if (!isConnected() && retryCount < MAX_RETRY_COUNT) {
            long delay = Math.min((long) Math.pow(2, retryCount), 60); // délai max de 60sec
            log("Tentative de reconnexion dans " + delay + " secondes...", null);

            new Handler(Looper.getMainLooper()).postDelayed(this::startConnection, delay * 1000);
            retryCount++;
        } else if (retryCount >= MAX_RETRY_COUNT) {
            log("Échec de la reconnexion après " + MAX_RETRY_COUNT + " tentatives.", null);
        }
    }

    /**
     * Réinitialise le compteur de tentatives de reconnexion après une connexion réussie.
     */
    private void resetReconnectionAttempts() {
        retryCount = 0;
    }

    /**
     * Vérifie si la connexion SignalR est active.
     *
     * @return true si la connexion est active, false sinon.
     */
    public boolean isConnected() {
        return hubConnection != null && hubConnection.getConnectionState() == HubConnectionState.CONNECTED;
    }

    /**
     * Arrête la connexion WebSocket au serveur SignalR.
     */
    public void stopConnection() {
        disposeConnection();
        hubConnection.stop()
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> log("Connexion fermée.", null))
                .blockingAwait();
    }

    /**
     * Rejoint un groupe de session spécifique sur le serveur si la connexion est active.
     *
     * @param sessionId L'identifiant de la session à rejoindre.
     */
    public void joinSessionGroup(String sessionId) {
        if (isConnected()) {
            hubConnection.send("JoinSessionGroup", sessionId);
            log("Requête envoyée au serveur : rejoindre le groupe avec sessionId : " + sessionId, null);
        } else {
            log("Connexion inactive, impossible de rejoindre le groupe.", null);
            attemptReconnection();
        }
    }

    /**
     * Notifie le serveur que le joueur a rejoint la session, si la connexion est active.
     *
     * @param sessionId  L'identifiant de la session.
     * @param playerName Le nom du joueur rejoignant la session.
     */
    public void notifyPlayerJoined(String sessionId, String playerName) {
        if (isConnected()) {
            hubConnection.send("PlayerJoined", sessionId, playerName);
        } else {
            log("Connexion inactive, impossible d'envoyer la notification.", null);
            attemptReconnection();
        }
    }

    /**
     * Envoie la mise à jour du statut de préparation d'un joueur au serveur, si la connexion est active.
     *
     * @param sessionId L'identifiant de la session de jeu.
     * @param playerId  L'identifiant du joueur.
     * @param isReady   Statut de préparation du joueur.
     */
    public void sendReadyStatusUpdate(String sessionId, String playerId, boolean isReady) {
        if (isConnected()) {
            hubConnection.send("SetPlayerReadyStatus", sessionId, playerId, isReady);
            log("Statut de préparation de " + playerId + " mis à jour.", null);
        } else {
            log("Connexion inactive, impossible d'envoyer le statut de préparation.", null);
            attemptReconnection();
        }
    }

    /**
     * Retourne un observable pour les événements de joueurs rejoignant la session.
     *
     * @return Un BehaviorSubject émettant les noms des joueurs qui rejoignent.
     */
    public BehaviorSubject<String> getPlayerJoinedObservable() {
        return playerJoinedSubject;
    }

    /**
     * Retourne un observable pour les changements de statut de préparation des joueurs.
     *
     * @return Un BehaviorSubject émettant les statuts de préparation des joueurs.
     */
    public BehaviorSubject<Boolean> getPlayerReadyStatusChangedObservable() {
        return playerReadyStatusChangedSubject;
    }

    /**
     * Retourne un observable pour la liste des joueurs mise à jour.
     *
     * @return Un BehaviorSubject émettant la liste complète des joueurs.
     */
    public BehaviorSubject<List<Player>> getPlayerListObservable() {
        return playerListSubject;
    }

    /**
     * Retourne un observable pour les messages d'alerte.
     *
     * @return Un BehaviorSubject émettant les messages d'alerte.
     */
    public BehaviorSubject<String> getAlertObservable() {
        return alertSubject;
    }

    /**
     * Retourne un observable pour les images reçues.
     *
     * @return Un BehaviorSubject émettant les images reçues sous forme de tableau d'octets.
     */
    public BehaviorSubject<byte[]> getImageObservable() {
        return imageSubject;
    }

    /**
     * Libère les ressources de la connexion en arrêtant tout abonnement en cours.
     */
    private void disposeConnection() {
        if (connectionDisposable != null && !connectionDisposable.isDisposed()) {
            connectionDisposable.dispose();
        }
    }

    /**
     * Gère les erreurs survenant lors des tentatives de connexion et lance une reconnexion.
     *
     * @param error   L'erreur rencontrée.
     * @param context Le contexte dans lequel l'erreur s'est produite (nom de la méthode).
     */
    private void handleError(Throwable error, String context) {
        log("Erreur dans " + context + ": " + error.getMessage(), error);
        attemptReconnection();
    }

    /**
     * Méthode de log centralisée pour gérer les messages de débogage et d'erreur.
     *
     * @param message Le message à loguer.
     * @param t       Une exception associée, si applicable.
     */
    private void log(String message, Throwable t) {
        if (DEBUG) {
            if (t != null) {
                Log.e("SignalRClient", message, t);
            } else {
                Log.d("SignalRClient", message);
            }
        }
    }
}