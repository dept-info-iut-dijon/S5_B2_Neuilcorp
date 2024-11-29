using API7D.Metier;
using API7D.objet;
using API7D.Services;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;

namespace API7D.Controllers
{
    [Route("api/[controller]")]


    [ApiController]
    public class DifferanceController : ControllerBase
    {
            private IDifferanceChecker checker;

        public DifferanceController()
        {
            this.checker = new DifferanceChecker();
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
        public IActionResult CheckDifference([FromBody] Coordonnees coordonnees , int IdImage)
        {
            try
            {
                bool isInZone = checker.IsWithinDifference(coordonnees,IdImage);
                return Ok(isInZone);  // Envoie la réponse correcte
            }
            catch (Exception ex)
            {
                return BadRequest("Erreur dans le traitement des coordonnées");  // Gestion d'erreurs
            }
        }


    }
}
