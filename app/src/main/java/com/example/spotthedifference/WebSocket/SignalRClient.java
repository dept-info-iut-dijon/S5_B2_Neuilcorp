package com.example.spotthedifference.WebSocket;

import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.example.spotthedifference.config.ServerConfig;
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

/**
 * SignalRClient est une classe responsable de gérer la connexion et la communication en temps réel
 * avec le serveur SignalR. Elle fournit des méthodes pour envoyer et recevoir des messages, gérer
 * les événements de session et synchroniser l'état entre les clients connectés.
 *
 * Les fonctionnalités incluent :
 * - Connexion et reconnexion au serveur.
 * - Gestion des événements tels que la synchronisation des sessions, les changements de statut des joueurs,
 *   les notifications de jeu commencé ou terminé, et les messages du serveur.
 * - Observables pour surveiller les mises à jour en temps réel et réagir en conséquence.
 */
public class SignalRClient {

    private static final String SERVER_URL = ServerConfig.SIGNALR_URL;
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
    private BehaviorSubject<String> SessionDeletedSubject = BehaviorSubject.create();

    private GameStartedListener gameStartedListener;
    private GameEndedListener gameEndedListener;
    private BehaviorSubject<String> readyNotAllowedSubject = BehaviorSubject.create();
    private BehaviorSubject<String> notifyMessageSubject = BehaviorSubject.create();
    private BehaviorSubject<String> PlayerRemovedSubject = BehaviorSubject.create();
    private BehaviorSubject<Integer> timerDurationSubject = BehaviorSubject.create();
    private BehaviorSubject<int[]> gameStatisticsSubject = BehaviorSubject.create();


    /**
     * Initialise le client SignalR en configurant la connexion et les événements.
     *
     * @param playerid L'identifiant unique du joueur pour cette session.
     */
    public SignalRClient(String playerid) {
        this.playerId = playerid;
        initializeHubConnection();
        setupHubEvents();
    }


    /**
     * Initialise la connexion HubConnection avec le serveur SignalR.
     */
    private void initializeHubConnection() {
        hubConnection = HubConnectionBuilder.create(SERVER_URL).build();
    }

    /**
     * Configure tous les événements SignalR pris en charge par le client.
     */
    private void setupHubEvents() {
        hubConnection.on("PlayerJoined", this::handlePlayerJoined, Player.class);
        hubConnection.on("PlayerRemoved", this::handlePlayerRemoved, Player.class);
        hubConnection.on("PlayerReadyStatusChanged", this::handlePlayerReadyStatusChanged, String.class, Boolean.class);
        hubConnection.on("SyncSessionState", this::handleSyncSessionState, String.class);
        hubConnection.on("GameStarted", this::handleGameStarted, String.class, Integer.class, Integer.class);
        hubConnection.on("GameEnded", this::handleGameEnded);
        hubConnection.on("ReadyNotAllowed", readyNotAllowedSubject::onNext, String.class);
        hubConnection.on("NotifyMessage", notifyMessageSubject::onNext, String.class);
        hubConnection.on("ReceiveConnectionId", this::handleConnectionIdReceived, String.class);
        hubConnection.on("SessionDeleted", SessionDeletedSubject::onNext, String.class);
        hubConnection.on("TimerStarted", this::handleTimerStarted, Integer.class, String.class);
        hubConnection.on("GameStatisticsUpdated", this::handleGameStatisticsUpdated, Integer.class, Integer.class, Integer.class);

    }

    /**
     * Gestion de l'événement "PlayerJoined".
     */
    private void handlePlayerJoined(Player player) {
        playerJoinedSubject.onNext(player);
        log("SignalRClient: PlayerJoined reçu -> ID: " + player.getPlayerId() + ", Nom: " + player.getName(), null);
    }

    /**
     * Gestion de l'événement "PlayerRemoved".
     */
    private void handlePlayerRemoved(Player player) {
        PlayerRemovedSubject.onNext(player.getPlayerId());
        log("SignalRClient: PlayerRemoved reçu -> ID: " + player.getPlayerId() + ", Nom: " + player.getName(), null);
    }

    /**
     * Gestion de l'événement "PlayerReadyStatusChanged".
     */
    private void handlePlayerReadyStatusChanged(String playerId, Boolean isReady) {
        playerReadyStatusChangedSubject.onNext(new Player(playerId, isReady));
    }

    /**
     * Gestion de l'événement "SyncSessionState".
     */
    private void handleSyncSessionState(String sessionState) {
        GameSession session = new Gson().fromJson(sessionState, GameSession.class);
        syncSessionStateSubject.onNext(session);
        log("SignalRClient: SyncSessionState reçu", null);
    }

