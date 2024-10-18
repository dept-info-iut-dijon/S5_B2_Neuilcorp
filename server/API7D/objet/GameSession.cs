namespace API7D.objet
{
    public class GameSession
    {
        public string SessionId { get; set; }
        public List<Player> Players { get; set; }
        public bool GameCompleted { get; set; }

        public bool GameTimer { get; set; }
        public GameSession() { }
    }
}
