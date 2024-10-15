using API7D.objet;

namespace API7D.Metier
{
    public interface IDifferanceChecker
    {
        /// <summary>
        /// verifie la presence d'une differance a cette coordonnée
        /// </summary>
        /// <param name="x"> coordonée X </param>
        /// <param name="y"> coordonée Y </param>
        /// <returns></returns>
        public bool IsWithinDifference(Coordonnees coordinate);
    }
}
