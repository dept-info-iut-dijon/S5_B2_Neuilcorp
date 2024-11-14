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
            bool toggleImage = true;  // Variable pour alterner les images

            foreach (var player in session.Players)
            {
                var imageToSend = toggleImage ? image1 : image2;
                toggleImage = !toggleImage;

                // Envoi de l'image spécifique à chaque joueur
                await Clients.Client(player.PlayerId).SendAsync("ReceiveImage", imageToSend);
            }
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
