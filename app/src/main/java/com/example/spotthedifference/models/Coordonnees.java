package com.example.spotthedifference.models;

/**
 * Classe représentant les coordonnées X et Y dans un système de coordonnées 2D.
 * Implémente l'interface ICoordonnees pour assurer la cohérence du contrat.
 */
public class Coordonnees implements ICoordonnees {
    private int x;
    private int y;

    /**
     * Constructeur avec paramètres pour initialiser les coordonnées.
     *
     * @param x Coordonnée X dans le plan.
     * @param y Coordonnée Y dans le plan.
     */
    public Coordonnees(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getX() {
        return x;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setX(int x) {
        this.x = x;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getY() {
        return y;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setY(int y) {
        this.y = y;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("Coordonnees{x=%d, y=%d}", x, y);
    }
}