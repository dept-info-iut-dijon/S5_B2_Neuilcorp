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
        /// <param name="image">Objet ImageDifference contenant les informations sur l'image et ses différences.</param>
        void SetImages(byte[] image1 , byte[] image2 , List<Coordonnees> difference);

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

        public (byte[] Image1, byte[] Image2) ReadyImageToPlayer(string Idsession, SessionService _session);
    }
}
