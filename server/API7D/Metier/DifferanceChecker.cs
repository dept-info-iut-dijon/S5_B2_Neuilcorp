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

    private readonly object _lock = new object(); // Synchronisation
    private readonly Dictionary<string, TaskCompletionSource<bool>> _sessionTasks = new Dictionary<string, TaskCompletionSource<bool>>();

    public DifferanceChecker()
    {
        IDifferanceCheckerDATA data = new DifferanceCheckerDATA();
        this.differences = data.getAllDifferance();
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
            if (AllPlayersSelectedSameDifference(playerSelec.Values, difference))
            {
                if (!gameSession.DifferenceTrouver.Contains(difference))
                {
                    gameSession.DifferenceTrouver.Add(difference);
                    isDifferenceValid = true;

                }
                else
                {
                    isDifferenceValid = false;
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

            if (distance > AcceptanceRadius)
            {
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
            gameSession.PlayerSelections.Clear();
            _sessionTasks.Remove(sessionId);
        }
    }

    /// <summary>
    /// Vérifie si les coordonnées fournies se situent dans une zone de différence
    /// pour une paire d'images spécifique.
    /// </summary>
    public async Task<int> IsWithinDifferenceAsync(
        Coordonnees coordinate,
        int idImagePaire,
        string sessionId,
        SessionService sessionService,
        string playerId)
    {
        int result = 0;
        var gameSession = sessionService.GetSessionById(sessionId);

        if (gameSession == null)
            throw new ArgumentException($"Session {sessionId} introuvable.");

        if (IsTimerExpired(gameSession))
        {
            gameSession.TimersExpired++;
            ResetPlayerSelections(gameSession, sessionId);
            ResetTimer(gameSession);
            result = 1;
        }
        else { 
            AddPlayerSelection(gameSession, playerId, coordinate);

            var sessionTask = GetOrCreateSessionTask(sessionId);
            var playerSelec = gameSession.PlayerSelections;
            var playerSess = gameSession.Players;

            if (playerSelec.Count == playerSess.Count())
            {
                if (!differences.ContainsKey(idImagePaire))
                {
                    throw new ArgumentException($"Aucune différence trouvée pour l'image paire {idImagePaire}.");
                }

                var pairDifferences = differences[idImagePaire];
                bool isDifferenceValid = ValidatePlayerSelections(gameSession, pairDifferences);

                if (!isDifferenceValid)
                {
                    gameSession.MissedAttempts++;
                }
                gameSession.Attempts++;
                ResetPlayerSelections(gameSession, sessionId);
                sessionTask.SetResult(isDifferenceValid);
                if (gameSession.DifferenceTrouver.Count() == differences[idImagePaire].Count())
                {
                    gameSession.GameCompleted = true;
                }

                ResetTimer(gameSession);
            }

            await sessionTask.Task;
            if (sessionTask.Task.Result == true) {
                result = 2;
            }
            else
            {
                result = 0;
            }
        }
        return result;
    }

    /// <summary>
    /// Initialise ou réinitialise le timer pour la session donnée.
    /// </summary>
    internal void ResetTimer(GameSession gameSession)
    {
        gameSession.TimerActive = true;
        gameSession.TimerStartTime = DateTime.UtcNow;
    }

    /// <summary>
    /// Méthode exposée pour réinitialiser le timer depuis l'extérieur (facultatif).
    /// </summary>
    public void StartOrResetTimer(GameSession session)
    {
        ResetTimer(session);
    }

    /// <summary>
    /// Vérifie si le timer est écoulé pour une session donnée.
    /// </summary>
    private bool IsTimerExpired(GameSession gameSession)
    {
        if (!gameSession.TimerActive)
            return false;

        var elapsedTime = DateTime.UtcNow - gameSession.TimerStartTime;
        return elapsedTime.TotalSeconds >= gameSession.TimerDuration;
    }
}
