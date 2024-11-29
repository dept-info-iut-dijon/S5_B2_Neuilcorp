package com.example.spotthedifference.WebSocket;

import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.example.spotthedifference.models.GameSession;
import com.google.gson.Gson;
import com.example.spotthedifference.models.Player;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;
import com.microsoft.signalr.TransportEnum;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;


public class SignalRClient {

    private static final String SERVER_URL = "http://203.55.81.18:5195/gameSessionHub";
    private static final boolean DEBUG = true;
    private static final int MAX_RETRY_COUNT = 5;

    private HubConnection hubConnection;
    private Disposable connectionDisposable;
    private int retryCount = 0;
    private String playerId;

    private BehaviorSubject<GameSession> syncSessionStateSubject = BehaviorSubject.create();
    private BehaviorSubject<Player> playerJoinedSubject = BehaviorSubject.create();
    private BehaviorSubject<Player> playerReadyStatusChangedSubject = BehaviorSubject.create();
    private BehaviorSubject<Boolean> connectionEstablishedSubject = BehaviorSubject.create();
    private GameStartedListener gameStartedListener;
    public BehaviorSubject<Boolean> getConnectionEstablishedObservable() {
        return connectionEstablishedSubject;
    }
    private BehaviorSubject<String> readyNotAllowedSubject = BehaviorSubject.create();
    private BehaviorSubject<String> notifyMessageSubject = BehaviorSubject.create();

    /**
     * Constructeur de SignalRClient. Initialise la connexion au serveur SignalR et
     * configure les événements pour écouter les changements de statut des joueurs.
     */
    public SignalRClient(String playerid) {
        playerId = playerid;
        hubConnection = HubConnectionBuilder.create(SERVER_URL).build();

        hubConnection.on("PlayerJoined", player -> {
            playerJoinedSubject.onNext(player);
            log("SignalRClient: PlayerJoined reçu -> ID: " + player.getPlayerId() + ", Nom: " + player.getName(), null);
        }, Player.class);

        hubConnection.on("PlayerReadyStatusChanged", (playerId, isReady) -> {
            playerReadyStatusChangedSubject.onNext(new Player(playerId, isReady));
        }, String.class, Boolean.class);

        hubConnection.on("SyncSessionState", sessionState -> {
            GameSession session = new Gson().fromJson(sessionState, GameSession.class);
            syncSessionStateSubject.onNext(session);
            log("SignalRClient: SyncSessionState reçu", null);
        }, String.class);

        hubConnection.on("GameStarted", (base64Data) -> {
            byte[] imageData = Base64.decode(base64Data, Base64.DEFAULT);
            Log.d("SignalRClient", "Image reçue et décodée, taille : " + imageData.length);

            if (gameStartedListener != null) {
                gameStartedListener.onGameStarted(imageData);
            }
        }, String.class);

        hubConnection.on("ReadyNotAllowed", (message) -> {
            readyNotAllowedSubject.onNext(message);
        }, String.class);

        hubConnection.on("NotifyMessage", (message) -> {
            notifyMessageSubject.onNext(message);
        }, String.class);

        hubConnection.on("ReceiveConnectionId", (connectionId) -> {
            Log.d("SignalRClient", "ConnectionId reçu : " + connectionId);
        }, String.class);

    }

    public void setGameStartedListener(GameStartedListener listener) {
        this.gameStartedListener = listener;
    }

    /**
     * Démarre la connexion WebSocket au serveur SignalR avec gestion de la reconnexion.
     */
    public void startConnection() {
        disposeConnection();
        connectionEstablishedSubject.onNext(false);

        connectionDisposable = hubConnection.start()
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> {
                    log("Connexion établie.", null);
                    connectionEstablishedSubject.onNext(true);
                    resetReconnectionAttempts();
                    registerPlayer(playerId);
                })
                .doOnError(error -> {
                    handleError(error, "startConnection");
                    connectionEstablishedSubject.onNext(false);
                })
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

    public void registerPlayer(String playerId) {
        if (isConnected()) {
            hubConnection.send("RegisterPlayer", playerId);
            log("SignalRClient: Enregistrement du joueur avec playerId = " + playerId, null);
        } else {
            log("Connexion inactive, impossible d'enregistrer le joueur.", null);
            attemptReconnection();
        }
    }

    /**
     * Rejoint un groupe de session spécifique sur le serveur si la connexion est active.
     *
     * @param sessionId L'identifiant de la session à rejoindre.
     */
    public void joinSessionGroup(String sessionId) {
        log("SignalR: Appel JoinSessionGroup avec sessionId = " + sessionId, null);

        connectionEstablishedSubject
                .filter(isConnected -> isConnected) // Attend que la connexion soit établie
                .take(1) // Ne procède qu'une fois
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(isConnected -> {
                    try {
                        hubConnection.send("JoinSessionGroup", sessionId);
                        log("SignalR: Rejoint le groupe avec succès pour sessionId = " + sessionId, null);
                    } catch (Exception error) {
                        log("SignalR: Erreur lors du JoinSessionGroup : " + error.getMessage(), error);
                    }
                }, throwable -> log("SignalR: Erreur dans l'attente de la connexion : " + throwable.toString(), throwable));
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
        } else {
            log("Connexion inactive, impossible d'envoyer le statut de préparation.", null);
            attemptReconnection();
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

    /**
     * Retourne un observable pour les événements de joueurs rejoignant la session.
     *
     * @return Un BehaviorSubject émettant les noms des joueurs qui rejoignent.
     */
    public BehaviorSubject<Player> getPlayerJoinedObservable() {
        return playerJoinedSubject;
    }

    /**
     * Retourne un observable pour les changements de statut de préparation des joueurs.
     *
     * @return Un BehaviorSubject émettant les statuts de préparation des joueurs.
     */
    public BehaviorSubject<Player> getPlayerReadyStatusChangedObservable() {
        return playerReadyStatusChangedSubject;
    }

    /**
     * Envoie une requête au serveur pour synchroniser l'état complet de la session.
     *
     * @param sessionId L'identifiant de la session à synchroniser.
     */
    public void requestSync(String sessionId) {
        if (isConnected()) {
            hubConnection.send("RequestSync", sessionId);
        } else {
            log("Connexion inactive, impossible de demander une synchronisation.", null);
            attemptReconnection();
        }
    }

    /**
     * Retourne un observable pour les événements de synchronisation de session.
     *
     * @return Un BehaviorSubject émettant l'état complet de la session.
     */
    public BehaviorSubject<GameSession> getSyncSessionStateObservable() {
        return syncSessionStateSubject;
    }

    public BehaviorSubject<String> getReadyNotAllowedObservable() {
        return readyNotAllowedSubject;
    }

    public BehaviorSubject<String> getNotifyMessageObservable() {
        return notifyMessageSubject;
    }

    /**
     * Libère les ressources de la connexion en arrêtant tout abonnement en cours.
     */
    private void disposeConnection() {
        if (connectionDisposable != null && !connectionDisposable.isDisposed()) {
            connectionDisposable.dispose();
        }
    }
}