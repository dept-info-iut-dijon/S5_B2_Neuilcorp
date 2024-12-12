using System.Collections.Generic;

namespace API7D.objet
{

    /// <summary>
    /// Représente une session de jeu.
    /// </summary>
    public class GameSession
    {
        /// <summary>
        /// Obtient ou définit l'ID de session.
        /// </summary>
        public string SessionId { get; set; }

        /// <summary>
        /// Obtient ou définit la liste des joueurs.
        /// </summary>
        public List<Player> Players { get; set; }

        /// <summary>
        /// Obtient ou définit si le jeu est terminé.
        /// </summary>
        public bool GameCompleted { get; set; }

        /// <summary>
        /// Obtient ou définit l'état du minuteur du jeu
        /// </summary>
        public bool GameTimer { get; set; }

        /// <summary>
        /// Obtient ou définit l'ID de la paire d'images.
        /// </summary>
        public int ImagePairId { get; set; }

        /// <summary>
        /// Obtient ou définit un dictionnaire des sélections des joueurs avec les coordonnées x et y
        /// </summary>
        public Dictionary<string, (int x, int y)> PlayerSelections { get; set; }

        /// <summary>
        /// Obtient ou définit un dictionnaire des statuts de prêt des joueurs
        /// </summary>
        public Dictionary<string, bool> PlayerReadyStatus { get; set; }

        /// <summary>
        /// Constructeur par défaut de la session de jeu 
        /// </summary>
        public GameSession()
        {
            Players = new List<Player>();
            PlayerSelections = new Dictionary<string, (int x, int y)>();
            PlayerReadyStatus = new Dictionary<string, bool>();
        }

        /// <summary>
        /// vérifie si tous les joueurs sont prêt ou non
        /// </summary>
        /// <returns>Retourne true si tous les joueurs sont prêts, sinon false</returns>
        public bool AreAllPlayersReady()
        {
            return PlayerReadyStatus.Count == Players.Count && !PlayerReadyStatus.ContainsValue(false);
        }

        /// <summary>
        /// Vérifie si tous les joueurs ont sélectionné une différence
        /// </summary>
        /// <returns>Retourne true si tous les joueurs ont fait une sélection sinon false</returns>
        public bool HaveAllPlayersSelected()
        {
            return PlayerSelections.Count == Players.Count;
        }

        /// <summary>
        /// Vérifie si un joueur est l'hôte de la session.
        /// </summary>
        /// <param name="playerId">L'ID du joueur à vérifier</param>
        /// <returns>True si le joueur est l'hôte, sinon False</returns>
        public bool IsHost(string playerId)
        {
            return Players.Count > 0 && Players[0].PlayerId == playerId;
        }

        /// <summary>
        /// Vérifie si un joueur existe dans la session.
        /// </summary>
        /// <param name="playerId">L'ID du joueur à vérifier</param>
        /// <returns>True si le joueur existe dans la session, sinon False</returns>
        public bool ContainsPlayer(string playerId)
        {
            return Players.Any(p => p.PlayerId == playerId);
        }

    }
}
