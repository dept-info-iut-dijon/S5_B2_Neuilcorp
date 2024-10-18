using API7D.objet;
using Microsoft.AspNetCore.Mvc;
using System.Numerics;

[ApiController]
[Route("api/[controller]")]
public class GameSessionController : ControllerBase
{
    private static List<GameSession> _sessions = new List<GameSession>();

    // Crée une nouvelle session avec une liste de joueurs
    [HttpPost("create")]
    public ActionResult<GameSession> CreateSession([FromBody] List<Player> players)
    {
        var session = new GameSession
        {
            SessionId = Guid.NewGuid().ToString(),
            Players = players,
            PlayerImages = new List<ImageDifference>(), // Tu pourrais initialiser avec des images
            GameCompleted = false
        };

        _sessions.Add(session);
        return Ok(session);
    }

    // Permet à un joueur de rejoindre une session en cours
    [HttpPost("{sessionId}/join")]
    public ActionResult JoinSession(string sessionId, [FromBody] Player player)
    {
        var session = _sessions.FirstOrDefault(s => s.SessionId == sessionId);
        if (session == null)
        {
            return NotFound("Session introuvable.");
        }

        // Vérifie si le joueur est déjà dans la session
        if (session.Players.Any(p => p.PlayerId == player.PlayerId))
        {
            return BadRequest("Le joueur est déjà dans la session.");
        }

        session.Players.Add(player);
        return Ok($"Joueur {player.Name} ajouté à la session.");
    }

    // Récupère toutes les sessions en cours
    [HttpGet("all")]
    public ActionResult<List<GameSession>> GetAllSessions()
    {
        return Ok(_sessions);
    }

    // Récupère une session spécifique par son ID
    [HttpGet("{sessionId}")]
    public ActionResult<GameSession> GetSession(string sessionId)
    {
        var session = _sessions.FirstOrDefault(s => s.SessionId == sessionId);
        if (session == null)
        {
            return NotFound();
        }
        return Ok(session);
    }
}
