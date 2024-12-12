using API7D.Metier;
using API7D.objet;
using Microsoft.AspNetCore.SignalR;

namespace API7D.Services
{
    /// <summary>
    /// Service responsable de la gestion des sessions de jeu.
    /// </summary>
    public class SessionService
    {
        private readonly List<GameSession> _sessions = new List<GameSession>();
        private readonly IHubContext<GameSessionHub> _hubContext;
        private readonly ILogger<SessionService> _logger;

        /// <summary>
        /// Initialise une nouvelle instance du service de gestion des sessions.
        /// </summary>
        /// <param name="hubContext">Contexte du hub SignalR pour la communication en temps réel</param>
        /// <param name="logger">Logger pour le traçage des événements</param>
        public SessionService(IHubContext<GameSessionHub> hubContext, ILogger<SessionService> logger)
        {
            _hubContext = hubContext;
            _logger = logger;
        }

        /// <summary>
        /// Ajoute une nouvelle session de jeu.
        /// </summary>
        /// <param name="gameSession">La session de jeu à ajouter.</param>
        /// <exception cref="ArgumentNullException">Lancée si <paramref name="gameSession"/> est null.</exception>
        /// <exception cref="InvalidOperationException">Lancée si une session avec le même ID existe déjà.</exception>
        public void AddSession(GameSession gameSession)
        {
            if (gameSession == null)
            {
                throw new ArgumentNullException(nameof(gameSession), "La session de jeu ne peut pas être null.");
            }

            if (_sessions.Any(s => s.SessionId == gameSession.SessionId))
            {
                throw new InvalidOperationException($"Une session avec l'ID {gameSession.SessionId} existe déjà.");
            }

            _sessions.Add(gameSession);
        }

        /// <summary>
        /// Récupère une copie immuable de toutes les sessions de jeu.
        /// </summary>
        /// <returns>Une liste en lecture seule des sessions de jeu.</returns>
        public IReadOnlyList<GameSession> GetAllSessions()
        {
            return _sessions.AsReadOnly();
        }

        /// <summary>
        /// Récupère une session par son ID.
        /// </summary>
        /// <param name="sessionId">L'ID de la session.</param>
        /// <returns>La session correspondante ou null si elle n'existe pas.</returns>
        public GameSession GetSessionById(string sessionId)
        {
            return _sessions.FirstOrDefault(s => s.SessionId == sessionId);
        }

        /// <summary>
        /// Supprime une session de jeu par son ID.
        /// </summary>
        /// <param name="sessionId">L'ID de la session à supprimer.</param>
        /// <returns>True si la session est supprimée, sinon False.</returns>
        /// <exception cref="ArgumentException">Lancée si <paramref name="sessionId"/> est null ou vide.</exception>
        public bool RemoveSession(string sessionId)
        {
            if (string.IsNullOrEmpty(sessionId))
            {
                throw new ArgumentException("L'ID de la session est requis.", nameof(sessionId));
            }

            GameSession session = GetSessionById(sessionId);
            return session != null && _sessions.Remove(session);
        }

        /// <summary>
        /// Met à jour une session existante en fonction de son ID.
        /// </summary>
        /// <param name="updatedSession">La session de jeu mise à jour.</param>
        /// <exception cref="ArgumentNullException">Lancée si <paramref name="updatedSession"/> est null.</exception>
        /// <exception cref="KeyNotFoundException">Lancée si l'ID de la session n'est pas trouvé.</exception>
        public void UpdateSession(GameSession updatedSession)
        {
            if (updatedSession == null)
            {
                throw new ArgumentNullException(nameof(updatedSession), "La session mise à jour ne peut pas être null.");
            }

            int index = _sessions.FindIndex(s => s.SessionId == updatedSession.SessionId);
            bool sessionExists = index >= 0;

            if (sessionExists)
            {
                _sessions[index] = updatedSession;
            }
            else
            {
                throw new KeyNotFoundException($"Session avec l'ID {updatedSession.SessionId} introuvable.");
            }
        }

        /// <summary>
        /// Supprime un joueur d'une session.
        /// </summary>
        /// <param name="sessionId">ID de la session.</param>
        /// <param name="playerId">ID du joueur à retirer.</param>
        /// <returns>True si le joueur a été retiré, sinon False.</returns>
        public bool RemovePlayerFromSession(string sessionId, string playerId)
        {
            GameSession session = GetSessionById(sessionId);
            bool result = false;

            if (session != null && session.ContainsPlayer(playerId))
            {
                Player playerToRemove = session.Players.FirstOrDefault(p => p.PlayerId == playerId);

                if (playerToRemove != null)
                {
                    session.Players.Remove(playerToRemove);

                    // Si le joueur est l'hôte, supprimer la session
                    if (session.IsHost(playerId))
                    {
                        RemoveSession(sessionId);
                    }
                    else
                    {
                        UpdateSession(session);
                    }
                    result = true;
                }
            }

            return result;
        }

        /// <summary>
        /// Notifie tous les joueurs d'une session d'un résultat de différence.
        /// </summary>
        /// <param name="sessionId">Identifiant de la session.</param>
        /// <param name="isInZone">Résultat de validation de la différence (true ou false).</param>
        /// <returns>Tâche asynchrone représentant l'opération de notification.</returns>
        /// <exception cref="ArgumentException">Lancée si <paramref name="sessionId"/> est null ou vide.</exception>
        /// <exception cref="Exception">Lancée si une erreur se produit lors de la notification via SignalR.</exception>
        public async Task NotifyPlayers(string sessionId, bool isInZone)
        {
            if (string.IsNullOrEmpty(sessionId))
            {
                _logger.LogError("SessionId ne peut pas être null ou vide.");
                throw new ArgumentException("SessionId est requis.", nameof(sessionId));
            }

            try
            {
                // Notifie tous les clients appartenant au groupe SignalR correspondant à la session
                await _hubContext.Clients.Group(sessionId).SendAsync("ResultNotification", isInZone);
                _logger.LogInformation($"Tous les joueurs de la session {sessionId} ont été notifiés du résultat : {isInZone}.");
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, $"Erreur lors de la notification des joueurs de la session {sessionId}.");
                throw;
            }
        }
    }
}
