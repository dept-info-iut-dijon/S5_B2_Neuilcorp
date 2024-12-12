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
using static System.Net.Mime.MediaTypeNames;

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
        private static readonly Dictionary<string, string> PlayerConnections = new Dictionary<string, string>();

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


        /// <summary>
        /// Méthode appelée lorsqu'un client se connecte.
        /// </summary>
        /// <returns>Tâche représentant l'opération asynchrone</returns>
        public override Task OnConnectedAsync()
        {
            _logger.LogInformation($"Client connecté : {Context.ConnectionId}.");
            return base.OnConnectedAsync();
        }

        /// <summary>
        /// Methode appelée lorsqu'un client se déconnecte.
        /// </summary>
        /// <param name="exception">Exception éventuellement générée lors de la déconnection</param>
        /// <returns>Tâche représentant l'opération asynchrone</returns>
        public override Task OnDisconnectedAsync(Exception exception)
        {
            var playerId = PlayerConnections.FirstOrDefault(pair => pair.Value == Context.ConnectionId).Key;
            if (playerId != null)
            {
                PlayerConnections.Remove(playerId);
                _logger.LogInformation($"Player {playerId} déconnecté et supprimé du dictionnaire.");
            }

            return base.OnDisconnectedAsync(exception);
        }

        /// <summary>
        /// Enregistre un joueur dans le dictionnaire des connexions.
        /// </summary>
        /// <param name="playerId">Id du joueur à enregistrer</param>
        /// <returns>Tâche représentant l'opération asynchrone</returns>
        public async Task RegisterPlayer(string playerId)
        {
            if (PlayerConnections.ContainsKey(playerId))
            {
                // Mettre à jour si l'association existe déjà
                PlayerConnections[playerId] = Context.ConnectionId;
                _logger.LogInformation($"Mise à jour de la connexion pour le joueur {playerId} avec {Context.ConnectionId}.");

            }
            else
            {
                // Ajouter le nouvel enregistrement
                PlayerConnections.Add(playerId, Context.ConnectionId);
                _logger.LogInformation($"Enregistrement du joueur {playerId} avec {Context.ConnectionId}.");
            }
            await Clients.Caller.SendAsync("ReceiveConnectionId", Context.ConnectionId);
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
        /// Notifie les clients lorsqu'une session est supprimée.
        /// </summary>
        /// <param name="sessionId">ID de la session supprimée.</param>
        public async Task SessionDeleted(string sessionId)
        {
            _logger.LogInformation($"Notifying clients that session {sessionId} was deleted.");
            await Clients.Group(sessionId).SendAsync("SessionDeleted", sessionId);
        }

        public async Task<string> GetConnectionIdByPlayerId(string playerId)
        {
            if (PlayerConnections.ContainsKey(playerId))
            {
                string connectionId = PlayerConnections[playerId];
                _logger.LogInformation($"ConnectionId pour Player {playerId} : {connectionId}");
                return connectionId;
            }
            else
            {
                _logger.LogWarning($"Aucune connexion trouvée pour Player {playerId}.");
                return null;
            }
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

        /// <summary>
        /// Notifie les clients lorsqu'un joueur est supprimé.
        /// </summary>
        /// <param name="sessionId">ID de la session.</param>
        /// <param name="playerId">ID du joueur supprimé.</param>
        public async Task NotifyPlayerRemoved(string sessionId, Player player)
        {
            _logger.LogInformation($"Notifying clients in session {sessionId} that player {player.Name} was removed.");
            await Clients.Group(sessionId).SendAsync("PlayerRemoved", player);
        }

        /// <summary>
        /// Mets à jour l'état de préparation d'un joueur dans une session.
        /// Valide qu'une session ne peut démarrer que si au moins deux joueurs sont prêts.
        /// Notifie les joueurs de l'état de préparation mis à jour.
        /// </summary>
        /// <param name="sessionId">ID de session</param>
        /// <param name="playerId">ID des joueurs</param>
        /// <param name="isReady">savoir si un joueur est prêt ou non</param>
        /// <returns></returns>

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
            Player thisplayer = existingSession.Players.FirstOrDefault(p => p.PlayerId == playerId);
            if (thisplayer == null)
            {
                _logger.LogWarning($"Player {playerId} not found in session {sessionId}.");
                return;
            }

            // Si un seul joueur dans la session on le force à ne pas etre pret
            if (existingSession.Players.Count < 2)
            {
                _logger.LogInformation($"Player {playerId} cannot be ready alone in session {sessionId}.");
                await Clients.Caller.SendAsync("ReadyNotAllowed", "Vous ne pouvez pas être prêt en étant seul.");
                thisplayer.IsReady = false;
                isReady = false;
            }
            else { 
            thisplayer.IsReady = isReady;

            }
            _logger.LogInformation($"Player {playerId} readiness updated to {isReady} in session {sessionId}.");
            await Clients.Group(sessionId).SendAsync("PlayerReadyStatusChanged", playerId, isReady);

            if (existingSession.Players.All(p => p.IsReady) && existingSession.ImagePairId!=0)
            {
                _logger.LogInformation($"All players in session {sessionId} are ready. Requesting ImageController to send images...");

                //var imageController = new ImageControlleur(_hubContext, _sessionService, _imageService, _logger);
                var image = _imageService.ReadyImageToPlayer(sessionId, _sessionService);

                var GameSession = _sessionService.GetSessionById(sessionId);

                var melangePlayers = GameSession.Players.OrderBy(_ => Guid.NewGuid()).ToList();

                int imageSwitch = 0;
                foreach (Player player in melangePlayers)
                {
                    var connectionId = await GetConnectionIdByPlayerId(player.PlayerId);
                    var imageToSend = (imageSwitch % 2 == 0) ? image.Image1 : image.Image2;
                    imageSwitch++;

                    if (connectionId != null)
                    {
                        var imageDataBase64 = Convert.ToBase64String(imageToSend);
                        await _hubContext.Clients.Client(connectionId).SendAsync("GameStarted", imageDataBase64 ,GameSession.ImagePairId);
                        _logger.LogInformation($"image envoyer a : {player.PlayerId} avec {connectionId}.");
                        _logger.LogInformation($"taille de l'image : {imageToSend.Length}.");
                    }
                    else
                    {
                        _logger.LogWarning($"Failed to send image to player {connectionId}");
                    }
                }
            }
            else if (existingSession.Players.All(p => p.IsReady) && existingSession.ImagePairId == 0)
            {
                await Clients.Group(sessionId).SendAsync("ReadyNotAllowed", "La partie ne peut pas commencer, aucune image n'est choisie pour la partie");
                thisplayer.IsReady = false;
                isReady = false;
            }
        }

        /// <summary>
        /// Fournit l'état actuel de la session 
        /// </summary>
        /// <param name="sessionId"></param>
        /// <returns></returns>
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

        public async Task NotifyEnd(string sessionId)
        {
            if (string.IsNullOrEmpty(sessionId))
            {
                _logger.LogError("SessionId ne peut pas être null ou vide.");
                throw new ArgumentException("SessionId est requis.", nameof(sessionId));
            }

            try
            {
                // Notifie tous les clients appartenant au groupe SignalR correspondant à la session
                await _hubContext.Clients.Group(sessionId).SendAsync("GameEnded");
                _logger.LogInformation($"Tous les joueurs de la session {sessionId} ont été notifiés de la fin du jeu.");
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, $"Erreur lors de la notification des joueurs de la session {sessionId}.");
                throw;
            }
        }


    }
}