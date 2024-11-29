using API7D.Metier;

namespace API7D.objet
{
    public class ImageDifference
    {
        private Dictionary<int, List<Coordonnees>> diferanceImage = new Dictionary<int, List<Coordonnees>>();

        /// <summary>
        /// Obtient les différences d'image associées à un identifiant d'image
        /// </summary>
        public Dictionary<int, List<Coordonnees>> DiferanceImage
        {
            get { return diferanceImage; }
        }

        /// <summary>
        /// Constructeur pour initialiser l'objet ImageDifference avec une liste de coordonnées et un identifiant d'image
        /// </summary>
        /// <param name="coordonne">Liste de coordonnées représentant les différences dans l'image</param>
        /// <param name="image">Identifiant de l'image à laquelle ces différences sont associées</param>
        public ImageDifference(List<Coordonnees> coordonne, int image )
        {
            diferanceImage.Add(image, coordonne);
        }

    }
}
