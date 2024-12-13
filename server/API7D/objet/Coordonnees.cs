using System.Text.Json.Serialization;

/// <summary>
/// Classe représentant les coordonnées X et Y.
/// </summary>
public class Coordonnees
{
    
    private int x;
    private int y;

    /// <summary>
    /// obtient ou définit la coordonnée X
    /// </summary>
    public int X 
    {
        get { return x; } 
        set { x = value; }
    }

    /// <summary>
    /// obtient ou définit la coordonnée Y
    /// </summary>
    public int Y 
    { 
        get { return y;}
        set { y = value;}
    }

    /// <summary>
    /// Initialise une nouvelle instance de la classe Coordonnees
    /// </summary>
    /// <param name="x">La coordonnées X</param>
    /// <param name="y">La coordonnées Y</param>
    public Coordonnees(int x, int y)
    {
        this.X = x;
        this.Y = y;
    }

    public override bool Equals(object obj)
    {
        bool result = false;
        if (obj is Coordonnees other)
        {

            result = this.X == other.X && this.Y == other.Y;
        }
        return result;
    }
}
