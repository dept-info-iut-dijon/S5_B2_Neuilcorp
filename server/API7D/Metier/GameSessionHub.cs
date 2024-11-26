using Microsoft.AspNetCore.SignalR;
using System.Threading.Tasks;
using API7D.objet;
using API7D.Services;
using System;
using System.Linq;
using System.Collections.Generic;
using API7D.DATA;

namespace API7D.Metier
{
    /// <summary>
    /// Hub SignalR pour la gestion des sessions de jeu multijoueur.
    /// </summary>
    public class GameSessionHub : Hub
    {
        private readonly SessionService _sessionService;
        private readonly ImageDATA _imageService;
        private static readonly Dictionary<string, (byte[] Image1, byte[] Image2)> ImagePairs = new();

        /// <summary>
        /// Initialise une nouvelle instance de <see cref="GameSessionHub"/>.
        /// </summary>
        public GameSessionHub(SessionService sessionService, ImageDATA imageService)
        {
            _sessionService = sessionService;
            _imageService = imageService;
        }

        /// <summary>
        /// Permet à un client de rejoindre un groupe de session spécifique.
        /// </summary>
        /// <param name="sessionId">L'ID de la session de jeu à rejoindre.</param>
        public async Task JoinSessionGroup(string sessionId)
        {
            await Groups.AddToGroupAsync(Context.ConnectionId, sessionId);
            Console.WriteLine($"Client {Context.ConnectionId} ajouté au groupe {sessionId}");
        }

        /// <summary>
        /// Ajoute un joueur à une session et notifie les clients.
        /// </summary>
        /// <param name="sessionId">L'ID de la session.</param>
        /// <param name="playerName">Le nom du joueur qui rejoint.</param>
        public async Task PlayerJoined(string sessionId, string playerName)
        {
            GameSession session = _sessionService.GetSessionById(sessionId);
            if (session != null)
            {
                // Ajoute le joueur s'il n'existe pas encore dans la session
                if (!session.Players.Any(p => p.Name == playerName))
                {
                    session.Players.Add(new Player
                    {
                        PlayerId = Context.ConnectionId, // Utilise ConnectionId comme identifiant
                        Name = playerName,
                        IsReady = false // Par défaut : pas prêt
                    });
                }

                await BroadcastPlayerList(sessionId);
            }
            else
            {
                Console.WriteLine($"Session {sessionId} non trouvée.");
            }
        }

        /// <summary>
        /// Définit le statut de préparation d'un joueur, notifie tous les clients du groupe,
        /// et distribue des images parmi une paire sélectionnée lorsque tous les joueurs sont prêts.
        /// Si un seul joueur est présent dans la session, envoie un message d'alerte et remet le statut à "pas prêt".
        /// </summary>
        /// <param name="sessionId">L'ID de la session de jeu.</param>
        /// <param name="playerId">L'ID du joueur dont le statut est mis à jour.</param>
        /// <param name="isReady">Le nouveau statut de préparation du joueur.</param>
        /// <returns>Une tâche asynchrone représentant l'opération en cours.</returns>
        public async Task SetPlayerReadyStatus(string sessionId, string playerId, bool isReady)
        {
            GameSession existingSession = _sessionService.GetSessionById(sessionId);
            if (existingSession == null) return;

            Player player = existingSession.Players.FirstOrDefault(p => p.PlayerId == playerId);
            if (player == null) return;

            if (existingSession.Players.Count == 1 && isReady)
            {
                await Clients.Client(player.PlayerId).SendAsync("Alert", "Vous ne pouvez pas lancer la partie seul.");
                player.IsReady = false;

                await Clients.Group(sessionId).SendAsync("PlayerReadyStatusChanged", playerId, false);
                return;
            }

            player.IsReady = isReady;

            await Clients.Group(sessionId).SendAsync("PlayerReadyStatusChanged", playerId, isReady);

            // On vérifie si tous les joueurs sont prêts
            if (existingSession.Players.All(p => p.IsReady))
            {
                // Vérifie si une paire d'images a été sélectionnée
                if (!ImagePairs.ContainsKey(sessionId))
                {
                    Console.WriteLine("Erreur : aucune paire d'images sélectionnée pour la session.");
                    await Clients.Group(sessionId).SendAsync("Error", "Aucune paire d'images n'a été sélectionnée pour la session.");
                    return;
                }

                var (image1, image2) = ImagePairs[sessionId];

                var players = existingSession.Players.ToList();
                var shuffledPlayers = players.OrderBy(_ => Guid.NewGuid()).ToList(); // On mélange les joueurs pour éviter un pattern prévisible
                int half = players.Count / 2;

                for (int i = 0; i < players.Count; i++)
                {
                    byte[] imageToSend = (i < half || players.Count % 2 != 0 && i == players.Count - 1) ? image1 : image2;
                    await Clients.Client(shuffledPlayers[i].PlayerId).SendAsync("ReceiveImage", imageToSend);
                }

                Console.WriteLine("Les images ont été distribuées à tous les joueurs.");
            }
        }

        /// <summary>
        /// Diffuse la liste complète des joueurs d'une session à tous les clients.
        /// </summary>
        /// <param name="sessionId">L'ID de la session.</param>
        public async Task BroadcastPlayerList(string sessionId)
        {
            GameSession session = _sessionService.GetSessionById(sessionId);
            if (session != null)
            {
                await Clients.Group(sessionId).SendAsync("PlayerListUpdated", session.Players);
                Console.WriteLine($"Liste des joueurs de la session {sessionId} diffusée.");
            }
            else
            {
                Console.WriteLine($"Session {sessionId} non trouvée pour diffusion de la liste des joueurs.");
            }
        }

        /// <summary>
        /// Permet à l'hôte de choisir une paire d'images pour la session.
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
            bool toggleImage = true; // Variable pour alterner les images

            foreach (var player in session.Players)
            {
                var imageToSend = toggleImage ? image1 : image2;
                toggleImage = !toggleImage;

                // Envoi de l'image spécifique à chaque joueur
                await Clients.Client(player.PlayerId).SendAsync("ReceiveImage", imageToSend);
            }
        }

        /// <summary>
        /// Sélectionne une paire d'images pour la session et notifie les clients.
        /// </summary>
        public async Task SelectImagePair(string sessionId, int imagePairId)
        {
            GameSession existingSession = _sessionService.GetSessionById(sessionId);
            if (existingSession == null)
            {
                await Clients.Caller.SendAsync("Error", "Session not found.");
                return;
            }

            // L'hôte sélectionne la paire d'images pour la session
            existingSession.ImagePairId = imagePairId;

            await Clients.Group(sessionId).SendAsync("ImagePairSelected", imagePairId);
        }

        /// <summary>
        /// Vérifie si tous les joueurs ont sélectionné la même différence et valide ou invalide la tentative.
        /// </summary>
        public async Task VerifyPlayerSelection(string sessionId, string playerId, Coordonnees selection)
        {
            // a faire apres
            /*GameSession session = _sessionService.GetSessionById(sessionId);
            if (session == null)
                return;

            var currentSelection = selection;
            var allPlayersSelectedSame = session.Players.All(p => p.CurrentSelection.Equals(currentSelection));

            if (allPlayersSelectedSame)
            {
                await Clients.Group(sessionId).SendAsync("SelectionValidated", currentSelection);
                // Ajouter logique pour passer à la différence suivante, si nécessaire.
            }
            else
            {
                await Clients.Group(sessionId).SendAsync("SelectionFailed", currentSelection);
            }*/
        }
    }
}