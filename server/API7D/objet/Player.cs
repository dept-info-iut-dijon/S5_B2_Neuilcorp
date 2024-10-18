﻿namespace API7D.objet
{
    public class Player
    {
        public string PlayerId { get; set; } // Identifiant unique du joueur
        public string Name { get; set; } // Nom du joueur
        public bool IsReady { get; set; } // Statut prêt/pas prêt

        // Constructeur sans paramètre requis pour la désérialisation JSON
        public Player() { }

        // Constructeur avec paramètres
        public Player(string playerId, string name)
        {
            PlayerId = playerId;
            Name = name;
            IsReady = false;
        }
    }
}
