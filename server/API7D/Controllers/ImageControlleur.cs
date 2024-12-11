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
        private readonly IImage _imageService;
        private readonly ILogger<GameSessionHub> _logger;


        /// <summary>
        /// Initialise une nouvelle instance de ImageControlleur.
        /// </summary>
        /// <param name="hubContext">Contexte SignalR permettant la communication en temsp réel</param>
        /// <param name="sessionService">Service de session de jeu</param>
        /// <param name="imageService">Service de gestion d'image </param>
        /// <param name="logger">Service de log </param>
        public ImageControlleur(IHubContext<GameSessionHub> hubContext, SessionService sessionService, IImage imageService , ILogger<GameSessionHub> logger)
        {
            _hubContext = hubContext;
            _sessionService = sessionService;
            _imageService = imageService;
            // Spécifie le chemin vers le dossier contenant les images
            _imageFolderPath = Path.Combine(Directory.GetCurrentDirectory(), "Image");
            _logger = logger;

        }

        /// <summary>
        /// permet de recuperer une image par son id
        /// </summary>
        /// <param name="id"></param>
        /// <returns>une réponse contenant l'image sous forme de tableau d'octet ou un code http 404</returns>

        [HttpPost("{id}")]
        public ActionResult<byte[]> GetImage(int id)
        {
            byte[] returnedImage = _imageService.GetImages(id);

            ActionResult<byte[]> result;
            if (returnedImage == null)
            {
                result = NotFound($"Image {id} non trouvée.");
            }
            else
            {
                result = new FileContentResult(returnedImage, "application/octet-stream");
            }

            return result;
        }



        /// <summary>
        /// permet de recuperer toutes les images
        /// </summary>
        /// <returns>liste de tableau de d'octet représentant les images ou une liste vide si aucune image n'est disponible</returns>
        [HttpGet("allImage")]
        public ActionResult<List<byte[]>> GetAllImage()
        {
            List<byte[]> returnedImage = _imageService.GetAllImages();

            ActionResult<List<byte[]>> result = returnedImage.Any() ? Ok(returnedImage) : new ActionResult<List<byte[]>>(new List<byte[]>());

            return result;
        }


        /// <summary>
        /// Récupère toutes les paires d'images disponibles.
        /// </summary>
        /// <returns>Une liste d'objet "ImageWithPair" contenant les informations sur  les paires d'images</returns>
        [HttpGet("allImagesWithPairs")]
        public ActionResult<List<ImageWithPair>> GetAllImagesWithPairs()
        {
            List<ImageWithPair> images = _imageService.GetAllImagesWithPairs();

            ActionResult<List<ImageWithPair>> result = images.Any() ? Ok(images) : NotFound("Aucune paire d'images disponible.");

            return result;
        }


        /// <summary>
        /// Envoie une paire d'image sélectionnée aux joueurs d'une session
        /// </summary>
        /// <param name="sessionId">l'ID de la session de jeu</param>
        /// <returns>Une réponse HTTP indiquant le succès ou l'échec de l'envoi des images</returns>
        [HttpPost("{sessionId}/sendImages")]
        public async Task<ActionResult> SendImagesToPlayers(string sessionId)
        {
            var session = _sessionService.GetSessionById(sessionId);
            ActionResult result;

            if (session == null)
            {
                result = NotFound($"Session {sessionId} non trouvée.");
            }
            else if (session.ImagePairId == 0)
            {
                await _hubContext.Clients.Group(sessionId).SendAsync("NotifyMessage", "L'hôte n'a pas encore choisi une paire d'images.");
                result = BadRequest("L'hôte n'a pas encore choisi une paire d'images.");
            }
            else
            {
                var imagePairId = session.ImagePairId;
                var images = _imageService.GetImagePair(imagePairId);

                var melangePlayers = session.Players.OrderBy(_ => Guid.NewGuid()).ToList();
                int imageSwitch = 0;

                foreach (var player in melangePlayers)
                {
                    var imageToSend = (imageSwitch % 2 == 0) ? images.Image1 : images.Image2;
                    imageSwitch++;

                    await _hubContext.Clients.Client(player.PlayerId).SendAsync("GameStarted", imageToSend);
                    _logger.LogInformation($"Image envoyée à : {player.PlayerId}.");
                }

                result = Ok("Images envoyées aux joueurs.");
            }

            return result;
        }


        /// <summary>
        /// Permet à l'hôte de sélectionner une paire d'images pour la session.
        /// </summary>
        /// <param name="sessionId">l'ID unique de la session de jeu</param>
        /// <param name="imagePairId">Identifiant de la paire d'image à sélectionner</param>
        /// <returns>Une réponse HTTP 200 si la paire d'images est sélectionnée avec succès ou HTTP 404 si la session n'existe pas</returns>
        [HttpPost("{sessionId}/selectImagePair")]
        public ActionResult SelectImagePair(string sessionId, [FromBody] int imagePairId)
        {
            var session = _sessionService.GetSessionById(sessionId);

            ActionResult result;
            if (session == null)
            {
                result = NotFound($"Session {sessionId} non trouvée.");
            }
            else
            {
                session.ImagePairId = imagePairId;
                _sessionService.UpdateSession(session);
                result = Ok("Paire d'images sélectionnée pour la session.");
            }

            return result;
        }

    }
}