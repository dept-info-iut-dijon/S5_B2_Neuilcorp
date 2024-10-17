using API7D.Metier;
using API7D.objet;
using Microsoft.AspNetCore.Mvc;
using System.Numerics;

[ApiController]
[Route("api/[controller]")]
public class GameSessionController : ControllerBase
{
    private static List<GameSession> _sessions = new List<GameSession>();
    private SessionCodeGenerator _CodeGenerator = new SessionCodeGenerator();

    // Crée une nouvelle session avec une liste de joueurs
    [HttpPost("CreateSession")]
    public ActionResult<GameSession> CreateSession([FromBody] GameSession gameSession)
    {
        if (gameSession == null || gameSession.Players == null || gameSession.Players.Count == 0)
        {
            return BadRequest("Les données de session ou les informations sur l'hôte sont invalides.");
        }

        // Génération d'un code de session unique

        // Affecter le code de session
        gameSession.SessionId = _CodeGenerator.GenerateUniqueCode().ToString();

        // S'assurer que la liste des joueurs ne contient que l'hôte au début
        Player host = gameSession.Players[0];
        gameSession.Players = new List<Player> { host };

        // Le jeu n'est pas terminé à la création
        gameSession.GameCompleted = false;

        // Timer de jeu (true ou false, mais ne fait rien pour l'instant)
        gameSession.GameTimer = false;
        _sessions.Add(gameSession);

        // Retourner la session de jeu créée
        return Ok(gameSession);
    }


    // Permet à un joueur de rejoindre une session en cours
    [HttpPost("{sessionId}/join")]
    public ActionResult JoinSession(string sessionId, [FromBody] Player player)
    {
        // Vérifier si le joueur est bien passé en paramètre
        if (player == null || string.IsNullOrEmpty(player.PlayerId))
        {
            return BadRequest("Informations du joueur invalides.");
        }

        // Chercher la session de jeu correspondante à l'ID
        GameSession existingSession = FindSessionById(sessionId);
        if (existingSession == null)
        {
            return NotFound($"La session avec l'ID {sessionId} n'a pas été trouvée.");
        }

        // Vérifier si le joueur est déjà dans la session
        bool playerAlreadyInSession = existingSession.Players.Any(p => p.PlayerId == player.PlayerId);
        if (playerAlreadyInSession)
        {
            return BadRequest("Le joueur est déjà dans la session.");
        }

        // Ajouter le joueur à la session
        existingSession.Players.Add(player);

        // Retourner un message de succès
        return Ok($"Le joueur {player.Name} a rejoint la session {sessionId}.");
    }

    // Méthode fict ive pour retrouver la session de jeu par son ID
    private GameSession FindSessionById(string sessionId)
    {
        var gameSession = _sessions.FirstOrDefault(p => p.SessionId == sessionId);
        if (gameSession == null)
        {
            throw new Exception("session does not exist");
        }
        return gameSession;
    }


    // Récupère toutes les sessions en cours
    [HttpGet("all")]
    public ActionResult<List<GameSession>> GetAllSessions()
    {
        return Ok(_sessions);
    }
    // Récupère une session de jeu par son ID
    [HttpGet("{sessionId}")]
    public ActionResult<GameSession> GetSessionById(string sessionId)
    {
        // Chercher la session de jeu correspondante à l'ID
        GameSession existingSession = FindSessionById(sessionId);

        if (existingSession == null)
        {
            return NotFound($"La session avec l'ID {sessionId} n'a pas été trouvée.");
        }

        // Retourner la session de jeu trouvée
        return Ok(existingSession);
    }

}
