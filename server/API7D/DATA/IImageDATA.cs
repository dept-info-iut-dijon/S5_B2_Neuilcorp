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

    }
}
