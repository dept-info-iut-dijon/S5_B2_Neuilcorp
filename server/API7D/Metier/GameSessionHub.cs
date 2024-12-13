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
        /// <param name="sessionId">ID de session.</param>
        /// <param name="playerId">ID du joueur.</param>
        /// <param name="isReady">Statut "prêt" du joueur.</param>
        public async Task SetPlayerReadyStatus(string sessionId, string playerId, bool isReady)
        {
            _logger.LogInformation($"Received readiness update for player {playerId} in session {sessionId}. IsReady: {isReady}.");

            // Vérifie et récupère la session
            var existingSession = ValidateSession(sessionId);
            if (existingSession == null) return;

            // Vérifie et récupère le joueur
            var player = ValidatePlayer(existingSession, playerId);
            if (player == null) return;

            // Gestion de la logique de statut "prêt"
            HandlePlayerReadiness(existingSession, player, isReady);

            // Notifie les autres joueurs de la mise à jour
            await NotifyPlayerReadinessUpdate(sessionId, playerId, isReady);

            // Si tous les joueurs sont prêts, vérifie si le jeu peut démarrer
            if (CanStartGame(existingSession))
            {
                await StartGame(sessionId, existingSession);
            }
            else if (existingSession.Players.All(p => p.IsReady) && existingSession.ImagePairId == 0)
            {
                await NotifyMissingImage(sessionId, player);
            }
        }

        /// <summary>
        /// Valide l'existence d'une session.
        /// </summary>
        /// <param name="sessionId">ID de la session à valider.</param>
        /// <returns>La session correspondante ou null si elle n'existe pas.</returns>
        private GameSession ValidateSession(string sessionId)
        {
            var session = _sessionService.GetSessionById(sessionId);
            if (session == null)
            {
                _logger.LogWarning($"Session {sessionId} not found.");
            }
            return session;
        }

        /// <summary>
        /// Valide l'existence d'un joueur dans une session donnée.
        /// </summary>
        /// <param name="session">La session dans laquelle rechercher le joueur.</param>
        /// <param name="playerId">ID du joueur à valider.</param>
        /// <returns>Le joueur correspondant ou null s'il n'existe pas.</returns>
        private Player ValidatePlayer(GameSession session, string playerId)
        {
            var player = session.Players.FirstOrDefault(p => p.PlayerId == playerId);
            if (player == null)
            {
                _logger.LogWarning($"Player {playerId} not found in session {session.SessionId}.");
            }
            return player;
        }

        /// <summary>
        /// Gère la mise à jour du statut "prêt" d'un joueur.
        /// </summary>
        /// <param name="session">La session dans laquelle le joueur est présent.</param>
        /// <param name="player">Le joueur dont le statut est mis à jour.</param>
        /// <param name="isReady">Nouveau statut "prêt" du joueur.</param>
        private void HandlePlayerReadiness(GameSession session, Player player, bool isReady)
        {
            if (session.Players.Count < 2)
            {
                _logger.LogInformation($"Player {player.PlayerId} cannot be ready alone in session {session.SessionId}.");
                player.IsReady = false;
            }
            else
            {
                player.IsReady = isReady;
            }
        }

        /// <summary>
        /// Notifie tous les joueurs d'une session de la mise à jour du statut "prêt" d'un joueur.
        /// </summary>
        /// <param name="sessionId">ID de session.</param>
        /// <param name="playerId">ID du joueur.</param>
        /// <param name="isReady">Statut "prêt" mis à jour.</param>
        private async Task NotifyPlayerReadinessUpdate(string sessionId, string playerId, bool isReady)
        {
            await Clients.Group(sessionId).SendAsync("PlayerReadyStatusChanged", playerId, isReady);
            _logger.LogInformation($"Player {playerId} readiness updated to {isReady} in session {sessionId}.");
        }

        /// <summary>
        /// Vérifie si une session peut commencer.
        /// </summary>
        /// <param name="session">La session à vérifier.</param>
        /// <returns>True si la session peut démarrer, sinon False.</returns>
        private bool CanStartGame(GameSession session)
        {
            return session.Players.All(p => p.IsReady) && session.ImagePairId != 0;
        }

        /// <summary>
        /// Notifie les joueurs qu'aucune image n'est définie pour la session.
        /// </summary>
        /// <param name="sessionId">ID de session.</param>
        /// <param name="player">Le joueur à notifier.</param>
        private async Task NotifyMissingImage(string sessionId, Player player)
        {
            await Clients.Group(sessionId).SendAsync("ReadyNotAllowed", "La partie ne peut pas commencer, aucune image n'est choisie pour la partie");
            player.IsReady = false;
        }

        /// <summary>
        /// Démarre le jeu pour une session donnée en envoyant les images et la durée du timer à tous les joueurs.
        /// </summary>
        /// <param name="sessionId">ID de session.</param>
        /// <param name="session">La session à démarrer.</param>
        private async Task StartGame(string sessionId, GameSession session)
        {
            _logger.LogInformation($"All players in session {sessionId} are ready. Starting game...");

            var image = _imageService.ReadyImageToPlayer(sessionId, _sessionService);
            var melangePlayers = session.Players.OrderBy(_ => Guid.NewGuid()).ToList();

            int imageSwitch = 0;
            foreach (var player in melangePlayers)
            {
                var connectionId = await GetConnectionIdByPlayerId(player.PlayerId);
                if (!string.IsNullOrEmpty(connectionId))
                {
                    var imageToSend = (imageSwitch % 2 == 0) ? image.Image1 : image.Image2;
                    imageSwitch++;

                    var imageDataBase64 = Convert.ToBase64String(imageToSend);
                    DifferanceChecker lancementTimer = new DifferanceChecker();
                    await Clients.Client(connectionId).SendAsync("GameStarted", imageDataBase64, session.ImagePairId,session.TimerDuration);
                    if (session.TimerDuration > 0) {
                        lancementTimer.StartOrResetTimer(session);
                    }
                    _logger.LogInformation($"Image envoyée à {player.PlayerId} avec {session.TimerDuration}");
                }
                else
                {
                    _logger.LogWarning($"Failed to send image to player {player.PlayerId} (connection ID missing).");
                }
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
                // Récupère la session correspondante à partir d'une source (par exemple, un service ou un repository)
                GameSession session = _sessionService.GetSessionById(sessionId);

                if (session == null)
                {
                    _logger.LogError($"Session avec l'ID {sessionId} introuvable.");
                    throw new InvalidOperationException($"La session {sessionId} n'existe pas.");
                }

                // Notifie tous les clients appartenant au groupe SignalR correspondant à la session
                await _hubContext.Clients.Group(sessionId).SendAsync("GameEnded", session.Attempts , session.MissedAttempts);

                _logger.LogInformation($"Tous les joueurs de la session {sessionId} ont été notifiés de la fin du jeu.");
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, $"Erreur lors de la notification des joueurs de la session {sessionId}.");
                throw;
            }
        }


        public async Task NotifyTimerStart(string sessionId, int timerDuration)
        {
            _logger.LogInformation($"Notifiant le début du Timer ({timerDuration} secondes) pour la session {sessionId}.");
            await Clients.Group(sessionId).SendAsync("TimerStarted", timerDuration);
        }

        public async Task NotifyTimerExpired(string sessionId)
        {
            await Clients.Group(sessionId).SendAsync("TimerExpired");
        }
    }
}