namespace API7D.objet
{
    public class Player
    {
        private string playerId;
        private string name;
        private bool isReady;
        public string PlayerId 
        {
            get {return playerId;} 
            set {playerId = value;}
        }
        public string Name
        {
            get { return Name; }
            set { Name = value; }
        }
        public bool IsReady
        {
            get { return IsReady; }
            set { IsReady = value; }
        }

        /// <summary>
        /// ce constructeur n'est ici que pour deserialiser les JSON
        /// </summary>
        public Player() { }

        /// <summary>
        /// constructeur de player
        /// </summary>
        /// <param name="playerId">Id du joueur</param>
        /// <param name="name">nom du joueur</param>
        public Player(string playerId, string name)
        {
            PlayerId = playerId;
            Name = name;
            IsReady = false;
        }
    }
}
