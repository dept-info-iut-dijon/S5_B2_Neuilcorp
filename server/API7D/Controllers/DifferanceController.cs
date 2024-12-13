using API7D.Metier;
using API7D.objet;
using API7D.Services;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.ActionConstraints;
using Microsoft.AspNetCore.SignalR;
using Microsoft.Extensions.Logging;
using System.Numerics;

namespace API7D.Controllers
{
    [Route("api/[controller]")]


    [ApiController]
    public class DifferanceController : ControllerBase
    {
            private IDifferanceChecker checker;
            private readonly SessionService _sessionService;
            private readonly ILogger<DifferanceChecker> _logger;
            private readonly IHubContext<GameSessionHub> _hubContext; 


        public DifferanceController(SessionService sessionService , ILogger<DifferanceChecker> logger, IHubContext<GameSessionHub> hubContext)
        {
            _logger = logger;
            this.checker = new DifferanceChecker();
            _sessionService = sessionService;
            _hubContext = hubContext;

        }

        /// <summary>
        /// permet de verifier les différences
        /// </summary>
        /// <param name="coordonnees">une coordonée X Y</param>
        /// <param name="IdImage">L'identifiant de l'image à vérifier</param>
        [HttpPost("check")]
        public async void CheckDifference(
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
                int result = await checker.IsWithinDifferenceAsync(
                    coordonnees,
                    int.Parse(imageId),
                    sessionId,
                    _sessionService,
                    playerId
                );
                switch (result)
                {
                    case 0:
                        await _sessionService.NotifyPlayers(sessionId, false);
                        break;
                    case 1:
                        await _sessionService.NotifyTimerExpired(sessionId);
                        break;
                    case 2:
                        await _sessionService.NotifyPlayers(sessionId, true);
                        break;

                }

                GameSession existingSession = _sessionService.GetSessionById(sessionId);

                if (existingSession.GameCompleted)
                {
                    if (string.IsNullOrEmpty(sessionId))
                    {
                        _logger.LogError("SessionId ne peut pas être null ou vide.");
                        throw new ArgumentException("SessionId est requis.", nameof(sessionId));
                    }

                  
                        // Récupère la session correspondante à partir d'une source (par exemple, un service ou un repository)
                        GameSession session = _sessionService.GetSessionById(sessionId);

                        if (session == null)
                        {
                            _logger.LogError($"Session avec l'ID {sessionId} introuvable.");
                            throw new InvalidOperationException($"La session {sessionId} n'existe pas.");
                        }

                        // Notifie tous les clients appartenant au groupe SignalR correspondant à la session
                        await _hubContext.Clients.Group(sessionId).SendAsync("GameEnded", session.Attempts, session.MissedAttempts, session.TimersExpired);

                        _logger.LogInformation($"Tous les joueurs de la session {sessionId} ont été notifiés de la fin du jeu.");
                    
                    
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Une erreur est survenue lors de la vérification de la différence.");
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

        /// <summary>
        /// Récupère le statut actuel du timer pour une session.
        /// </summary>
        /// <param name="sessionId">ID de la session.</param>
        /// <returns>Statut du timer et détails associés.</returns>
        [HttpGet("{sessionId}/timer/status")]
        public IActionResult GetTimerStatus(string sessionId)
        {
            var session = _sessionService.GetSessionById(sessionId);
            if (session == null)
            {
                return NotFound($"Session {sessionId} introuvable.");
            }

            var elapsedTime = (DateTime.UtcNow - session.TimerStartTime).TotalSeconds;
            var timeRemaining = session.TimerDuration - elapsedTime;

            return Ok(new
            {
                TimerActive = session.TimerActive,
                TimeRemaining = timeRemaining > 0 ? timeRemaining : 0,
                Attempts = session.Attempts,
                MissedAttempts = session.MissedAttempts,
                TimersExpired = session.TimersExpired
            });
        }
    }
}
