package com.example.spotthedifference;

public interface IPlayer {

    // Méthode pour obtenir l'ID du joueur
    String getPlayerId();

    // Méthode pour définir l'ID du joueur
    void setPlayerId(String playerId);

    // Méthode pour obtenir le nom du joueur
    String getName();

    // Méthode pour définir le nom du joueur
    void setName(String name);

    // Méthode pour vérifier si le joueur est prêt
    boolean isReady();

    // Méthode pour définir l'état de préparation du joueur
    void setReady(boolean ready);
}
