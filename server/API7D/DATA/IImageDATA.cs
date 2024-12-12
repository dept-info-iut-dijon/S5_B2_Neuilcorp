using API7D.Metier;
using API7D.objet;

namespace API7D.DATA
{
    public interface IImageData
    {
        /// <summary>
        /// Récupère le chemin d'une image spécifique par son ID.
        /// </summary>
        /// <param name="ID">ID de l'image</param>
        /// <returns>Le chemin de l'image</returns>
        /// <exception cref="Exception">Si l'image n'est pas trouvée</exception>
        string GetImagesDATA(int ID);

        /// <summary>
        /// Permet d'ajouter une paire d'images avec une liste de différences.
        /// </summary>
        /// <param name="path1">Chemin de la première image</param>
        /// <param name="path2">Chemin de la deuxième image</param>
        /// <param name="difference">Liste des coordonnées des différences</param>
        /// <exception cref="ArgumentException">Si les chemins sont null ou vides</exception>
        /// <exception cref="ArgumentNullException">Si la liste des différences est null</exception>
        public void SetImagesDATA(string path1, string path2, List<Coordonnees> difference);

        /// <summary>
        /// Récupère tous les chemins des images disponibles dans la base de données.
        /// </summary>
        /// <returns>Liste des chemins d'images</returns>
        List<string> GetAllImagesDATA();

        /// <summary>
        /// Récupère une paire d'images associées à un ID de paire donné.
        /// </summary>
        /// <param name="imagePaireId">ID de la paire d'images</param>
        /// <returns>Tuple contenant les deux images sous forme de tableaux de bytes</returns>
        (byte[] Image1, byte[] Image2) GetImagePair(int imagePaireId);

        /// <summary>
        /// Récupère toutes les images, leurs ID, ID de paires, et leurs liens.
        /// </summary>
        /// <returns>Liste de tuples contenant l'ID de l'image, l'ID de la paire et le lien de l'image</returns>
        List<(int ImageId, int ImagePairId, string ImageLink)> GetAllImagesWithPairData();
    }
}
