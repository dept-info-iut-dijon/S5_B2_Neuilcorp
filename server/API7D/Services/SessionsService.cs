using API7D.objet;
using System.Collections.Generic;
using System.Linq;

namespace API7D.Services
{
    public class SessionService
    {
        private readonly List<GameSession> _sessions = new List<GameSession>();

        /// <summary>
        /// Ajoute une nouvelle session de jeu.
        /// </summary>
        /// <param name="gameSession">La session de jeu à ajouter.</param>
        public void AddSession(GameSession gameSession)
        {
            _sessions.Add(gameSession);
        }

        /// <summary>
        /// Récupère toutes les sessions de jeu.
        /// </summary>
        /// <returns>Une liste de toutes les sessions de jeu.</returns>
        public List<GameSession> GetAllSessions()
        {
            return _sessions;
        }

        /// <summary>
        /// Récupère une session par son ID.
        /// </summary>
        /// <param name="sessionId">L'ID de la session.</param>
        /// <returns>La session correspondante ou null si elle n'existe pas.</returns>
        public GameSession GetSessionById(string sessionId)
        {
            return _sessions.FirstOrDefault(s => s.SessionId == sessionId);
        }

        /// <summary>
        /// Supprime une session de jeu par son ID.
        /// </summary>
        /// <param name="sessionId">L'ID de la session à supprimer.</param>
        /// <returns>True si la session est supprimée, sinon False.</returns>
        public bool RemoveSession(string sessionId)
        {
            var session = GetSessionById(sessionId);
            if (session != null)
            {
                _sessions.Remove(session);
                return true;
            }
            return false;
        }
    }
}
