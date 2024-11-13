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
                byte[] imageBytes = System.IO.File.ReadAllBytes(path);
                return imageBytes;
            }
            catch (Exception ex)
            {
                throw new Exception(ex.ToString());
            }
        }

        public List<byte[]> GetAllImages()
        {
            try
            {
                List<string> path = _data.GetAllImagesDATA();
                List<byte[]> image = new List<byte[]>();
                foreach (string pathItem in path)
                {
                    byte[] imageBytes = System.IO.File.ReadAllBytes(pathItem);
                    image.Add(imageBytes);
                }
                
                return image;
            }
            catch (Exception ex)
            {
                throw new Exception(ex.ToString());
            }
        }

        public void SetImages(ImageDifference image)
        {
            throw new NotImplementedException();
        }
    }
}
