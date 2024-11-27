using API7D.Metier;
using API7D.objet;
using API7D.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.SignalR;
using System.Collections.Generic;
using System.Threading.Tasks;

[ApiController]
[Route("api/[controller]")]
public class GameSessionController : ControllerBase
{
    private readonly SessionService _sessionService;
    private readonly IHubContext<GameSessionHub> _hubContext;
    private readonly SessionCodeGenerator _codeGenerator = new SessionCodeGenerator();
    private readonly ILogger<GameSessionController> _logger;

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
        if (gameSession == null || gameSession.Players == null || gameSession.Players.Count == 0)
        {
            return BadRequest("Les données de session ou les informations sur l'hôte sont invalides.");
        }

        gameSession.SessionId = _codeGenerator.GenerateUniqueCode().ToString();
        Player host = gameSession.Players[0];
        gameSession.Players = new List<Player> { host };
        gameSession.GameCompleted = false;
        gameSession.GameTimer = false;
        _sessionService.AddSession(gameSession);

        return Ok(gameSession);
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
        _logger.LogInformation($"Player {player?.Name ?? "Unknown"} is attempting to join session {sessionId}.");

        if (player == null || string.IsNullOrEmpty(player.PlayerId))
        {
            _logger.LogWarning($"Invalid player data for session {sessionId}.");
            return BadRequest("Informations du joueur invalides.");
        }

        GameSession existingSession = _sessionService.GetSessionById(sessionId);
        if (existingSession == null)
        {
            _logger.LogWarning($"Session {sessionId} not found.");
            return NotFound($"La session avec l'ID {sessionId} n'a pas été trouvée.");
        }

        bool playerAlreadyInSession = existingSession.Players.Exists(p => p.PlayerId == player.PlayerId);
        if (playerAlreadyInSession)
        {
            _logger.LogWarning($"Player {player.PlayerId} is already in session {sessionId}.");
            return BadRequest("Le joueur est déjà dans la session.");
        }

        existingSession.Players.Add(player);
        _logger.LogInformation($"Player {player.Name} added to session {sessionId}.");
        await _hubContext.Clients.Group(sessionId).SendAsync("SyncSessionState", existingSession);
        _logger.LogDebug($"SyncSessionState sent for session {sessionId} after player joined.");
        await _hubContext.Clients.Group(sessionId).SendAsync("PlayerJoined", player.Name);
        return Ok($"Le joueur {player.Name} a rejoint la session {sessionId}.");
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
        if (existingSession == null)
        {
            return NotFound($"La session avec l'ID {sessionId} n'a pas été trouvée.");
        }

        return Ok(existingSession);
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
        if (!sessionDeleted)
        {
            return BadRequest("La session n'existe pas ou n'a pas pu être supprimée.");
        }

        _codeGenerator.InvalidateCode(int.Parse(sessionId));
        return Ok("Session détruite");
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
        if (gameSession == null)
        {
            return NotFound($"La session avec l'ID {sessionId} n'a pas été trouvée.");
        }

        return Ok(gameSession.Players);
    }

    [HttpGet("{sessionId}/forceSync")]
    public ActionResult<GameSession> ForceSync(string sessionId)
    {
        _logger.LogInformation($"Force sync requested for session {sessionId}.");
        GameSession existingSession = _sessionService.GetSessionById(sessionId);
        if (existingSession == null)
        {
            _logger.LogWarning($"Session {sessionId} not found for force sync.");
            return NotFound($"La session avec l'ID {sessionId} n'a pas été trouvée.");
        }

        _logger.LogInformation($"Force sync returning session state for {sessionId}.");
        return Ok(existingSession);
    }
}
