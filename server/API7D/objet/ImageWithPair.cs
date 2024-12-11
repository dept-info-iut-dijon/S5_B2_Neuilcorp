namespace API7D.objet
{

    /// <summary>
    /// Représente une image avec son ID et l'identifiant de la paire d'images à laquelle elle appartient.
    /// </summary>
    public class ImageWithPair
    {
        private int imageId;
        private int imagePairId;
        private string base64Image;

        /// <summary>
        /// Obtient ou définit l'ID de l'image
        /// </summary>
        public int ImageId
        {
            get { return imageId; }
            set { imageId = value; }
        }
        /// <summary>
        /// Obtient ou définit l'ID de la paire d'images
        /// </summary>
        public int ImagePairId
        {
            get { return imagePairId; }
            set { imagePairId = value; }
        }

        /// <summary>
        /// Obtient ou définit l'image encodée en base64
        /// </summary>
        public string Base64Image
        {
            get { return base64Image; }
            set { base64Image = value; }
        }
    }
}