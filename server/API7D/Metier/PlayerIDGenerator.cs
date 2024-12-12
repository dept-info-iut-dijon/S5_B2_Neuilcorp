namespace API7D.Metier
{
    /// <summary>
    /// Générateur d'ID unique pour les joueurs.
    /// </summary>
    public static class PlayerIdGenerator
    {

        /// <summary>
        /// Génère un ID unique pour un joueur.
        /// </summary>
        /// <returns>Un identifiant unique sous forme de chaîne de caractères</returns>
        public static string GeneratePlayerId()
        {
            return Guid.NewGuid().ToString();
        }
    }
}
