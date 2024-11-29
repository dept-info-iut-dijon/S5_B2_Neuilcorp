namespace API7D.Metier;

using API7D.DATA;
using API7D.objet;
using API7D.Services;
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
    public bool IsWithinDifference(Coordonnees coordinate, int idImagePaire,string SessionID,SessionService _sessionService,string playerID)
    {
        GameSession gameSession = _sessionService.GetSessionById(SessionID);
        gameSession.PlayerSelections.Add(playerID, (coordinate.X, coordinate.Y));
        // Récupère la liste des différences pour la paire d'images donnée
        List<Coordonnees> pairDifferences = differences[idImagePaire];
        bool result = false;

        if (gameSession.PlayerSelections.Count == gameSession.Players.Count())
        {
            bool isDifferenceValid = false;

            foreach (var difference in pairDifferences)
            {
                // Vérifie si tous les joueurs ont sélectionné une position autour de la même différence
                bool allPlayersSelectedSameDifference = true;

                foreach (var playerSelection in gameSession.PlayerSelections.Values)
                {
                    double distance = Math.Sqrt(
                        Math.Pow(difference.X - playerSelection.x, 2) +
                        Math.Pow(difference.Y - playerSelection.y, 2)
                    );

                    // Si un joueur n'est pas dans la zone d'acceptation, cette différence n'est pas validée
                    if (distance > AcceptanceRadius)
                    {
                        allPlayersSelectedSameDifference = false;
                        break;
                    }
                }

                // Si tous les joueurs ont validé une différence, on termine la vérification
                if (allPlayersSelectedSameDifference)
                {
                    isDifferenceValid = true;
                    break;
                }
            }
            
            if (isDifferenceValid)
            {
                // Tous les joueurs ont sélectionné une différence valide
                Console.WriteLine("Différence validée !");
                // Réinitialiser les sélections des joueurs pour la prochaine tentative
                gameSession.PlayerSelections.Clear();
                result = true;
            }
            else
            {
                // Au moins un joueur s'est trompé
                Console.WriteLine("Erreur, tous les joueurs doivent sélectionner la même différence.");
                // Réinitialiser les sélections des joueurs pour la prochaine tentative
                gameSession.PlayerSelections.Clear();
                result = false;
            }
            


        }
        return result;
    }
}
