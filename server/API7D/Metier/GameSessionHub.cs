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
using API7D.Controllers;

namespace API7D.Metier
{
    /// <summary>
    /// Hub SignalR pour la gestion des sessions de jeu multijoueur.
    /// </summary>
    public class GameSessionHub : Hub
    {
        private readonly SessionService _sessionService;
        private readonly ILogger<GameSessionHub> _logger;
        private readonly IImage _imageService;
        private readonly IHubContext<GameSessionHub> _hubContext;

        /// <summary>
        /// Initialise une nouvelle instance de <see cref="GameSessionHub"/>.
        /// </summary>
        /// <param name="sessionService">Service de gestion des sessions de jeu.</param>
        public GameSessionHub(SessionService sessionService, ILogger<GameSessionHub> logger, IImage imageService, IHubContext<GameSessionHub> hubContext)
        {
            _sessionService = sessionService;
            _logger = logger;
            _imageService = imageService;
            _hubContext = hubContext;
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

            // Récupération de la session
            GameSession existingSession = _sessionService.GetSessionById(sessionId);
            if (existingSession == null)
            {
                _logger.LogWarning($"Session {sessionId} not found.");
                return;
            }

            // Récupération du joueur dans la session
            Player player = existingSession.Players.FirstOrDefault(p => p.PlayerId == playerId);
            if (player == null)
            {
                _logger.LogWarning($"Player {playerId} not found in session {sessionId}.");
                return;
            }

            // Si un seul joueur dans la session on le force à ne pas etre pret
            if (existingSession.Players.Count < 2)
            {
                _logger.LogInformation($"Player {playerId} cannot be ready alone in session {sessionId}.");
                await Clients.Caller.SendAsync("ReadyNotAllowed", "Vous ne pouvez pas être prêt en étant seul.");
                player.IsReady = false;
                isReady = false;
            }
            else { 
            player.IsReady = isReady;

            }
            _logger.LogInformation($"Player {playerId} readiness updated to {isReady} in session {sessionId}.");
            await Clients.Group(sessionId).SendAsync("PlayerReadyStatusChanged", playerId, isReady);

            if (existingSession.Players.All(p => p.IsReady))
            {
                _logger.LogInformation($"All players in session {sessionId} are ready. Requesting ImageController to send images...");

                var imageController = new ImageControlleur(_hubContext, _sessionService, _imageService);
                var result = await imageController.SendImagesToPlayers(sessionId);

                if (result is OkObjectResult)
                {
                    // Notifie le groupe que la partie commence après l'envoi des images
                    await Clients.Group(sessionId).SendAsync("GameStarted");
                }
                else
                {
                    _logger.LogWarning($"Failed to send images for session {sessionId}: {result}");
                }
            }
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
    }
}