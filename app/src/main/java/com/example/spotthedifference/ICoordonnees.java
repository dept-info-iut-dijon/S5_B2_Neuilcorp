package com.example.spotthedifference;

/// <summary>
/// Interface contenant toutes les méthodes de Coordonnees.
/// </summary>
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
