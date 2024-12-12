package com.example.spotthedifference.models;

/**
 * Interface définissant le contrat pour un joueur dans le jeu.
 * Fournit les méthodes nécessaires pour gérer l'identité et l'état d'un joueur.
 */
public interface IPlayer {

    /**
     * Obtient l'identifiant unique du joueur.
     * Cet ID est généré automatiquement à la création du joueur.
     *
     * @return L'identifiant unique du joueur.
     */
    String getPlayerId();

    /**
     * Obtient le nom d'affichage du joueur.
     *
     * @return Le nom du joueur.
     */
    String getName();

    /**
     * Définit ou modifie le nom d'affichage du joueur.
     *
     * @param name Le nouveau nom du joueur.
     * @throws IllegalArgumentException si le nom est null ou vide.
     */
    void setName(String name);

    /**
     * Vérifie si le joueur est prêt à commencer la partie.
     *
     * @return true si le joueur est prêt, false sinon.
     */
    boolean isReady();

    /**
     * Définit l'état de préparation du joueur pour la partie.
     *
     * @param ready true pour indiquer que le joueur est prêt, false sinon.
     */
    void setReady(boolean ready);
}