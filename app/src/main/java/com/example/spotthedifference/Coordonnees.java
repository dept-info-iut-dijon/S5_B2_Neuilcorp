package com.example.spotthedifference;

/// <summary>
/// Classe représentant les coordonnées X et Y.
/// </summary>
public class Coordonnees {
    private int x;
    private int y;

    // Constructeur avec paramètres pour initialiser les coordonnées
    public Coordonnees(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Getters
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    // Setters
    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
}
