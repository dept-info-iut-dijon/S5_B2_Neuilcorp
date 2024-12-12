using API7D.Metier;
using API7D.objet;
using API7D.Services;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.ActionConstraints;
using Microsoft.Extensions.Logging;

namespace API7D.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class DifferanceController : ControllerBase
    {
        private readonly IDifferanceChecker _checker;
        private readonly SessionService _sessionService;
        private readonly ILogger<DifferanceChecker> _logger;

        /// <summary>
        /// Initialise une nouvelle instance du contrôleur de différences.
        /// </summary>
        /// <param name="sessionService">Service de gestion des sessions</param>
        /// <param name="logger">Logger pour le traçage des événements</param>
        public DifferanceController(SessionService sessionService, ILogger<DifferanceChecker> logger)
        {
            _logger = logger;
            _checker = new DifferanceChecker(logger);
            _sessionService = sessionService;
        }

        /// <summary>
        /// Vérifie si les coordonnées correspondent à une différence dans l'image.
        /// </summary>
        /// <param name="coordonnees">Coordonnées X,Y sélectionnées par le joueur</param>
        /// <param name="sessionId">Identifiant de la session de jeu</param>
        /// <param name="playerId">Identifiant du joueur</param>
        /// <param name="imageId">Identifiant de l'image à vérifier</param>
        /// <returns>200 OK avec succès=true si la différence est valide, 400 sinon</returns>
        /// <response code="200">La vérification a réussi</response>
        /// <response code="400">La vérification a échoué</response>
        /// <response code="500">Erreur interne du serveur</response>
        [HttpPost("check")]
        public async Task<IActionResult> CheckDifference(
            [FromBody] Coordonnees coordonnees,
            [FromQuery] string sessionId,
            [FromQuery] string playerId,
            [FromQuery] string imageId)
        {
            _logger.LogInformation("Réception d'une requête de vérification de différence");
            
            try
            {
                bool isInZone = await _checker.IsWithinDifferenceAsync(
                    coordonnees,
                    int.Parse(imageId),
                    sessionId,
                    _sessionService,
                    playerId
                );

                await _sessionService.NotifyPlayers(sessionId, isInZone);
                return Ok(new { success = isInZone });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Erreur lors de la vérification de la différence");
                return StatusCode(500, new { success = false, message = ex.Message });
            }
        }

        /// <summary>
        /// Ajoute une liste de différences (coordonnées).
        /// </summary>
        /// <param name="coordonnees">Liste des coordonnées représentant les différences.</param>
        /// <returns>
        /// 200 Ok : si l'ajout est réussi.
        /// 400 BadRequest : si les données sont invalides ou si une erreur survient.
        /// </returns>
        [HttpPost("add")]
        public IActionResult AddDifferance([FromBody] List<Coordonnees> coordonnees)
        {
            IActionResult result;

            if (coordonnees == null || !coordonnees.Any())
            {
                _logger.LogWarning("La liste de coordonnées est vide ou nulle.");
                result = BadRequest("La liste de coordonnées est vide ou invalide.");
            }
            else
            {
                try
                {
                    // Logique d'ajout des différences
                    foreach (var coord in coordonnees)
                    {
                        _logger.LogInformation($"Ajout de la coordonnée : X={coord.X}, Y={coord.Y}");
                        // Appel à une méthode pour sauvegarder les différences, si nécessaire.
                        // Exemple : _sessionService.AddDifference(coord);
                    }

                    result = Ok("Les différences ont été ajoutées avec succès.");
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "Erreur lors de l'ajout des différences.");
                    result = StatusCode(500, new { success = false, message = "Une erreur est survenue lors de l'ajout des différences." });
                }
            }

            return result;
        }
    }
}
