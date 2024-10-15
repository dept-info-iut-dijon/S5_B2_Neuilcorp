using API7D.Metier;
using API7D.objet;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;

namespace API7D.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class DifferanceController : ControllerBase
    {
            // Instance de DifferenceChecker avec la liste des différences
            private IDifferanceChecker checker;

            public DifferanceController()
            {
           
                checker = new DifferanceChecker();
            }

        // Endpoint pour vérifier si les coordonnées fournies sont dans la zone d'acceptation
        // GET: api/difference/check?x=...&y=...
        [HttpPost("check")]
        public IActionResult CheckDifference([FromBody] Coordonnees coordonnees)
        {
            try
            {
                bool isInZone = checker.IsWithinDifference(coordonnees);
                return Ok(isInZone);  // Envoie la réponse correcte
            }
            catch (Exception ex)
            {
                return BadRequest("Erreur dans le traitement des coordonnées");  // Gestion d'erreurs
            }
        }

    }
}
