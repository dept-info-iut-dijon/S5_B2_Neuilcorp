using API7D.Metier;
using API7D.objet;

namespace API7D.DATA
{
    public interface IImageDATA
    {
        /// <summary>
        /// recupere une image
        /// </summary>
        /// <param name="ID"> l'id de l'image</param>
        /// <returns></returns>
        public string GetImagesDATA(int ID);
        /// <summary>
        /// permet d'ajouter une image avec une liste des differances
        /// </summary>
        /// <param name="image"></param>
        public void SetImagesDATA(ImageDifference image);

        /// <summary>
        /// Récupère tous les chemins d'images.
        /// </summary>
        /// <returns>Liste des chemins d'images</returns>
        public List<string> GetAllImagesDATA();

        /// <summary>
        /// Récupère une paire d'images par ID de paire.
        /// </summary>
        /// <param name="imagePaireId">ID de la paire d'images</param>
        /// <returns>Tuple contenant les deux images sous forme de byte arrays</returns>
        public (byte[] Image1, byte[] Image2) GetImagePair(int imagePaireId);

        /// <summary>
        /// Récupère toutes les images avec leurs données de paires.
        /// </summary>
        /// <returns>Liste des images avec leurs ID, ID de paires et lien</returns>
        List<(int ImageId, int ImagePairId, string ImageLink)> GetAllImagesWithPairData();
    }
}
