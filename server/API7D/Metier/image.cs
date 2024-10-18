using API7D.DATA;
using API7D.objet;

namespace API7D.Metier
{
    public class image : IImage
    {
        private IImageDATA _data = new ImageDATA()!;
        public byte[] GetImages(int ID)
        {
            try
            {
                string path = _data.GetImagesDATA(ID);
                // Lire l'image et la convertir en byte[]
                byte[] imageBytes = System.IO.File.ReadAllBytes(path);
                return imageBytes;
            }
            catch (Exception ex)
            {
                // faire une exception plutard
                return null;
            }
        }

        public void SetImages(ImageDifference image)
        {
            throw new NotImplementedException();
        }
    }
}
