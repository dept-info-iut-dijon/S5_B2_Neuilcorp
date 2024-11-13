package com.example.spotthedifference.data.model;

/**
 * Classe représentant les coordonnées X et Y.
 */
public class Coordonnees {
    private int x;
    private int y;

    /**
     * Constructeur avec paramètres pour initialiser les coordonnées.
     *
     * @param x La coordonnée X.
     * @param y La coordonnée Y.
     */
    public Coordonnees(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Récupère la valeur de la coordonnée X.
     *
     * @return La coordonnée X.
     */
    public int getX() {
        return x;
    }

    /**
     * Récupère la valeur de la coordonnée Y.
     *
     * @return La coordonnée Y.
     */
    public int getY() {
        return y;
    }

    /**
     * Définit la valeur de la coordonnée X.
     *
     * @param x La nouvelle coordonnée X.
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Définit la valeur de la coordonnée Y.
     *
     * @param y La nouvelle coordonnée Y.
     */
    public void setY(int y) {
        this.y = y;
    }
}
