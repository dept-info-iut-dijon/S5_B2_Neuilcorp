using API7D.Metier;
using API7D.objet;
using System.IO;

namespace API7D.DATA
{
    public class ImageDATA : IImageDATA
    {
        public string GetImagesDATA(int ID)
        {
            //a connecter a la futur BDD en attandant mettre chemin image ici
            string path = "C:\\Users\\antoi\\Desktop\\android2\\S5_B2_Neuilcorp\\server\\API7D\\Image\\test.png";
            return path;
        }

        public bool ImageDifferanceChecker(int IdImage, Coordinate coordonnéeDiffTest)
        {
            throw new NotImplementedException();
        }

        public void SetImagesDATA(ImageDifference image)
        {
            throw new NotImplementedException();
        }
    }
}
