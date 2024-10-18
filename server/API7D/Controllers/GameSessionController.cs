using API7D.Metier;
using API7D.objet;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.SignalR;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

[ApiController]
[Route("api/[controller]")]
public class GameSessionController : ControllerBase
{
    private static List<GameSession> _sessions = new List<GameSession>();
    private SessionCodeGenerator _CodeGenerator = new SessionCodeGenerator();
    private readonly IHubContext<GameSessionHub> _hubContext;

    public GameSessionController(IHubContext<GameSessionHub> hubContext)
    {
        _hubContext = hubContext;
    }

    /// <summary>
    /// Crée une nouvelle session de jeu avec les joueurs fournis.
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

        gameSession.SessionId = _CodeGenerator.GenerateUniqueCode().ToString();
        Player host = gameSession.Players[0];
        gameSession.Players = new List<Player> { host };
        gameSession.GameCompleted = false;
        gameSession.GameTimer = false;
        _sessions.Add(gameSession);

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
        if (player == null || string.IsNullOrEmpty(player.PlayerId))
        {
            return BadRequest("Informations du joueur invalides.");
        }

        GameSession existingSession = FindSessionById(sessionId);
        if (existingSession == null)
        {
            return NotFound($"La session avec l'ID {sessionId} n'a pas été trouvée.");
        }

        bool playerAlreadyInSession = existingSession.Players.Any(p => p.PlayerId == player.PlayerId);
        if (playerAlreadyInSession)
        {
            return BadRequest("Le joueur est déjà dans la session.");
        }

        existingSession.Players.Add(player);
        await _hubContext.Clients.Group(sessionId).SendAsync("PlayerJoined", player.Name);
        return Ok($"Le joueur {player.Name} a rejoint la session {sessionId}.");
    }

    /// <summary>
    /// Définit le statut de préparation d'un joueur dans une session de jeu.
    /// </summary>
    /// <param name="sessionId">L'ID de la session de jeu.</param>
    /// <param name="playerId">L'ID du joueur dont le statut de préparation est mis à jour.</param>
    /// <param name="isReady">Le nouveau statut de préparation du joueur.</param>
    /// <returns>Un résultat Ok si le statut est mis à jour, ou un résultat NotFound si la session ou le joueur est introuvable.</returns>
    [HttpPost("{sessionId}/player/{playerId}/ready")]
    public ActionResult SetPlayerReadyStatus(string sessionId, string playerId, [FromBody] bool isReady)
    {
        GameSession existingSession = FindSessionById(sessionId);
        if (existingSession == null)
        {
            return NotFound($"La session avec l'ID {sessionId} n'a pas été trouvée.");
        }

        Player player = existingSession.Players.FirstOrDefault(p => p.PlayerId == playerId);
        if (player == null)
        {
            return NotFound($"Le joueur avec l'ID {playerId} n'a pas été trouvé dans cette session.");
        }

        player.IsReady = isReady;
        return Ok($"Le statut de préparation du joueur {player.Name} a été mis à jour à {(isReady ? "prêt" : "pas prêt")}.");
    }

    /// <summary>
    /// Récupère toutes les sessions de jeu actives.
    /// </summary>
    /// <returns>Une liste de toutes les sessions de jeu en cours.</returns>
    [HttpGet("all")]
    public ActionResult<List<GameSession>> GetAllSessions()
    {
        return Ok(_sessions);
    }

    /// <summary>
    /// Récupère une session de jeu spécifique par son ID.
    /// </summary>
    /// <param name="sessionId">L'ID de la session de jeu à récupérer.</param>
    /// <returns>La session de jeu si elle est trouvée, ou un résultat NotFound si elle n'existe pas.</returns>
    [HttpGet("{sessionId}")]
    public ActionResult<GameSession> GetSessionById(string sessionId)
    {
        GameSession existingSession = FindSessionById(sessionId);
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
        var gameSession = _sessions.FirstOrDefault(s => s.SessionId == sessionId);
        if (gameSession == null)
        {
            return BadRequest();
        }

        _CodeGenerator.InvalidateCode(int.Parse(sessionId));
        _sessions.Remove(gameSession);
        return Ok("session destroyed");
    }

    /// <summary>
    /// Récupère tous les joueurs d'une session de jeu spécifique.
    /// </summary>
    /// <param name="sessionId">L'ID de la session de jeu.</param>
    /// <returns>Une liste de joueurs dans la session spécifiée, ou un résultat NotFound si la session n'existe pas.</returns>
    [HttpGet("{sessionId}/players")]
    public ActionResult<List<Player>> GetAllPlayersFromSession(string sessionId)
    {
        var gameSession = _sessions.FirstOrDefault(s => s.SessionId == sessionId);
        if (gameSession == null)
        {
            return NotFound($"La session avec l'ID {sessionId} n'a pas été trouvée.");
        }

        return Ok(gameSession.Players);
    }

    /// <summary>
    /// Trouve une session de jeu par son ID.
    /// </summary>
    /// <param name="sessionId">L'ID de la session de jeu à trouver.</param>
    /// <returns>La session de jeu si elle est trouvée, ou null si elle n'existe pas.</returns>
    private GameSession FindSessionById(string sessionId)
    {
        return _sessions.FirstOrDefault(p => p.SessionId == sessionId);
    }
}
