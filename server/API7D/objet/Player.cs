namespace API7D.objet
{
    /// <summary>
    /// Représente un joueur dans une session de jeu.
    /// </summary>
    public class Player
    {
        /// <summary>
        /// Obtient ou définit l'identifiant unique du joueur.
        /// </summary>
        public string PlayerId { get; set; }

        /// <summary>
        /// Obtient ou définit le nom du joueur.
        /// </summary>
        public string Name { get; set; }

        /// <summary>
        /// Obtient ou définit l'état de préparation du joueur.
        /// </summary>
        public bool IsReady { get; set; }

        /// <summary>
        /// Ce constructeur n'est ici que pour désérialiser les JSON.
        /// </summary>
        public Player() { }

        /// <summary>
        /// Initialise une nouvelle instance de la classe Player avec un ID et un nom.
        /// </summary>
        /// <param name="playerId">Identifiant unique du joueur</param>
        /// <param name="name">Nom du joueur</param>
        public Player(string playerId, string name)
        {
            PlayerId = playerId;
            Name = name;
            IsReady = false;
        }
    }
}
