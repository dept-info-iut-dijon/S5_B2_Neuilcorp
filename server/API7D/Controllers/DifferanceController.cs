using API7D.Metier;
using API7D.objet;
using API7D.Services;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
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
                // Retourne une réponse HTTP avec le statut approprié
                return Ok(new { success = isInZone });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Une erreur est survenue lors de la vérification de la différence.");
                return StatusCode(500, new { success = false, message = ex.Message });
            }
        }


    }
}
