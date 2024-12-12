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
            private IDifferanceChecker checker;
            private readonly SessionService _sessionService;
            private readonly ILogger<DifferanceChecker> _logger;

        public DifferanceController(SessionService sessionService , ILogger<DifferanceChecker> logger)
        {
            _logger = logger;
            this.checker = new DifferanceChecker(logger);
            _sessionService = sessionService;

        }

        /// <summary>
        /// permet de verifier les différences
        /// </summary>
        /// <param name="coordonnees">une coordonée X Y</param>
        /// <param name="IdImage">L'identifiant de l'image à vérifier</param>
        /// <returns>
        /// 200 Ok : si la vérification est réussie
        /// 400 Bad Request : si la vérification a échoué
        /// </returns>
        [HttpPost("check")]
        public async Task<IActionResult> CheckDifference(
            [FromBody] Coordonnees coordonnees,
            [FromQuery] string sessionId,
            [FromQuery] string playerId,
            [FromQuery] string imageId)
        {
            _logger.LogWarning("j'ai reçu une requête pour vérifier une différence.");
            IActionResult actionResult;

            try
            {
                // Appelle la méthode asynchrone pour vérifier si la différence est valide
                bool isInZone = await checker.IsWithinDifferenceAsync(
                    coordonnees,
                    int.Parse(imageId),
                    sessionId,
                    _sessionService,
                    playerId
                );
                await _sessionService.NotifyPlayers(sessionId, isInZone);
                GameSession existingSession = _sessionService.GetSessionById(sessionId);



                if (existingSession.GameCompleted)
                {
                    //signialR ici
                }






                // Retourne une réponse HTTP avec le statut approprié
                actionResult = Ok(new { success = isInZone });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Une erreur est survenue lors de la vérification de la différence.");
                actionResult = StatusCode(500, new { success = false, message = ex.Message });
            }
            return actionResult;
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
