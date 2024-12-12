using API7D.objet;
using API7D.Services;

namespace API7D.Metier
{
    public interface IDifferenceChecker
    {
        /// <summary>
        /// Vérifie de manière asynchrone si une différence existe aux coordonnées spécifiées.
        /// </summary>
        /// <param name="coordinate">Les coordonnées à vérifier</param>
        /// <param name="idImagePaire">Identifiant de la paire d'images</param>
        /// <param name="sessionId">Identifiant de la session</param>
        /// <param name="sessionService">Service de gestion des sessions</param>
        /// <param name="playerId">Identifiant du joueur</param>
        /// <returns>True si une différence est trouvée, False sinon</returns>
        /// <exception cref="ArgumentException">Si aucune différence n'est trouvée pour la paire d'images</exception>
        Task<bool> IsWithinDifferenceAsync(
            Coordonnees coordinate,
            int idImagePaire,
            string sessionId,
            SessionService sessionService,
            string playerId);
    }
}
