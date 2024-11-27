namespace API7D.Metier
{
    /// <summary>
    /// Générateur d'ID unique pour les joueurs.
    /// </summary>
    public static class PlayerIdGenerator
    {
        public static string GeneratePlayerId()
        {
            return Guid.NewGuid().ToString();
        }
    }
}
