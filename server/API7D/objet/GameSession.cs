using System.Collections.Generic;

namespace API7D.objet
{
    public class GameSession
    {
        private string sessionId;
        private List<Player> players;
        private bool gameCompleted;
        private bool gameTimer;
        private int imagePairId;
        private Dictionary<string, (int x, int y)> playerSelections;
        private Dictionary<string, bool> playerReadyStatus;

        public string SessionId
        {
            get { return sessionId; }
            set { sessionId = value; }
        }

        public List<Player> Players
        {
            get { return players; }
            set { players = value; }
        }

        public bool GameCompleted
        {
            get { return gameCompleted; }
            set { gameCompleted = value; }
        }

        public bool GameTimer
        {
            get { return gameTimer; }
            set { gameTimer = value; }
        }

        // Identifiant de la paire d’images choisi par l’hôte
        public int ImagePairId
        {
            get { return imagePairId; }
            set { imagePairId = value; }
        }

        // Dictionnaire pour stocker les sélections des joueurs
        public Dictionary<string, (int x, int y)> PlayerSelections
        {
            get { return playerSelections; }
            set { playerSelections = value; }
        }

        // Dictionnaire pour stocker le statut de préparation des joueurs
        public Dictionary<string, bool> PlayerReadyStatus
        {
            get { return playerReadyStatus; }
            set { playerReadyStatus = value; }
        }

        // Constructeur par défaut
        public GameSession()
        {
            players = new List<Player>();
            playerSelections = new Dictionary<string, (int x, int y)>();
            playerReadyStatus = new Dictionary<string, bool>();
        }

        // Méthode pour vérifier si tous les joueurs sont prêts
        public bool AreAllPlayersReady()
        {
            return playerReadyStatus.Count == players.Count && !playerReadyStatus.ContainsValue(false);
        }

        // Méthode pour vérifier si tous les joueurs ont fait une sélection
        public bool HaveAllPlayersSelected()
        {
            return playerSelections.Count == players.Count;
        }
    }
}
