package com.example.spotthedifference.models;

/**
 * Interface représentant les coordonnées X et Y dans un système de coordonnées.
 * Fournit des méthodes pour obtenir et définir les valeurs des coordonnées.
 */
public interface ICoordonnees {

    /**
     * Obtient la coordonnée X.
     *
     * @return La valeur de X.
     */
    int getX();

    /**
     * Obtient la coordonnée Y.
     *
     * @return La valeur de Y.
     */
    int getY();

    /**
     * Définit la coordonnée X.
     *
     * @param x La nouvelle valeur de X.
     */
    void setX(int x);

    /**
     * Définit la coordonnée Y.
     *
     * @param y La nouvelle valeur de Y.
     */
    void setY(int y);
}
