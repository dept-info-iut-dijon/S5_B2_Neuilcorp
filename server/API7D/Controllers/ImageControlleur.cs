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

        public ImageControlleur()
        {
            // Spécifie le chemin vers le dossier contenant les images
            _imageFolderPath = Path.Combine(Directory.GetCurrentDirectory(), "Image");
        }

        [HttpGet("{id}")]
        [Produces("application/octet-stream")]
        public ActionResult<byte[]> GetImage(int id)
        {
            // Construit le nom de fichier basé sur l'ID
            string fileName = $"{id}.png"; 
            string filePath = Path.Combine(_imageFolderPath, fileName);

            // Vérifie si le fichier existe
            if (!System.IO.File.Exists(filePath))
            {
                return NotFound(); 
            }

            // Lis le fichier et le convertit en tableau de bytes
            byte[] imageBytes = System.IO.File.ReadAllBytes(filePath);
            return File(imageBytes, "application/octet-stream");
        }
    }
}