package com.example.spotthedifference.WebSocket;

import android.util.Log;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import io.reactivex.schedulers.Schedulers;

public class SignalRClient {

    private static final String SERVER_URL = "http://203.55.81.18:5195/gameSessionHub";

    private HubConnection hubConnection;

    /// <summary>
    /// Constructeur de SignalRClient. Initialise la connexion au serveur SignalR.
    /// </summary>
    public SignalRClient() {
        hubConnection = HubConnectionBuilder.create(SERVER_URL)
                .build();

        // Écoute les événements "PlayerJoined" pour informer que le joueur a rejoint la session
        hubConnection.on("PlayerJoined", (playerName) -> {
            Log.d("SignalRClient", playerName + " a rejoint la session");
        }, String.class);

        // Écoute les changements de statut "Prêt" pour mettre à jour les autres clients
        hubConnection.on("PlayerReadyStatusChanged", (playerId, isReady) -> {
            Log.d("SignalRClient", "Statut de préparation mis à jour pour le joueur " + playerId + ": " + isReady);
        }, String.class, Boolean.class);
    }

    /// <summary>
    /// Démarre la connexion WebSocket au serveur SignalR.
    /// </summary>
    public void startConnection() {
        hubConnection.start()
                .subscribeOn(Schedulers.io()) // Exécute la connexion en arrière-plan
                .doOnComplete(() -> Log.d("SignalRClient", "Connexion WebSocket établie."))
                .doOnError(error -> Log.e("SignalRClient", "Erreur lors de la connexion WebSocket : " + error.getMessage(), error))
                .blockingAwait(); // Attendre que la connexion soit établie
    }

    /// <summary>
    /// Arrête la connexion WebSocket au serveur SignalR.
    /// </summary>
    public void stopConnection() {
        hubConnection.stop()
                .subscribeOn(Schedulers.io()) // Assure que l'arrêt est aussi asynchrone
                .doOnComplete(() -> Log.d("SignalRClient", "Connexion WebSocket fermée."))
                .blockingAwait();
    }

    /// <summary>
    /// Rejoint un groupe de session spécifique sur le serveur.
    /// </summary>
    public void joinSessionGroup(String sessionId) {
        hubConnection.send("JoinSessionGroup", sessionId);
    }

    /// <summary>
    /// Notifie le serveur que le joueur a rejoint la session.
    /// </summary>
    public void notifyPlayerJoined(String sessionId, String playerName) {
        hubConnection.send("PlayerJoined", sessionId, playerName);
    }

    /**
     * Envoie la mise à jour du statut de préparation d'un joueur au serveur.
     *
     * @param sessionId L'ID de la session de jeu.
     * @param playerId  L'ID du joueur dont le statut est modifié.
     * @param isReady   Le statut de préparation du joueur.
     */
    public void sendReadyStatusUpdate(String sessionId, String playerId, boolean isReady) {
        Log.d("SignalRClient", "Envoi de la mise à jour de préparation pour " + playerId + " à " + isReady);
        hubConnection.send("SetPlayerReadyStatus", sessionId, playerId, isReady);
    }


    public HubConnection getConnection() {
        return hubConnection;
    }
}
