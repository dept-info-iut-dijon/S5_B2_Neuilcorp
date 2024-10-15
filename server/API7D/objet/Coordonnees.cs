using System.Text.Json.Serialization;

/// <summary>
/// Classe représentant les coordonnées X et Y.
/// </summary>
public class Coordonnees
{
    // Propriétés publiques pour X et Y
    [JsonPropertyName("x")]
    public int X { get; set; }

    [JsonPropertyName("y")]
    public int Y { get; set; }

    // Constructeur avec paramètres pour initialiser les coordonnées
    public Coordonnees(int x, int y)
    {
        this.X = x;
        this.Y = y;
    }
}
