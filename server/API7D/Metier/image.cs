using API7D.DATA;
using API7D.objet;
using API7D.Services;
using Microsoft.AspNetCore.SignalR;
using System.Data.SqlTypes;

namespace API7D.Metier
{

    /// <summary>
    /// Classe qui implémente l'interface IImage pour la gestion des images
    /// </summary>
    public class image : IImage
    {
        private IImageDATA _data = new ImageDATA()!;

        /// <summary>
        /// Récupère une image par son ID.
        /// </summary>
        /// <param name="ID"></param>
        /// <returns></returns>
        /// <exception cref="Exception"></exception>
        public byte[] GetImages(int ID)
        {
            try
            {
                string path = _data.GetImagesDATA(ID);
                byte[] imageBytes = System.IO.File.ReadAllBytes(path);
                return imageBytes;
            }
            catch (Exception ex)
            {
                throw new Exception(ex.ToString());
            }
        }

        /// <summary>
        /// Récupère toutes les images
        /// </summary>
        /// <returns>Une liste de tableaux de bytes représentant toutes les images</returns>
        /// <exception cref="Exception">si une erreur survient lors de la lecture des images</exception>
        public List<byte[]> GetAllImages()
        {
            try
            {
                List<string> path = _data.GetAllImagesDATA();
                List<byte[]> image = new List<byte[]>();
                foreach (string pathItem in path)
                {
                    byte[] imageBytes = System.IO.File.ReadAllBytes(pathItem);
                    image.Add(imageBytes);
                }
                
                return image;
            }
            catch (Exception ex)
            {
                throw new Exception(ex.ToString());
            }
        }

        /// <summary>
        /// Méthode non implémentée
        /// </summary>
        /// <param name="image">L'objet ImageDifference représentant les images</param>
        /// <exception cref="NotImplementedException">Cette méthode n'est pas encore implémentée</exception>
        public void SetImages(ImageDifference image)
        {
            throw new NotImplementedException();
        }

        /// <summary>
        /// Récupère une paire d'images à partir de l'ID de la paire.
        /// </summary>
        /// <param name="pairId">l'ID de la paire d'images</param>
        /// <returns>Un tuple contenant deux tableaux de bytes représentant les images de la paire</returns>
        /// <exception cref="Exception">Si la paire d'image est incomplète ou introuvable</exception>
        public (byte[] Image1, byte[] Image2) GetImagePair(int pairId)
        {
            try
            {
                var imagePaths = _data.GetAllImagesWithPairData()
                                      .Where(img => img.ImagePairId == pairId)
                                      .Select(img => img.ImageLink)
                                      .ToList();

                if (imagePaths.Count != 2)
                {
                    throw new Exception($"La paire d'images avec l'ID {pairId} est incomplète ou introuvable.");
                }

                byte[] image1 = File.ReadAllBytes(imagePaths[0]);
                byte[] image2 = File.ReadAllBytes(imagePaths[1]);

                return (image1, image2);
            }
            catch (Exception ex)
            {
                throw new Exception($"Erreur lors de la récupération de la paire d'images avec l'ID {pairId}: {ex.Message}", ex);
            }
        }

        /// <summary>
        /// Récupère toutes les images avec leurs paires associées et les retourne sous forme d'une liste 
        /// </summary>
        /// <returns>une liste d'objets ImageWithPair contenant les paires d'images de leurs ID</returns>
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
        /// <param name="Idsession">l'ID de la session de jeu</param>
        /// <param name="_session">Le service de gestion de session</param>
        /// <returns>Un tuple contenant les deux images à envoyer au joueur</returns>
        /// <exception cref="Exception">Si la session est invalide ou si l'hôte n'a pas choisi d'image</exception>
        public (byte[] Image1, byte[] Image2) ReadyImageToPlayer(string Idsession , SessionService _session)
        {
            var session = _session.GetSessionById(Idsession);
            if (session == null)
            {
                throw new Exception("session invalide");
            }

            if (session.ImagePairId == 0)
            {
                throw new Exception("l'hote n'a pas choisi d'image");
            }
            
            var imagePairId = session.ImagePairId;
            var images = GetImagePair(imagePairId);

            return images;
        }


    }
}
