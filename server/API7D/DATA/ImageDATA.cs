using API7D.Metier;
using API7D.objet;
using System.IO;

namespace API7D.DATA
{
    public class ImageDATA : IImageDATA
    {

        /// <summary>
        /// fausse base de donnée
        /// </summary>
        /// <param name="ID"></param>
        /// <returns></returns>
        public string GetImagesDATA(int ID)
        {
            //a connecter a la futur BDD en attandant mettre chemin image ici
            string path = "C:\\Users\\antoi\\Desktop\\android2\\S5_B2_Neuilcorp\\server\\API7D\\Image\\test.png";
            return path;
        }

        public void SetImagesDATA(ImageDifference image)
        {
            throw new NotImplementedException();
        }
    }
}
