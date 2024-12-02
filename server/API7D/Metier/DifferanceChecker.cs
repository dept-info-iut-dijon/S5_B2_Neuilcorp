namespace API7D.Metier;

using API7D.DATA;
using API7D.objet;
using API7D.Services;
using Microsoft.Extensions.Logging;
using System;
using System.Collections.Generic;

public class DifferanceChecker : IDifferanceChecker
{
    private Dictionary<int, List<Coordonnees>> differences;
    private const int AcceptanceRadius = 100; // Rayon d'acceptation en pixels
    private readonly ILogger<DifferanceChecker> _logger;

    public DifferanceChecker(ILogger<DifferanceChecker> logger)
    {
        IDifferanceCheckerDATA data = new DifferanceCheckerDATA(); // Instancier l'accès aux données
        this.differences = data.getAllDifferance(); // Charger toutes les différences
        _logger = logger;
    }

    /// <summary>
    /// Vérifie si les coordonnées fournies se situent dans une zone de différence
    /// pour une paire d'images spécifique.
    /// </summary>
    /// <param name="coordinate">Coordonnées à vérifier</param>
    /// <param name="idImagePaire">ID de la paire d'images</param>
    /// <returns>True si les coordonnées sont dans une zone de différence, sinon False</returns>
    private readonly object _lock = new object(); // Pour synchroniser l'accès
    private readonly Dictionary<string, TaskCompletionSource<bool>> _sessionTasks = new Dictionary<string, TaskCompletionSource<bool>>();

    public async Task<bool> IsWithinDifferenceAsync(
        Coordonnees coordinate,
        int idImagePaire,
        string SessionID,
        SessionService _sessionService,
        string playerID)
    {
        _logger.LogInformation($"Vérification de la différence pour le joueur {playerID} dans la session {SessionID} avec les coordonnées ({coordinate.X}, {coordinate.Y}) et l'image paire {idImagePaire}.");
        GameSession gameSession = _sessionService.GetSessionById(SessionID);

        // Ajout sécurisé de la sélection du joueur
        lock (_lock)
        {
            if (!gameSession.PlayerSelections.ContainsKey(playerID))
            {
                gameSession.PlayerSelections.Add(playerID, (coordinate.X, coordinate.Y));
                _logger.LogInformation($"Coordonnées ajoutées pour le joueur {playerID}: ({coordinate.X}, {coordinate.Y}).");
            }
            else
            {
                _logger.LogWarning($"Le joueur {playerID} a déjà soumis des coordonnées.");
            }
        }

        // Vérifie si un TaskCompletionSource existe déjà pour cette session
        TaskCompletionSource<bool> sessionTask;
        lock (_lock)
        {
            if (!_sessionTasks.TryGetValue(SessionID, out sessionTask))
            {
                sessionTask = new TaskCompletionSource<bool>();
                _sessionTasks[SessionID] = sessionTask;
            }
        }

        // Si tous les joueurs ont fait leur sélection, on procède à la validation
        if (gameSession.PlayerSelections.Count == gameSession.Players.Count())
        {
            _logger.LogInformation($"Toutes les sélections sont prêtes pour la session {SessionID}. Validation en cours...");
            bool isDifferenceValid = false;

            if (!differences.ContainsKey(idImagePaire))
            {
                _logger.LogError($"Aucune différence trouvée pour l'image paire {idImagePaire}.");
                throw new ArgumentException($"Aucune différence trouvée pour l'image paire {idImagePaire}.");
            }
            // Récupère la liste des différences pour la paire d'images donnée
            List<Coordonnees> pairDifferences = differences[idImagePaire];

            foreach (var difference in pairDifferences)
            {
                _logger.LogInformation($"Validation pour la différence centrée sur ({difference.X}, {difference.Y}).");
                // Vérifie si tous les joueurs ont sélectionné une position autour de la même différence
                bool allPlayersSelectedSameDifference = true;

                foreach (var playerSelection in gameSession.PlayerSelections.Values)
                {
                    double distance = Math.Sqrt(
                        Math.Pow(difference.X - playerSelection.x, 2) +
                        Math.Pow(difference.Y - playerSelection.y, 2)
                    );

                    _logger.LogInformation($"Distance calculée entre ({difference.X}, {difference.Y}) et ({playerSelection.x}, {playerSelection.y}): {distance} pixels.");

                    // Si un joueur n'est pas dans la zone d'acceptation, cette différence n'est pas validée
                    if (distance > AcceptanceRadius)
                    {
                        _logger.LogWarning($"Un joueur est en dehors de la zone d'acceptation (radius = {AcceptanceRadius}). Distance: {distance}.");
                        allPlayersSelectedSameDifference = false;
                        break;
                    }
                }

                // Si tous les joueurs ont validé une différence, on termine la vérification
                if (allPlayersSelectedSameDifference)
                {
                    isDifferenceValid = true;
                    _logger.LogInformation("Tous les joueurs ont validé cette différence.");
                    break;
                }
            }

            // Réinitialise les sélections des joueurs pour la prochaine tentative
            lock (_lock)
            {
                _logger.LogInformation("Réinitialisation des sélections des joueurs.");
                gameSession.PlayerSelections.Clear();
                _sessionTasks.Remove(SessionID);
            }

            // Complète la tâche pour notifier tous les appels en attente
            sessionTask.SetResult(isDifferenceValid);
        }

        // Attend la validation de la sélection de tous les joueurs
        return await sessionTask.Task;
    }
}
