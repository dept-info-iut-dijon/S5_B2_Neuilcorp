﻿using System.Text.Json.Serialization;

/// <summary>
/// Classe représentant les coordonnées X et Y.
/// </summary>
public class Coordonnees
{
    
    private int x;
    private int y;

    [JsonPropertyName("x")]
    public int X 
    {
        get { return x; } 
        set { x = value; }
    }

    [JsonPropertyName("y")]
    public int Y 
    { 
        get { return y;}
        set { y = value;}
    }

    public Coordonnees(int x, int y)
    {
        this.X = x;
        this.Y = y;
    }
}
