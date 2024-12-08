using API7D.Metier;
using API7D.objet;
using Microsoft.AspNetCore.SignalR;
using Microsoft.Extensions.Logging;
using System.Collections.Generic;
using System.Linq;

namespace API7D.Services
{
    public class SessionService
    {
        private readonly List<GameSession> _sessions = new List<GameSession>();
        private readonly IHubContext<GameSessionHub> _hubContext;
        private readonly ILogger<SessionService> _logger;


        public SessionService(IHubContext<GameSessionHub> hubContext, ILogger<SessionService> logger)
        {
            _hubContext = hubContext;
            _logger = logger;
        }
        /// <summary>
        /// Ajoute une nouvelle session de jeu.
        /// </summary>
        /// <param name="gameSession">La session de jeu à ajouter.</param>
        public void AddSession(GameSession gameSession)
        {
            _sessions.Add(gameSession);
        }

        /// <summary>
        /// Récupère toutes les sessions de jeu.
        /// </summary>
        /// <returns>Une liste de toutes les sessions de jeu.</returns>
        public List<GameSession> GetAllSessions()
        {
            return _sessions;
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
        public bool RemoveSession(string sessionId)
        {
            var session = GetSessionById(sessionId);
            if (session != null)
            {
                _sessions.Remove(session);
                return true;
            }
            return false;
        }

        /// <summary>
        /// Met à jour une session existante en fonction de son ID.
        /// </summary>
        /// <param name="updatedSession">La session de jeu mise à jour.</param>
        public void UpdateSession(GameSession updatedSession)
        {
            int index = _sessions.FindIndex(s => s.SessionId == updatedSession.SessionId);

            if (index >= 0)
            {
                _sessions[index] = updatedSession;
            }
            else
            {
                throw new KeyNotFoundException($"Session with ID {updatedSession.SessionId} not found.");
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
            if (session == null || !session.ContainsPlayer(playerId))
            {
                return false;
            }

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
                return true;
            }

            return false;
        }

        /// <summary>
        /// Notifie tous les joueurs d'une session d'un résultat de différence.
        /// </summary>
        /// <param name="sessionId">Identifiant de la session.</param>
        /// <param name="isInZone">Résultat de validation de la différence (true ou false).</param>
        /// <returns>Tâche asynchrone représentant l'opération de notification.</returns>
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
            }
        }
    }
}
