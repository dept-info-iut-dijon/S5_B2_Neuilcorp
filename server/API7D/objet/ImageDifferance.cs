using API7D.Metier;

namespace API7D.objet
{
    public class ImageDifference
    {
        private Dictionary<int, List<Coordonnees>> diferanceImage = new Dictionary<int, List<Coordonnees>>();

        public Dictionary<int, List<Coordonnees>> DiferanceImage
        {
            get { return diferanceImage; }
        }

        public ImageDifference(List<Coordonnees> coordonne, int image )
        {
            diferanceImage.Add(image, coordonne);
        }

    }
}
