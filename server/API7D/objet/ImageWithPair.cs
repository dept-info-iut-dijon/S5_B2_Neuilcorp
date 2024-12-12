namespace API7D.objet
{

    /// <summary>
    /// Représente une image avec son ID et l'identifiant de la paire d'images à laquelle elle appartient.
    /// </summary>
    public class ImageWithPair
    {
        /// <summary>
        /// Obtient ou définit l'identifiant unique de l'image.
        /// </summary>
        public int ImageId { get; set; }

        /// <summary>
        /// Obtient ou définit l'identifiant de la paire d'images à laquelle cette image appartient.
        /// </summary>
        public int ImagePairId { get; set; }

        /// <summary>
        /// Obtient ou définit la représentation de l'image en format Base64.
        /// </summary>
        public string Base64Image { get; set; }
    }
}