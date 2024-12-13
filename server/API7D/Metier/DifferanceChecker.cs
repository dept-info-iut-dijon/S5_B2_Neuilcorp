namespace API7D.Metier;

using API7D.DATA;
using API7D.objet;
using API7D.Services;
using Microsoft.Extensions.Logging;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

public class DifferanceChecker : IDifferanceChecker
{
    private Dictionary<int, List<Coordonnees>> differences;
    private const int AcceptanceRadius = 100; // Rayon d'acceptation en pixels
    private readonly ILogger<DifferanceChecker> _logger;

    private readonly object _lock = new object(); // Synchronisation
    private readonly Dictionary<string, TaskCompletionSource<bool>> _sessionTasks = new Dictionary<string, TaskCompletionSource<bool>>();

    public DifferanceChecker(ILogger<DifferanceChecker> logger)
    {
        IDifferanceCheckerDATA data = new DifferanceCheckerDATA();
        this.differences = data.getAllDifferance();
        _logger = logger;
    }

    /// <summary>
    /// Ajoute la sélection d'un joueur de manière sécurisée.
    /// </summary>
    private void AddPlayerSelection(GameSession gameSession, string playerID, Coordonnees coordinate)
    {
        lock (_lock)
        {
            var playerselec = gameSession.PlayerSelections;
            if (!playerselec.ContainsKey(playerID))
            {
                gameSession.PlayerSelections.Add(playerID, (coordinate.X, coordinate.Y));
                _logger.LogInformation($"Coordonnées ajoutées pour le joueur {playerID}: ({coordinate.X}, {coordinate.Y}).");
            }
            else
            {
                _logger.LogWarning($"Le joueur {playerID} a déjà soumis des coordonnées.");
            }
        }
    }

    /// <summary>
    /// Initialise ou récupère un TaskCompletionSource pour la session.
    /// </summary>
    private TaskCompletionSource<bool> GetOrCreateSessionTask(string sessionId)
    {
        TaskCompletionSource<bool> sessionTask;
        lock (_lock)
        {
            if (!_sessionTasks.TryGetValue(sessionId, out sessionTask))
            {
                sessionTask = new TaskCompletionSource<bool>();
                _sessionTasks[sessionId] = sessionTask;
            }
        }
        return sessionTask;
    }

    /// <summary>
    /// Valide les différences sélectionnées par tous les joueurs.
    /// </summary>
    private bool ValidatePlayerSelections(GameSession gameSession, List<Coordonnees> pairDifferences)
    {
        List<Coordonnees> gamesessionDiffList = gameSession.DifferenceTrouver;
        bool isDifferenceValid = false;
        var playerSelec = gameSession.PlayerSelections;
        foreach (var difference in pairDifferences)
        {
            _logger.LogInformation($"Validation pour la différence centrée sur ({difference.X}, {difference.Y}).");
            if (AllPlayersSelectedSameDifference(playerSelec.Values, difference))
            {
                if (!gameSession.DifferenceTrouver.Contains(difference))
                {
                    gameSession.DifferenceTrouver.Add(difference);
                    _logger.LogInformation("Tous les joueurs ont validé cette différence.");
                    isDifferenceValid = true;

                }
                else
                {
                    _logger.LogInformation("cette difference a deja été trouver");
                    isDifferenceValid = true;
                }


                break;
            }
        }
        return isDifferenceValid;
    }

    /// <summary>
    /// Vérifie si tous les joueurs ont sélectionné la même différence.
    /// </summary>
    private bool AllPlayersSelectedSameDifference(IEnumerable<(int x, int y)> playerSelections, Coordonnees difference)
    {
        bool allPlayersValid = true;

        foreach (var selection in playerSelections)
        {
            double distance = Math.Sqrt(
                Math.Pow(difference.X - selection.x, 2) +
                Math.Pow(difference.Y - selection.y, 2)
            );

            _logger.LogInformation($"Distance calculée entre ({difference.X}, {difference.Y}) et ({selection.x}, {selection.y}): {distance} pixels.");

            if (distance > AcceptanceRadius)
            {
                _logger.LogWarning($"Un joueur est en dehors de la zone d'acceptation (radius = {AcceptanceRadius}). Distance: {distance}.");
                allPlayersValid = false;
                break;
            }
        }
        return allPlayersValid;
    }

    /// <summary>
    /// Réinitialise les sélections des joueurs pour une nouvelle tentative.
    /// </summary>
    private void ResetPlayerSelections(GameSession gameSession, string sessionId)
    {
        lock (_lock)
        {
            _logger.LogInformation("Réinitialisation des sélections des joueurs.");
            gameSession.PlayerSelections.Clear();
            _sessionTasks.Remove(sessionId);
        }
    }

    /// <summary>
    /// Vérifie si les coordonnées fournies se situent dans une zone de différence
    /// pour une paire d'images spécifique.
    /// </summary>
    public async Task<bool> IsWithinDifferenceAsync(
        Coordonnees coordinate,
        int idImagePaire,
        string sessionId,
        SessionService sessionService,
        string playerId)
    {
        
        _logger.LogInformation($"Vérification de la différence pour le joueur {playerId} dans la session {sessionId} avec les coordonnées ({coordinate.X}, {coordinate.Y}) et l'image paire {idImagePaire}.");
        var gameSession = sessionService.GetSessionById(sessionId);

        AddPlayerSelection(gameSession, playerId, coordinate);

        var sessionTask = GetOrCreateSessionTask(sessionId);
        var playerSelec = gameSession.PlayerSelections;
        var playerSess = gameSession.Players;

        if (playerSelec.Count == playerSess.Count())
        {
            _logger.LogInformation($"Toutes les sélections sont prêtes pour la session {sessionId}. Validation en cours...");

            if (!differences.ContainsKey(idImagePaire))
            {
                _logger.LogError($"Aucune différence trouvée pour l'image paire {idImagePaire}.");
                throw new ArgumentException($"Aucune différence trouvée pour l'image paire {idImagePaire}.");
            }

            var pairDifferences = differences[idImagePaire];
            bool isDifferenceValid = ValidatePlayerSelections(gameSession, pairDifferences);

            ResetPlayerSelections(gameSession, sessionId);
            sessionTask.SetResult(isDifferenceValid);
            if (gameSession.DifferenceTrouver.Count() == differences[idImagePaire].Count())
            {
                gameSession.GameCompleted = true;
            }
        }

        return await sessionTask.Task;
    }
}
