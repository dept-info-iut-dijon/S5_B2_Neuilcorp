package com.example.spotthedifference.data.model;


/**
 * Classe représentant un joueur.
 */
public class Player {
    private String playerId;
    private String name;
    private boolean isReady;

    /**
     * Constructeur par défaut nécessaire pour la sérialisation.
     */
    public Player() {
    }


    /**
     * Constructeur avec paramètres.
     *
     * @param playerId L'identifiant du joueur.
     * @param name Le nom du joueur.
     */
    public Player(String playerId, String name) {
        this.playerId = java.util.UUID.randomUUID().toString();
        this.name = name;
        this.isReady = false;
    }

    /**
     * Récupère l'identifiant du joueur.
     *
     * @return L'identifiant du joueur.
     */
    public String getPlayerId() {
        return playerId;
    }
    /**
     * Définit l'identifiant du joueur.
     *
     * @param playerId L'identifiant du joueur à définir.
     */
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    /**
     * Récupère le nom du joueur.
     *
     * @return Le nom du joueur.
     */
    public String getName() {
        return name;
    }

    /**
     * Définit le nom du joueur.
     *
     * @param name Le nom du joueur à définir.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Récupère l'état de préparation du joueur.
     *
     * @return L'état de préparation du joueur.
     */
    public boolean isReady() {
        return isReady;
    }

    /**
     * Définit l'état de préparation du joueur.
     *
     * @param ready L'état de préparation du joueur à définir.
     */
    public void setReady(boolean ready) {
        isReady = ready;
    }
}
