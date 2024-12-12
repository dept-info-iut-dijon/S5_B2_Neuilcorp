package com.example.spotthedifference.WebSocket;

/**
 * Interface définissant le contrat pour la gestion du démarrage d'une partie.
 * Permet aux composants de l'application (activités, fragments) d'être notifiés
 * lorsqu'une nouvelle partie démarre et de recevoir les données nécessaires
 * à l'initialisation du jeu.
 * 
 * Cette interface est principalement utilisée avec SignalR pour la communication
 * en temps réel avec le serveur.
 */
public interface GameStartedListener {

    /**
     * Appelée par le système lorsqu'une nouvelle partie démarre.
     * Cette méthode fournit les données nécessaires pour initialiser
     * l'interface de jeu et afficher l'image à comparer.
     *
     * @param imageData    Données binaires de l'image au format byte[].
     *                     Ces données doivent être décodées pour afficher l'image.
     *                     Ne peut pas être null.
     * @param imagePairId  Identifiant unique de la paire d'images pour cette partie.
     *                     Utilisé pour la synchronisation et le suivi des différences.
     *                     Ne peut pas être null.
     * @throws IllegalArgumentException si imageData est null ou vide
     * @throws IllegalArgumentException si imagePairId est null
     */
    void onGameStarted(byte[] imageData, Integer imagePairId);
}