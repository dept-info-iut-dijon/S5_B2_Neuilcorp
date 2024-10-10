package com.example.spotthedifference;

/// <summary>
/// Classe représentant les coordonnées X et Y.
/// </summary>
public class Coordonnees {
    private float x;
    private float y;

    /// <summary>
    /// Constructeur pour initialiser les coordonnées.
    /// </summary>
    public Coordonnees(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /// <summary>
    /// Récupérer la coordonnée X.
    /// </summary>
    public float getX() {
        return x;
    }

    /// <summary>
    /// Récupérer la coordonnée Y.
    /// </summary>
    public float getY() {
        return y;
    }
}
