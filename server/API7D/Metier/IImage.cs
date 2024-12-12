using API7D.objet;
using API7D.Services;

namespace API7D.Metier
{
    public interface IImage
    {
        /// <summary>
        /// Récupère une image par son ID.
        /// </summary>
        /// <param name="ID">L'ID de l'image.</param>
        /// <returns>Tableau de bytes représentant l'image.</returns>
        byte[] GetImages(int ID);

        /// <summary>
        /// Ajoute une image avec une liste des différences.
        /// </summary>
        /// <param name="image1">Première image en bytes</param>
        /// <param name="image2">Deuxième image en bytes</param>
        /// <param name="name">Nom de base pour les fichiers</param>
        /// <param name="difference">Liste des coordonnées des différences</param>
        void SetImages(byte[] image1, byte[] image2, string name, List<Coordonnees> difference);

        /// <summary>
        /// Récupère toutes les images sous forme de tableau de bytes.
        /// </summary>
        /// <returns>Liste de tableaux de bytes représentant toutes les images.</returns>
        List<byte[]> GetAllImages();

        /// <summary>
        /// Récupère une paire d'images à partir de l'ID de la paire.
        /// </summary>
        /// <param name="imagePaireId">L'ID de la paire d'images.</param>
        /// <returns>Tuple contenant les deux images sous forme de tableaux de bytes.</returns>
        (byte[] Image1, byte[] Image2) GetImagePair(int imagePaireId);

        /// <summary>
        /// Récupère toutes les images avec leurs ID, ID de paire, et l'image encodée en base64.
        /// </summary>
        /// <returns>Liste contenant l'ID de l'image, l'ID de la paire et l'image en base64.</returns>
        List<ImageWithPair> GetAllImagesWithPairs();

        /// <summary>
        /// Prépare et récupère les images pour un joueur dans une session donnée.
        /// </summary>
        /// <param name="Idsession">Identifiant de la session</param>
        /// <param name="_session">Service de gestion des sessions</param>
        /// <returns>Tuple contenant les deux images de la paire</returns>
        /// <exception cref="ArgumentException">Si la session est invalide</exception>
        /// <exception cref="InvalidOperationException">Si aucune paire d'images n'est sélectionnée</exception>
        (byte[] Image1, byte[] Image2) ReadyImageToPlayer(string Idsession, SessionService _session);
    }
}
