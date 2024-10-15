namespace API7D.Metier;

using API7D.DATA;
using API7D.objet;
using System;
using System.Collections.Generic;

public class DifferanceChecker : IDifferanceChecker
{
    // Liste des différences (exemple : chaque différence est un point avec des coordonnées x, y)
    private List<Coordonnees> differences;
    private const int AcceptanceRadius = 20; // Rayon d'acceptation en pixels

    public DifferanceChecker()
    {
        IDifferanceCheckerDATA _data = new DifferanceCheckerDATA();
        this.differences = _data.GetListDifferanceCoordinates();
    }

    // Méthode pour vérifier si les coordonnées fournies sont proches d'une des différences
    public bool IsWithinDifference(Coordonnees coordinate)
    {
        foreach (var difference in differences)
        {
            // Calcul de la distance euclidienne entre les coordonnées entrées et la différence
            double distance = Math.Sqrt(Math.Pow(difference.X - coordinate.X, 2) + Math.Pow(difference.Y - coordinate.Y, 2));

            // Si la distance est inférieure ou égale au rayon d'acceptation, la différence est considérée comme trouvée
            if (distance <= AcceptanceRadius)
            {
                return true;
            }
        }

        // Si aucune différence n'a été trouvée dans la zone d'acceptation
        return false;
    }
}
