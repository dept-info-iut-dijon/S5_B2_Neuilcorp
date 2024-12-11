using API7D.Metier;
using API7D.objet;
using API7D.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.SignalR;
using System.Collections.Generic;
using System.Threading.Tasks;

[ApiController]
[Route("api/[controller]")]

/// <summary>
/// Contrôleur pour gérer les sessions de jeu, il fournit les endpoints pour créer, rejoindre et supprimer des sessions ainsi que pour gérer les joueurs dans une session.
public class GameSessionController : ControllerBase
{
    private readonly SessionService _sessionService;
    private readonly IHubContext<GameSessionHub> _hubContext;
    private readonly SessionCodeGenerator _codeGenerator = new SessionCodeGenerator();
    private readonly ILogger<GameSessionController> _logger;


    /// <summary>
    /// Initialise une nouvelle instance de GameSessionController.
    /// </summary>
    /// <param name="hubContext">Le contexte SinglaR utilisé pour envoyer des messages en temps réel</param>
    /// <param name="sessionService">Service de gestion des sessions de jeu</param>
    /// <param name="logger">L'instance de logger</param>
    public GameSessionController(IHubContext<GameSessionHub> hubContext, SessionService sessionService, ILogger<GameSessionController> logger)
    {
        _hubContext = hubContext;
        _sessionService = sessionService;
        _logger = logger;
    }

    /// <summary>
    /// Crée une nouvelle session de jeu avec l'hôte en tant que premier joueur.
    /// </summary>
    /// <param name="gameSession">L'objet GameSession contenant les informations initiales sur les joueurs.</param>
    /// <returns>La session de jeu créée ou un résultat BadRequest si les données de session sont invalides.</returns>
    [HttpPost("CreateSession")]
    public ActionResult<GameSession> CreateSession([FromBody] GameSession gameSession)
    {
        ActionResult actionResult = null;
        if (gameSession == null || gameSession.Players == null || gameSession.Players.Count == 0)
        {
            actionResult = BadRequest("Les données de session ou les informations sur l'hôte sont invalides.");
        }
        else
        {

            gameSession.SessionId = _codeGenerator.GenerateUniqueCode().ToString();
            Player host = gameSession.Players[0];
            gameSession.Players = new List<Player> { host };
            gameSession.GameCompleted = false;
            gameSession.GameTimer = false;
            _sessionService.AddSession(gameSession);
            actionResult = Ok(gameSession);

        }

        return actionResult;
    }


    /// <summary>
    /// Permet à un joueur de rejoindre une session de jeu existante.
    /// </summary>
    /// <param name="sessionId">L'ID de la session à rejoindre.</param>
    /// <param name="player">L'objet Player représentant le joueur rejoignant la session.</param>
    /// <returns>Un résultat Ok si le joueur rejoint avec succès, ou un résultat BadRequest/NotFound en cas de problème.</returns>
    [HttpPost("{sessionId}/join")]
    public async Task<ActionResult> JoinSession(string sessionId, [FromBody] Player player)
    {
        ActionResult result = null; // Déclarer une variable de résultat

        if (player == null || string.IsNullOrEmpty(player.PlayerId))
        {
            result = BadRequest("Informations du joueur invalides.");
        }
        else
        {
            GameSession existingSession = _sessionService.GetSessionById(sessionId);
            if (existingSession == null)
            {
                result = NotFound($"La session avec l'ID {sessionId} n'a pas été trouvée.");
            }
            else
            {
                bool playerAlreadyInSession = existingSession.Players.Exists(p => p.PlayerId == player.PlayerId);
                if (playerAlreadyInSession)
                {
                    result = BadRequest("Le joueur est déjà dans la session.");
                }
                else
                {
                    existingSession.Players.Add(player);
                    _logger.LogInformation($"Player {player.Name} added to session {sessionId}.");
                    
                    await _hubContext.Clients.Group(sessionId).SendAsync("PlayerJoined", player);
                    result = Ok($"Le joueur {player.Name} a rejoint la session {sessionId}.");
                }
            }
        }
        return result;
    }

    /// <summary>
    /// Récupère toutes les sessions de jeu actives.
    /// </summary>
    /// <returns>Une liste de toutes les sessions de jeu en cours.</returns>
    [HttpGet("all")]
    public ActionResult<List<GameSession>> GetAllSessions()
    {
        return Ok(_sessionService.GetAllSessions());
    }

    /// <summary>
    /// Récupère une session de jeu spécifique par son ID.
    /// </summary>
    /// <param name="sessionId">L'ID de la session de jeu à récupérer.</param>
    /// <returns>La session de jeu si elle est trouvée, ou un résultat NotFound si elle n'existe pas.</returns>
    [HttpGet("{sessionId}")]
    public ActionResult<GameSession> GetSessionById(string sessionId)
    {
        GameSession existingSession = _sessionService.GetSessionById(sessionId);
        ActionResult<GameSession> result;

        if (existingSession == null)
        {
            result = NotFound($"La session avec l'ID {sessionId} n'a pas été trouvée.");
        }
        else
        {
            result = Ok(existingSession);
        }

        return result;
    }

