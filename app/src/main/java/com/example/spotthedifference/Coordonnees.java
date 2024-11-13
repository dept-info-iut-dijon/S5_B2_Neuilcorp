package com.example.spotthedifference;

/**
 * Classe représentant les coordonnées X et Y.
 */
public class Coordonnees implements ICoordonnees {
    private int x;
    private int y;

    /**
     * Constructeur avec paramètres pour initialiser les coordonnées.
     *
     * @param x Coordonnée X.
     * @param y Coordonnée Y.
     */
    public Coordonnees(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Obtient la coordonnée X.
     *
     * @return la valeur de X.
     */
    @Override
    public int getX() {
        return x;
    }

    /**
     * Définit la coordonnée X.
     *
     * @param x Nouvelle coordonnée X.
     */
    @Override
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Obtient la coordonnée Y.
     *
     * @return la valeur de Y.
     */
    @Override
    public int getY() {
        return y;
    }

    /**
     * Définit la coordonnée Y.
     *
     * @param y Nouvelle coordonnée Y.
     */
    @Override
    public void setY(int y) {
        this.y = y;
    }

    /**
     * Fournit une représentation sous forme de chaîne pour déboguer les coordonnées.
     *
     * @return Une chaîne représentant les coordonnées (X, Y).
     */
    @Override
    public String toString() {
        return "Coordonnees{" + "x=" + x + ", y=" + y + '}';
    }
}