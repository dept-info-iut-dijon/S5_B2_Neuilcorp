package com.example.spotthedifference.WebSocket;

/**
 * Interface représentant un écouteur d'événements déclenchés lorsque le jeu commence.
 *
 * Cette interface est utilisée pour notifier les composants intéressés (comme des activités
 * ou des fragments) qu'une nouvelle partie a démarré. Elle fournit les données nécessaires
 * pour afficher ou initialiser les éléments liés au jeu.
 */
public interface GameStartedListener {

    /**
     * Méthode appelée lorsque le jeu commence.
     *
     * @param imageData    Les données de l'image encodées en tableau d'octets (byte array).
     *                     Ces données peuvent être utilisées pour afficher l'image de jeu.
     * @param imagePairId  L'identifiant de la paire d'images associées au jeu en cours.
     *                     Cet identifiant permet de suivre les images utilisées dans la partie.
     */
    void onGameStarted(byte[] imageData, Integer imagePairId);
}