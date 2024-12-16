using API7D.objet;
using API7D.Services;

namespace API7D.Metier
{
    public interface IDifferanceChecker
    {
        /// <summary>
        /// verifie la presence d'une differance a cette coordonnée
        /// 
        /// ASYNC dans le code
        /// 
        /// </summary>
        /// <param name="x"> coordonée X </param>
        /// <param name="y"> coordonée Y </param>
        /// <returns></returns>
        public Task<int> IsWithinDifferenceAsync(Coordonnees coordinate, int idImagePaire, string SessionID, SessionService _sessionService, string playerID);
    }
}
