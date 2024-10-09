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
            [HttpGet("check")]
            public IActionResult CheckDifference([FromQuery] int x, [FromQuery] int y)
            {
            Coordinate coordinate = new Coordinate(x, y);
                // Appel de la méthode IsWithinDifference pour vérifier si les coordonnées sont dans la zone d'acceptation
                bool isInZone = checker.IsWithinDifference(coordinate);

                // Retourne le résultat en tant que booléen (true si dans la zone, sinon false)
                return Ok(isInZone);
            }
        }
    }