    /**
     * Gestion de l'événement "GameStarted".
     */
    private void handleGameStarted(String base64Data, Integer imagePairId, Integer TimerDuration) {
        byte[] imageData = Base64.decode(base64Data, Base64.DEFAULT);
        log("SignalRClient: Image reçue et décodée, taille : " + imageData.length + " imagePairId : " + imagePairId, null);

        if (gameStartedListener != null) {
            gameStartedListener.onGameStarted(imageData, imagePairId, TimerDuration);
        } else {
            log("SignalRClient: GameStartedListener est null !", null);
        }
    }

    /**
     * Gestion de l'événement "GameStarted".
     */
    private void handleGameEnded() {
        if (gameEndedListener != null) {
            gameEndedListener.onGameEnded();
        } else {
            log("SignalRClient: GameEndedListener est null !", null);
        }
    }


    /**
     * Gestion de l'événement "ReceiveConnectionId".
     */
    private void handleConnectionIdReceived(String connectionId) {
        log("SignalRClient: ConnectionId reçu : " + connectionId, null);
    }

    /**
     * Gestion de l'événement "TimerStarted".
     * Notifie le client que le timer a démarré ou redémarré.
     *
     * @param duration La durée totale du timer en secondes.
     * @param startTime L'heure de début réelle du timer.
     */
    private void handleTimerStarted(Integer duration, String startTime) {
        timerDurationSubject.onNext(duration);
        Log.d("SignalRClient", "Timer démarré pour " + duration + " secondes à " + startTime);
    }

    /**
     * Gestion de l'événement "GameStatisticsUpdated".
     * Reçoit et met à jour les statistiques globales du jeu.
     *
     * @param attempts Nombre total de tentatives.
     * @param missedAttempts Nombre de tentatives ratées.
     * @param timersExpired Nombre de timers expirés.
     */
    private void handleGameStatisticsUpdated(Integer attempts, Integer missedAttempts, Integer timersExpired) {
        gameStatisticsSubject.onNext(new int[]{attempts, missedAttempts, timersExpired});
        Log.d("SignalRClient", "Statistiques mises à jour : Tentatives = " + attempts +
                ", Ratés = " + missedAttempts + ", Timers expirés = " + timersExpired);
    }


    /**
     * Définit un écouteur pour l'événement de début de jeu.
     *
     * Cet écouteur sera appelé lorsque le serveur SignalR émettra un événement "GameStarted",
     * fournissant les données nécessaires pour démarrer la partie (comme l'image et son identifiant).
     *
     * @param listener L'instance de {@link GameStartedListener} qui doit être notifiée
     *                 lorsque l'événement de début de jeu est déclenché.
     */
    public void setGameStartedListener(GameStartedListener listener) {
        this.gameStartedListener = listener;
        Log.d("SignalRClient", "GameStartedListener défini : " + (listener != null));
    }


    public void setGameEndedListener(GameEndedListener listener) {
        this.gameEndedListener = listener;
        Log.d("SignalRClient", "GameEndedListener défini : " + (listener != null));
    }

