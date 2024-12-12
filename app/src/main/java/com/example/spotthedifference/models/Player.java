package com.example.spotthedifference.models;

import java.util.UUID;

/**
 * Implémentation de l'interface IPlayer représentant un joueur dans le jeu.
 * Chaque joueur possède un identifiant unique, un nom et un état de préparation.
 */
public class Player implements IPlayer {
    
    /**
     * Identifiant unique du joueur, généré automatiquement à la création.
     */
    private final String playerId;
    
    /**
     * Nom d'affichage du joueur.
     */
    private String name;
    
    /**
     * État de préparation du joueur pour la partie.
     */
    private boolean isReady;

    /**
     * Constructeur par défaut nécessaire pour la sérialisation JSON.
     * Initialise un joueur avec un ID unique et un état non prêt.
     */
    public Player() {
        this.playerId = UUID.randomUUID().toString();
        this.isReady = false;
    }

    /**
     * Constructeur avec nom du joueur.
     *
     * @param name Nom du joueur.
     * @throws IllegalArgumentException si le nom est null ou vide.
     */
    public Player(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du joueur ne peut pas être null ou vide");
        }
        this.playerId = UUID.randomUUID().toString();
        this.name = name;
        this.isReady = false;
    }

    /**
     * Constructeur complet pour usage interne.
     *
     * @param name    Nom du joueur.
     * @param isReady État initial de préparation du joueur.
     * @throws IllegalArgumentException si le nom est null ou vide.
     */
    public Player(String name, boolean isReady) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du joueur ne peut pas être null ou vide");
        }
        this.playerId = UUID.randomUUID().toString();
        this.name = name;
        this.isReady = isReady;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPlayerId() {
        return playerId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du joueur ne peut pas être null ou vide");
        }
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isReady() {
        return isReady;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setReady(boolean ready) {
        isReady = ready;
    }
}