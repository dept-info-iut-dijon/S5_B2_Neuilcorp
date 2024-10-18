namespace API7D.objet
{
    public class Player
    {
        private string playerId;
        private string name;
        private bool isReady;

        public string PlayerId
        {
            get { return playerId; }
            set { playerId = value; }
        }
        public string Name
        {
            get { return name; }  // Utiliser la variable privée `name`
            set { name = value; }  // Utiliser la variable privée `name`
        }
        public bool IsReady
        {
            get { return isReady; }  // Utiliser la variable privée `isReady`
            set { isReady = value; }  // Utiliser la variable privée `isReady`
        }

        /// <summary>
        /// Ce constructeur n'est ici que pour désérialiser les JSON.
        /// </summary>
        public Player() { }

        /// <summary>
        /// Constructeur de player.
        /// </summary>
        /// <param name="playerId">Id du joueur</param>
        /// <param name="name">Nom du joueur</param>
        public Player(string playerId, string name)
        {
            PlayerId = playerId;
            Name = name;
            IsReady = false;
        }
    }
}
