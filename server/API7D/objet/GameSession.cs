using System.Collections.Generic;

namespace API7D.objet
{

    /// <summary>
    /// Représente une session de jeu.
    /// </summary>
    public class GameSession
    {
        private string sessionId;
        private List<Player> players;
        private bool gameCompleted;
        private bool gameTimer;
        private int imagePairId;
        private Dictionary<string, (int x, int y)> playerSelections;
        private Dictionary<string, bool> playerReadyStatus;
        private List<Coordonnees> differenceTrouver;


        /// <summary>
        /// Obtient ou définit l'ID de session
        /// </summary>
        public string SessionId
        {
            get { return sessionId; }
            set { sessionId = value; }
        }

        /// <summary>
        /// Obtient ou définit la liste des joueurs
        /// </summary>
        public List<Player> Players
        {
            get { return players; }
            set { players = value; }
        }

        /// <summary>
        /// Obtient ou définit si le jeu est terminé
        /// </summary>
        public bool GameCompleted
        {
            get { return gameCompleted; }
            set { gameCompleted = value; }
        }

        /// <summary>
        /// Obtient ou définit l'état du minuteur du jeu
        /// </summary>
        public bool GameTimer
        {
            get { return gameTimer; }
            set { gameTimer = value; }
        }

        /// <summary>
        /// Obient ou définit l'ID de la paire d'images
        /// </summary>
        public int ImagePairId
        {
            get { return imagePairId; }
            set { imagePairId = value; }
        }

        /// <summary>
        /// Obtient ou définit un dictionnaire des sélections des joueurs avec les coordonnées x et y
        /// </summary>
        public Dictionary<string, (int x, int y)> PlayerSelections
        {
            get { return playerSelections; }
            set { playerSelections = value; }
        }

        /// <summary>
        /// Obtient ou définit un dictionnaire des statuts de prêt des joueurs
        /// </summary>
        public Dictionary<string, bool> PlayerReadyStatus
        {
            get { return playerReadyStatus; }
            set { playerReadyStatus = value; }
        }

        public List<Coordonnees> DifferenceTrouver
        {
            get { return differenceTrouver; }
            set { differenceTrouver = value; }
        }

        /// <summary>
        /// Constructeur par défaut de la session de jeu 
        /// </summary>
        public GameSession()
        {
            players = new List<Player>();
            playerSelections = new Dictionary<string, (int x, int y)>();
            playerReadyStatus = new Dictionary<string, bool>();
        }

        /// <summary>
        /// vérifie si tous les joueurs sont prêt ou non
        /// </summary>
        /// <returns>Retourne true si tous les joueurs sont prêts, sinon false</returns>
        public bool AreAllPlayersReady()
        {
            return playerReadyStatus.Count == players.Count && !playerReadyStatus.ContainsValue(false);
        }

        /// <summary>
        /// Vérifie si tous les joueurs ont sélectionné une différence
        /// </summary>
        /// <returns>Retourne true si tous les joueurs ont fait une sélection sinon false</returns>
        public bool HaveAllPlayersSelected()
        {
            return playerSelections.Count == players.Count;
        }

        // Méthode pour vérifier si un joueur est l'hôte de la session.
        public bool IsHost(string playerId)
        {
            return Players.Count > 0 && Players[0].PlayerId == playerId;
        }

        // Méthode pour vérifier si un joueur existe dans la session.
        public bool ContainsPlayer(string playerId)
        {
            return Players.Any(p => p.PlayerId == playerId);
        }

    }
}
