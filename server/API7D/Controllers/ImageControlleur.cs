using API7D.DATA;
using API7D.Metier;
using API7D.objet;
using Microsoft.AspNetCore.Mvc;
using static System.Net.Mime.MediaTypeNames;

namespace API7D.Controllers
{
    [ApiController]
    [Route("[controller]")]
    public class ImageControlleur : ControllerBase
    {
        private readonly string _imageFolderPath;
        private IImage image = new image();

        public ImageControlleur()
        {
            // Spécifie le chemin vers le dossier contenant les images
            _imageFolderPath = Path.Combine(Directory.GetCurrentDirectory(), "Image");
        }

        /// <summary>
        /// permet de recuperer une image par son id
        /// </summary>
        /// <param name="id"></param>
        /// <returns></returns>

        [HttpPost("{id}")]
        public ActionResult<byte[]> GetImage(int id)
        {
            byte[] returnedImage = image.GetImages(id);
            return returnedImage;
        }

        [HttpGet("{id}")]
        public ActionResult<byte[]> GetAllImage(int id)
        {
            byte[] returnedImage = image.GetAllImages();
            return returnedImage;
        }
    }
}