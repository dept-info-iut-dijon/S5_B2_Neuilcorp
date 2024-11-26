using Microsoft.AspNetCore.SignalR;
using System.Threading.Tasks;
using API7D.objet;
using API7D.Services;
using System;
using System.Linq;
using System.Collections.Generic;
using API7D.DATA;
using Microsoft.AspNetCore.Mvc;

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
            Console.WriteLine($"Connexion {Context.ConnectionId} ajoutée au groupe {sessionId}.");
        }

        /// <summary>
        /// Notifie tous les clients d'une session qu'un joueur a rejoint la session.
        /// Ajoute également le joueur à la session côté serveur.
        /// </summary>
        /// <param name="sessionId">L'ID de la session de jeu.</param>
        /// <param name="playerName">Le nom du joueur qui a rejoint.</param>
        public async Task PlayerJoined(string sessionId, string playerName)
        {
            var session = _sessionService.GetSessionById(sessionId);
            if (session == null)
            {
                Console.WriteLine($"Session {sessionId} non trouvée.");
                return;
            }

            var existingPlayer = session.Players.FirstOrDefault(p => p.Name == playerName);
            if (existingPlayer != null)
            {
                Console.WriteLine($"Joueur {playerName} existe déjà dans la session {sessionId}.");
                return;
            }

            // Ajouter le joueur à la session
            session.Players.Add(new Player
            {
                Name = playerName,
                IsReady = false,
                PlayerId = Context.ConnectionId
            });
            _sessionService.UpdateSession(session);

            Console.WriteLine($"Joueur {playerName} ajouté à la session {sessionId}. Total joueurs : {session.Players.Count}");

            // Diffuser l'événement à tous les clients
            await Clients.Group(sessionId).SendAsync("PlayerJoined", playerName);
            Console.WriteLine($"Diffusion de l'événement PlayerJoined pour le joueur {playerName} à la session {sessionId}");
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