    /**
     * Démarre la connexion WebSocket au serveur SignalR.
     *
     * Cette méthode initialise une nouvelle connexion SignalR en libérant toute connexion précédente
     * via {@link #disposeConnection()}. Une fois la connexion établie avec succès, elle notifie
     * l'état de la connexion via le {@link BehaviorSubject} connectionEstablishedSubject, réinitialise
     * le compteur de tentatives de reconnexion, et enregistre le joueur actuel via {@link #registerPlayer(String)}.
     *
     * En cas d'erreur lors de la connexion, la méthode {@link #handleError(Throwable, String)} est appelée
     * pour gérer l'erreur et tenter une reconnexion.
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
     * Tente une reconnexion automatique au serveur SignalR.
     *
     * Cette méthode vérifie l'état de la connexion via {@link #isConnected()} et,
     * si la connexion est inactive et que le nombre maximum de tentatives de reconnexion
     * n'a pas été atteint, elle planifie une nouvelle tentative de connexion après un délai.
     * Le délai augmente de manière exponentielle avec chaque tentative, jusqu'à un maximum
     * de 60 secondes.
     *
     * Si le nombre de tentatives dépasse {@link #MAX_RETRY_COUNT}, un message est logué
     * indiquant l'échec des tentatives de reconnexion.
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
     * Arrête la connexion SignalR pour un joueur spécifique.
     *
     * Cette méthode notifie d'abord le serveur SignalR du départ du joueur via
     * {@link #notifyPlayerRemoved(String, String)}. Ensuite, elle libère les ressources de connexion
     * via {@link #disposeConnection()} et arrête la connexion SignalR de manière bloquante.
     *
     * @param sessionId L'identifiant de la session dont le joueur se retire.
     * @param playerId  L'identifiant du joueur qui quitte la session.
     */
    public void stopConnection(String sessionId, String playerId) {
        notifyPlayerRemoved(sessionId, playerId);
        disposeConnection();
        hubConnection.stop()
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> log("Connexion fermée.", null))
                .blockingAwait();
    }


    /**
     * Méthode permettant d'enregistrer un joueur auprès du serveur via SignalR.
     * @param playerId l'ID du joueur à enregistrer
     */
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
     * Notifie le serveur SignalR du départ d'un joueur de la session.
     *
     * Cette méthode envoie un message "PlayerLeft" au serveur avec les informations de session
     * et d'identifiant du joueur, si la connexion est active. Si la connexion est inactive,
     * une tentative de reconnexion est déclenchée via {@link #attemptReconnection()}.
     *
     * @param sessionId L'identifiant de la session dont le joueur se retire.
     * @param playerId  L'identifiant du joueur qui quitte la session.
     */
    public void notifyPlayerRemoved(String sessionId, String playerId) {
        if (isConnected()) {
            hubConnection.send("PlayerLeft", sessionId, playerId);
            log("SignalRClient: Notifié le départ du joueur avec playerId = " + playerId, null);
        } else {
            log("Connexion inactive, impossible de notifier le départ du joueur.", null);
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
     * @return Un {@link BehaviorSubject} émettant des objets {@link Player} représentant les joueurs qui rejoignent.
     */
    public BehaviorSubject<Player> getPlayerJoinedObservable() {
        return playerJoinedSubject;
    }

    /**
     * Retourne un observable pour les changements de statut de préparation des joueurs.
     *
     * @return Un {@link BehaviorSubject} émettant des objets {@link Player} contenant les statuts de préparation.
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
     * Retourne l'objet HubConnection utilisé pour gérer la connexion SignalR.
     *
     * @return L'objet {@link HubConnection}.
     */
    public HubConnection getHubConnection() {
        return hubConnection;
    }

    /**
     * Retourne un observable pour les événements de synchronisation de session.
     *
     * @return Un {@link BehaviorSubject} émettant des objets {@link GameSession} représentant l'état complet de la session.
     */
    public BehaviorSubject<GameSession> getSyncSessionStateObservable() {
        return syncSessionStateSubject;
    }

    /**
     * Retourne un observable pour les messages informant qu'un état de préparation n'est pas autorisé.
     *
     * @return Un {@link BehaviorSubject} émettant des chaînes de caractères contenant les messages d'erreur.
     */
    public BehaviorSubject<String> getReadyNotAllowedObservable() {
        return readyNotAllowedSubject;
    }

    /**
     * Retourne un observable pour les messages de notification envoyés par le serveur.
     *
     * @return Un {@link BehaviorSubject} émettant des chaînes de caractères contenant les messages de notification.
     */
    public BehaviorSubject<String> getNotifyMessageObservable() {
        return notifyMessageSubject;
    }

    /**
     * Libère les ressources de la connexion en arrêtant tout abonnement actif.
     */
    private void disposeConnection() {
        if (connectionDisposable != null && !connectionDisposable.isDisposed()) {
            connectionDisposable.dispose();
        }
    }

    /**
     * Notifie le serveur que la session a été supprimée.
     *
     * @param sessionId L'identifiant de la session supprimée.
     */
    public void notifySessionDeleted(String sessionId) {
        hubConnection.send("SessionDeleted", sessionId);
    }

    /**
     * Retourne un observable pour les événements de suppression de session.
     *
     * @return Un {@link BehaviorSubject} émettant l'identifiant des sessions supprimées.
     */
    public BehaviorSubject<String> getSessionDeletedObservable() {
        return SessionDeletedSubject;
    }

    /**
     * Retourne un observable pour les événements de joueurs supprimés de la session.
     *
     * @return Un {@link BehaviorSubject} émettant les identifiants des joueurs supprimés.
     */
    public BehaviorSubject<String> getPlayerRemovedObservable() {
        return PlayerRemovedSubject;
    }

    public BehaviorSubject<Integer> getTimerDurationObservable() {
        return timerDurationSubject;
    }

    public BehaviorSubject<int[]> getGameStatisticsObservable() {
        return gameStatisticsSubject;
    }

}