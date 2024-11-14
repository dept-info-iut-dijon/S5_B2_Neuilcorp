using API7D.DATA;
using API7D.Metier;
using API7D.objet;
using API7D.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.SignalR;
using static System.Net.Mime.MediaTypeNames;

namespace API7D.Controllers
{
    [ApiController]
    [Route("[controller]")]
    public class ImageControlleur : ControllerBase
    {
        private readonly SessionService _sessionService;
        private readonly string _imageFolderPath;
        private readonly IHubContext<GameSessionHub> _hubContext;

        private IImage _imageService = new image();


        public ImageControlleur(IHubContext<GameSessionHub> hubContext, SessionService sessionService)
        {
            _hubContext = hubContext;
            _sessionService = sessionService;
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
            byte[] returnedImage = _imageService.GetImages(id);
            return returnedImage;
        }

        [HttpGet("allImage")]
        public ActionResult<List<byte[]>> GetAllImage()
        {
            List<byte[]> returnedImage = _imageService.GetAllImages();
            return returnedImage;
        }

        [HttpPost("{sessionId}/sendImages")]
        public async Task<ActionResult> SendImagesToPlayers(string sessionId)
        {
            var session = _sessionService.GetSessionById(sessionId);
            if (session == null)
            {
                return NotFound($"Session {sessionId} non trouvée.");
            }

            var imagePairId = session.ImagePairId;
            var images = _imageService.GetImagePair(imagePairId); // images[0] et images[1] contiennent les deux images

            

            int imageSwitch = 0;
            foreach (var player in session.Players)
            {
                var imageToSend = (imageSwitch % 2 == 0) ? images.Image1 : images.Image2;
                imageSwitch++;

                await _hubContext.Clients.Client(player.PlayerId).SendAsync("ReceiveImage", imageToSend);
            }

            return Ok("Images envoyées aux joueurs.");
        }


    }
}