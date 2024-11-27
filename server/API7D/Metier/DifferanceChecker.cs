namespace API7D.Metier;

using API7D.DATA;
using API7D.objet;
using System;
using System.Collections.Generic;

public class DifferanceChecker : IDifferanceChecker
{
    private Dictionary<int, List<Coordonnees>> differences;
    private const int AcceptanceRadius = 20; // Rayon d'acceptation en pixels

    public DifferanceChecker()
    {
        IDifferanceCheckerDATA data = new DifferanceCheckerDATA(); // Instancier l'accès aux données
        this.differences = data.getAllDifferance(); // Charger toutes les différences
    }

    /// <summary>
    /// Vérifie si les coordonnées fournies se situent dans une zone de différence
    /// pour une paire d'images spécifique.
    /// </summary>
    /// <param name="coordinate">Coordonnées à vérifier</param>
    /// <param name="idImagePaire">ID de la paire d'images</param>
    /// <returns>True si les coordonnées sont dans une zone de différence, sinon False</returns>
    public bool IsWithinDifference(Coordonnees coordinate, int idImagePaire)
    {
        if (!differences.ContainsKey(idImagePaire))
        {
            throw new ArgumentException($"Aucune différence trouvée pour la paire d'images avec l'ID {idImagePaire}.");
        }

        // Récupère la liste des différences pour la paire d'images donnée
        var pairDifferences = differences[idImagePaire];

        foreach (var difference in pairDifferences)
        {
            // Calcul de la distance euclidienne entre les coordonnées fournies et chaque différence
            double distance = Math.Sqrt(Math.Pow(difference.X - coordinate.X, 2) + Math.Pow(difference.Y - coordinate.Y, 2));

            if (distance <= AcceptanceRadius)
            {
                return true;
            }
        }

        return false;
    }
}
