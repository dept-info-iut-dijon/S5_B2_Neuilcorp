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
        private static readonly Dictionary<string, (byte[] Image1, byte[] Image2)> ImagePairs = new();
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
        /// Méthode appelée lorsqu'un joueur rejoint une session.
        /// </summary>
        /// <param name="sessionId">ID de la session de jeu.</param>
        /// <param name="playerName">Nom du joueur.</param>
        public async Task PlayerJoined(string sessionId, string playerName)
        {
            string playerId = PlayerIdGenerator.GeneratePlayerId();
            Player newPlayer = new Player(playerId, playerName);
            _logger.LogDebug($"Player {playerName} obtained id in session {playerId}.");
            GameSession session = _sessionService.GetSessionById(sessionId);
            if (session == null)
            {
                await Clients.Caller.SendAsync("Error", "Session not found.");
                return;
            }
            session.Players.Add(newPlayer);

            _logger.LogInformation($"Player {playerName} is joining session {sessionId}.");
            await Clients.Group(sessionId).SendAsync("PlayerJoined", playerName);
            _logger.LogDebug($"Event 'PlayerJoined' sent for player {playerName} in session {sessionId}.");
        }

        /// <summary>
        /// Définit le statut de préparation d'un joueur et notifie les clients.
        /// </summary>
        public void ChooseImagePair(string sessionId, int imagePaireId)
        {
            var imagePair = _imageService.GetImagePair(imagePaireId);
            ImagePairs[sessionId] = imagePair;
        }

        /// <summary>
        /// Vérifie si tous les joueurs sont prêts, puis envoie une image aléatoire de la paire à chaque joueur.
        /// </summary>
        public async Task SendImagesToPlayersIfAllReady(string sessionId)
        {
            GameSession session = _sessionService.GetSessionById(sessionId);

            if (session == null || !session.Players.All(p => p.IsReady))
                return;

            if (!ImagePairs.ContainsKey(sessionId))
            {
                Console.WriteLine("Erreur : La paire d'images n'a pas été sélectionnée.");
                return;
            }

            var (image1, image2) = ImagePairs[sessionId];
            bool toggleImage = true;  // Variable pour alterner les images

            foreach (var player in session.Players)
            {
                var imageToSend = toggleImage ? image1 : image2;
                toggleImage = !toggleImage;

                // Envoi de l'image spécifique à chaque joueur
                await Clients.Client(player.PlayerId).SendAsync("ReceiveImage", imageToSend);
            }
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

        public async Task SelectImagePair(string sessionId, int imagePairId)
        {
            GameSession existingSession = _sessionService.GetSessionById(sessionId);
            if (existingSession == null)
            {
                Console.WriteLine($"Session {sessionId} non trouvée.");
                return;
            }

            Player player = existingSession.Players.FirstOrDefault(p => p.PlayerId == playerId);
            if (player == null)
            {
                Console.WriteLine($"Joueur {playerId} non trouvé dans la session.");
                return;
            }

            player.IsReady = isReady;
            Console.WriteLine($"Statut de préparation du joueur {player.Name} mis à jour à {(isReady ? "prêt" : "pas prêt")}");

            await Clients.Group(sessionId).SendAsync("PlayerReadyStatusChanged", playerId, isReady);
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