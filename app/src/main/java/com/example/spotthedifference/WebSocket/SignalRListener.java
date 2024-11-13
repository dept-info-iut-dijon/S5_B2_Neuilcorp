package com.example.spotthedifference.WebSocket;

public interface SignalRListener {
    /**
     * Appelé lorsqu'un joueur rejoint la session.
     *
     * @param playerName Nom du joueur qui a rejoint.
     */
    void onPlayerJoined(String playerName);

    /**
     * Appelé lorsque le statut de préparation d'un joueur est mis à jour.
     *
     * @param playerId Identifiant du joueur.
     * @param isReady  Statut de préparation du joueur.
     */
    void onPlayerReadyStatusChanged(String playerId, boolean isReady);
}