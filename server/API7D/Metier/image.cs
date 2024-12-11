using API7D.DATA;
using API7D.objet;
using API7D.Services;
using Microsoft.AspNetCore.SignalR;
using System.Data.SqlTypes;

namespace API7D.Metier
{
    /// <summary>
    /// Classe qui implémente l'interface IImage pour la gestion des images.
    /// </summary>
    public class Image : IImage
    {
        private IImageDATA _data = new ImageDATA()!;

        /// <summary>
        /// Récupère une image par son ID.
        /// </summary>
        /// <param name="ID">L'ID de l'image à récupérer.</param>
        /// <returns>Un tableau de bytes représentant l'image.</returns>
        /// <exception cref="FileNotFoundException">Si le fichier image est introuvable.</exception>
        /// <exception cref="IOException">Si une erreur survient lors de la lecture du fichier.</exception>
        public byte[] GetImages(int ID)
        {
            string path = _data.GetImagesDATA(ID);
            return File.ReadAllBytes(path);
        }

        /// <summary>
        /// Récupère toutes les images.
        /// </summary>
        /// <returns>Une liste de tableaux de bytes représentant toutes les images.</returns>
        /// <exception cref="IOException">Si une erreur survient lors de la lecture des fichiers.</exception>
        public List<byte[]> GetAllImages()
        {
            List<string> paths = _data.GetAllImagesDATA();
            List<byte[]> images = new List<byte[]>();

            foreach (string path in paths)
            {
                images.Add(File.ReadAllBytes(path));
            }

            return images;
        }

        /// <summary>
        /// Méthode non implémentée.
        /// </summary>
        /// <param name="image">L'objet ImageDifference représentant les images.</param>
        /// <exception cref="NotImplementedException">Cette méthode n'est pas encore implémentée.</exception>
        public void SetImages(ImageDifference image)
        {
            throw new NotImplementedException();
        }

        /// <summary>
        /// Récupère une paire d'images à partir de l'ID de la paire.
        /// </summary>
        /// <param name="pairId">L'ID de la paire d'images.</param>
        /// <returns>Un tuple contenant deux tableaux de bytes représentant les images de la paire.</returns>
        /// <exception cref="InvalidOperationException">Si la paire d'images est incomplète ou introuvable.</exception>
        public (byte[] Image1, byte[] Image2) GetImagePair(int pairId)
        {
            var imagePaths = _data.GetAllImagesWithPairData()
                                  .Where(img => img.ImagePairId == pairId)
                                  .Select(img => img.ImageLink)
                                  .ToList();

            if (imagePaths.Count != 2)
            {
                throw new InvalidOperationException($"La paire d'images avec l'ID {pairId} est incomplète ou introuvable.");
            }

            byte[] image1 = File.ReadAllBytes(imagePaths[0]);
            byte[] image2 = File.ReadAllBytes(imagePaths[1]);

            return (image1, image2);
        }

        /// <summary>
        /// Récupère toutes les images avec leurs paires associées.
        /// </summary>
        /// <returns>Une liste d'objets ImageWithPair contenant les paires d'images et leurs ID.</returns>
        /// <exception cref="IOException">Si une erreur survient lors de la lecture des fichiers.</exception>
        public List<ImageWithPair> GetAllImagesWithPairs()
        {
            var imageData = _data.GetAllImagesWithPairData();
            var imageWithPairs = new List<ImageWithPair>();

            foreach (var (imageId, imagePairId, imageLink) in imageData)
            {
                var base64String = Convert.ToBase64String(File.ReadAllBytes(imageLink));
                imageWithPairs.Add(new ImageWithPair
                {
                    ImageId = imageId,
                    ImagePairId = imagePairId,
                    Base64Image = base64String
                });
            }

            return imageWithPairs;
        }

        /// <summary>
        /// Récupère les images prêtes pour le joueur pour une session donnée.
        /// </summary>
        /// <param name="Idsession">L'ID de la session de jeu.</param>
        /// <param name="_session">Le service de gestion de session.</param>
        /// <returns>Un tuple contenant les deux images à envoyer au joueur.</returns>
        /// <exception cref="ArgumentException">Si l'ID de la session est invalide.</exception>
        /// <exception cref="InvalidOperationException">Si l'hôte n'a pas choisi de paire d'images.</exception>
        public (byte[] Image1, byte[] Image2) ReadyImageToPlayer(string Idsession, SessionService _session)
        {
            var session = _session.GetSessionById(Idsession);
            if (session == null)
            {
                throw new ArgumentException("Session invalide.", nameof(Idsession));
            }

            if (session.ImagePairId == 0)
            {
                throw new InvalidOperationException("L'hôte n'a pas choisi de paire d'images.");
            }

            var imagePairId = session.ImagePairId;
            return GetImagePair(imagePairId);
        }
    }
}
