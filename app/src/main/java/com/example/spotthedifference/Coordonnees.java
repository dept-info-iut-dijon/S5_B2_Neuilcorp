package com.example.spotthedifference;

/// <summary>
/// Classe représentant les coordonnées X et Y.
/// </summary>
public class Coordonnees implements ICoordonnees {
    private int x;
    private int y;

    // Constructeur avec paramètres pour initialiser les coordonnées
    public Coordonnees(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Getters
    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    // Setters
    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }
}
