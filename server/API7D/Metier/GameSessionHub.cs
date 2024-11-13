using Microsoft.AspNetCore.SignalR;
using System.Threading.Tasks;
using API7D.objet;
using API7D.Services;
using System;

namespace API7D.Metier
{
    /// <summary>
    /// Hub SignalR pour la gestion des sessions de jeu multijoueur.
    /// </summary>
    public class GameSessionHub : Hub
    {
        private readonly SessionService _sessionService;

        /// <summary>
        /// Initialise une nouvelle instance de <see cref="GameSessionHub"/>.
        /// </summary>
        /// <param name="sessionService">Service de gestion des sessions de jeu.</param>
        public GameSessionHub(SessionService sessionService)
        {
            _sessionService = sessionService;
        }

        /// <summary>
        /// Permet à un client de rejoindre un groupe de session spécifique.
        /// </summary>
        /// <param name="sessionId">L'ID de la session de jeu à rejoindre.</param>
        public async Task JoinSessionGroup(string sessionId)
        {
            await Groups.AddToGroupAsync(Context.ConnectionId, sessionId);
        }

        /// <summary>
        /// Notifie tous les clients d'une session que le joueur a rejoint la session.
        /// </summary>
        /// <param name="sessionId">L'ID de la session de jeu.</param>
        /// <param name="playerName">Le nom du joueur qui a rejoint.</param>
        public async Task PlayerJoined(string sessionId, string playerName)
        {
            await Clients.Group(sessionId).SendAsync("PlayerJoined", playerName);
        }

        /// <summary>
        /// Définit le statut de préparation d'un joueur et notifie les clients.
        /// </summary>
        /// <param name="sessionId">L'ID de la session de jeu.</param>
        /// <param name="playerId">L'ID du joueur dont le statut est mis à jour.</param>
        /// <param name="isReady">Le statut de préparation.</param>
        public async Task SetPlayerReadyStatus(string sessionId, string playerId, bool isReady)
        {
            Console.WriteLine($"Requête reçue : Changer statut de préparation pour session {sessionId}, joueur {playerId} à {isReady}");

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
    }
}
