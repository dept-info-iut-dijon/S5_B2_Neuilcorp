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
            /// <summary>
            /// permet de verifier si les differance trouver pas l'utilisateur est bonne
            /// </summary>
            /// <param name="IdImage">Id de l'image</param>
            /// <param name="coordonnéeDiffTest">une coordonée</param>
            /// <returns></returns>
            public bool ImageDifferanceChecker(int IdImage, Coordonnees coordonnéeDiffTest);

    }
    

}

