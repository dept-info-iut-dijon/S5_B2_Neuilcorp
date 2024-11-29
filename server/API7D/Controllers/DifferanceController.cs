﻿using API7D.Metier;
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
            private readonly SessionService _sessionService;

        public DifferanceController(SessionService sessionService)
        {
            this.checker = new DifferanceChecker();
            _sessionService = sessionService;
        }
        /// <summary>
        /// permet de veifier les différance
        /// </summary>
        /// <param name="coordonnees">une coordonée X Y</param>
        /// <returns></returns>
        [HttpPost("check")]
        public IActionResult CheckDifference([FromBody] Coordonnees coordonnees , int IdimagePair,string sessionID,string playerID)
        {
            try
            {
                bool isInZone = checker.IsWithinDifference(coordonnees,IdimagePair,sessionID, _sessionService, playerID);
                return Ok(isInZone);  // Envoie la réponse correctes
            }
            catch (Exception ex)
            {
                return BadRequest("Erreur dans le traitement des coordonnées");  // Gestion d'erreurs
            }
        }


    }
}
