using API7D.objet;

namespace API7D.Metier
{
    public interface IImage
    {
            /// <summary>
            /// recupere une image
            /// </summary>
            /// <param name="ID"> l'id de l'image</param>
            /// <returns></returns>
            public byte[] GetImages(int ID);
            /// <summary>
            /// permet d'ajouter une image avec une liste des differances
            /// </summary>
            /// <param name="image"></param>
            public void SetImages(ImageDifference image);

        public List<byte[]> GetAllImages();

        public (byte[] Image1, byte[] Image2) GetImagePair(int imagePaireId);


    }
    

}

