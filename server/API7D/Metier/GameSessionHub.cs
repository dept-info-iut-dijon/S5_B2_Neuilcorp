using Microsoft.AspNetCore.SignalR;
using System.Threading.Tasks;
using API7D.objet;
using API7D.Services;
using System;
using System.Linq;
using System.Collections.Generic;
using API7D.DATA;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;

namespace API7D.Metier
{
    /// <summary>
    /// Hub SignalR pour la gestion des sessions de jeu multijoueur.
    /// </summary>
    public class GameSessionHub : Hub
    {
        private readonly SessionService _sessionService;
        private readonly ILogger<GameSessionHub> _logger;

        /// <summary>
        /// Initialise une nouvelle instance de <see cref="GameSessionHub"/>.
        /// </summary>
        /// <param name="sessionService">Service de gestion des sessions de jeu.</param>
        public GameSessionHub(SessionService sessionService, ILogger<GameSessionHub> logger)
        {
            _sessionService = sessionService;
            _logger = logger;
        }

        public override Task OnConnectedAsync()
        {
            _logger.LogInformation($"Client connecté : {Context.ConnectionId}.");
            return base.OnConnectedAsync();
        }

        /// <summary>
        /// Permet à un client de rejoindre un groupe de session spécifique.
        /// </summary>
        /// <param name="sessionId">L'ID de la session de jeu à rejoindre.</param>
        public async Task JoinSessionGroup(string sessionId)
        {
            _logger.LogInformation($"JoinSessionGroup appelé avec sessionId: {sessionId}, ConnectionId: {Context.ConnectionId}");
            await Groups.AddToGroupAsync(Context.ConnectionId, sessionId);
            _logger.LogInformation($"Client ConnectionId: {Context.ConnectionId} successfully joined session group {sessionId}.");
        }

        /// <summary>
        /// Notifie les clients qu'un joueur a rejoint la session.
        /// </summary>
        /// <param name="sessionId">ID de la session de jeu.</param>
        /// <param name="player">L'objet Player représentant le joueur.</param>
        public async Task NotifyPlayerJoined(string sessionId, Player player)
        {
            _logger.LogInformation($"Notifying clients in session {sessionId} that player {player.Name} has joined.");
            await Clients.Group(sessionId).SendAsync("PlayerJoined", player);
        }

        //marche
        public async Task SetPlayerReadyStatus(string sessionId, string playerId, bool isReady)
        {
            _logger.LogInformation($"Received readiness update for player {playerId} in session {sessionId}. IsReady: {isReady}.");

            GameSession existingSession = _sessionService.GetSessionById(sessionId);
            if (existingSession == null)
            {
                _logger.LogWarning($"Session {sessionId} not found.");
                return;
            }

            Player player = existingSession.Players.FirstOrDefault(p => p.PlayerId == playerId);
            if (player == null)
            {
                _logger.LogWarning($"Player {playerId} not found in session {sessionId}.");
                return;
            }

            player.IsReady = isReady;
            _logger.LogInformation($"Player {playerId} readiness updated to {isReady} in session {sessionId}.");

            await Clients.Group(sessionId).SendAsync("PlayerReadyStatusChanged", playerId, isReady);

            // Vérifie si tous les joueurs sont prêts
            /**if (existingSession.Players.All(p => p.IsReady))
            {
                // Envoi des images une fois que tous les joueurs sont prêts
                var images = _imageService.GetImagePair(existingSession.ImagePairId);
                var players = existingSession.Players.ToList();

                // Distribution alternée des images
                for (int i = 0; i < players.Count; i++)
                {
                    byte[] imageToSend = (i % 2 == 0) ? images.Image1 : images.Image2;
                    await Clients.Client(players[i].PlayerId).SendAsync("ReceiveImage", imageToSend);
                }
            }*/
    }

        public async Task RequestSync(string sessionId)
        {
            _logger.LogInformation($"Sync request received for session {sessionId}.");
            GameSession existingSession = _sessionService.GetSessionById(sessionId);
            if (existingSession != null)
            {
                await Clients.Caller.SendAsync("SyncSessionState", existingSession);
                _logger.LogInformation($"Sync state sent to client {Context.ConnectionId} for session {sessionId}.");
            }
        }

        /// <summary>
        /// Notifie les clients lorsqu'une session est supprimée.
        /// </summary>
        /// <param name="sessionId">ID de la session supprimée.</param>
        public async Task NotifySessionDeleted(string sessionId)
        {
            _logger.LogInformation($"Notifying clients that session {sessionId} was deleted.");
            await Clients.Group(sessionId).SendAsync("SessionDeleted", sessionId);
        }

        /// <summary>
        /// Notifie les clients lorsqu'un joueur est supprimé.
        /// </summary>
        /// <param name="sessionId">ID de la session.</param>
        /// <param name="playerId">ID du joueur supprimé.</param>
        public async Task NotifyPlayerRemoved(string sessionId, string playerId)
        {
            _logger.LogInformation($"Notifying clients in session {sessionId} that player {playerId} was removed.");
            await Clients.Group(sessionId).SendAsync("PlayerRemoved", playerId);
        }

    }
}