    /// <summary>
    /// Supprime une session de jeu par son ID.
    /// </summary>
    /// <param name="sessionId">L'ID de la session à supprimer.</param>
    /// <returns>Un résultat Ok si la session est supprimée, ou un résultat BadRequest si elle n'existe pas.</returns>
    [HttpDelete("{sessionId}")]
    public ActionResult destructiondeSession(string sessionId)
    {
        bool sessionDeleted = _sessionService.RemoveSession(sessionId);
        ActionResult result;

        if (!sessionDeleted)
        {
            result = BadRequest("La session n'existe pas ou n'a pas pu être supprimée.");
        }
        else
        {
            _codeGenerator.InvalidateCode(int.Parse(sessionId));
            result = Ok("Session détruite");
        }

        return result;
    }

    /// <summary>
    /// Récupère tous les joueurs d'une session de jeu spécifique.
    /// </summary>
    /// <param name="sessionId">L'ID de la session de jeu.</param>
    /// <returns>Une liste de joueurs dans la session spécifiée, ou un résultat NotFound si la session n'existe pas.</returns>
    [HttpGet("{sessionId}/players")]
    public ActionResult<List<Player>> GetAllPlayersFromSession(string sessionId)
    {
        var gameSession = _sessionService.GetSessionById(sessionId);
        ActionResult<List<Player>> result;

        if (gameSession == null)
        {
            result = NotFound($"La session avec l'ID {sessionId} n'a pas été trouvée.");
        }
        else
        {
            result = Ok(gameSession.Players);
        }

        return result;
    }

    /// <summary>
    /// Force la synchronisation de l'état de la session.
    /// </summary>
    /// <param name="sessionId">l'ID de la session à synchroniser</param>
    /// <returns>L'état actuel de la session ou un message d'erreur</returns>
    [HttpGet("{sessionId}/forceSync")]
    public ActionResult<GameSession> ForceSync(string sessionId)
    {
        _logger.LogInformation($"Force sync requested for session {sessionId}.");
        GameSession existingSession = _sessionService.GetSessionById(sessionId);
        ActionResult<GameSession> result;

        if (existingSession == null)
        {
            _logger.LogWarning($"Session {sessionId} not found for force sync.");
            result = NotFound($"La session avec l'ID {sessionId} n'a pas été trouvée.");
        }
        else
        {
            _logger.LogInformation($"Force sync returning session state for {sessionId}.");
            result = Ok(existingSession);
        }

        return result;
    }

    /// <summary>
    /// Supprime un joueur d'une session existante.
    /// </summary>
    /// <param name="sessionId">ID de la session.</param>
    /// <param name="playerId">ID du joueur à retirer.</param>
    /// <returns>Un message indiquant le résultat de l'opération.</returns>
    [HttpDelete("{sessionId}/player/{playerId}/remove")]
    public async Task<ActionResult> RemovePlayerFromSession(string sessionId, string playerId)
    {
        _logger.LogInformation($"Removing player {playerId} from session {sessionId}.");

        // Vérifier si la session existe
        GameSession existingSession = _sessionService.GetSessionById(sessionId);
        if (existingSession == null)
        {
            _logger.LogWarning($"Session {sessionId} not found.");
            return NotFound($"La session avec l'ID {sessionId} n'a pas été trouvée.");
        }

        // Vérifier si le joueur est dans la session
        Player playerToRemove = existingSession.Players.FirstOrDefault(p => p.PlayerId == playerId);
        if (playerToRemove == null)
        {
            _logger.LogWarning($"Player {playerId} not found in session {sessionId}.");
            return NotFound($"Le joueur avec l'ID {playerId} n'est pas présent dans la session.");
        }

        // Si l'hôte est supprimé, supprimer la session
        if (existingSession.Players[0].PlayerId == playerId)
        {
            _sessionService.RemoveSession(sessionId);
            _logger.LogInformation($"Session {sessionId} deleted because the host was removed.");
            await _hubContext.Clients.Group(sessionId).SendAsync("SessionDeleted", sessionId);
            return Ok($"La session {sessionId} a été supprimée car l'hôte a été retiré.");
        }

        // Retirer le joueur
        existingSession.Players.Remove(playerToRemove);
        _sessionService.UpdateSession(existingSession);

        _logger.LogInformation($"Player {playerId} removed from session {sessionId}.");
        await _hubContext.Clients.Group(sessionId).SendAsync("PlayerRemoved", playerToRemove);
        return Ok($"Le joueur {playerToRemove.Name} a été retiré de la session {sessionId}.");
    }
}
