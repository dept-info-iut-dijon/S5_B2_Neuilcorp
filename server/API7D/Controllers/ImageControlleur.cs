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
        IImage image = new image();
        [HttpGet("{id}")]
        public ActionResult<ImageDifference> GetImage(int id)
        {
            // a modif avec la BDD que j ai pas faite encore
            byte[] image = this.image.GetImages(id);
            if (image == null)
            {
                return NotFound();
            }
            return Ok(image);
        }
    }
}
