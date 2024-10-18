namespace API7D.objet
{
    public class GameSession
    {
        private string sessionId;
        private List<Player> player;
        private bool gameCompleted;
        private bool gameTimer;

        public string SessionId 
        { 
            get { return sessionId; }
            set {  sessionId = value; } 
        }
        public List<Player> Players
        {
            get { return player; }
            set { player = value; }
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
        public GameSession() { }
    }
}
