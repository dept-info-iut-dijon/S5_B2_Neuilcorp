using API7D.objet;

namespace API7D.DATA
{
    public class DifferanceCheckerDATA : IDifferanceCheckerDATA
    {

        /// <summary>
        /// une fausse base de donner a remplacer dans le futur
        /// </summary>
        /// <returns>retourne une lise de coordonées </returns>
        public List<Coordonnees> GetListDifferanceCoordinates()
        {
            List<Coordonnees> list = new List<Coordonnees>();
            // a remplacer par un fur appelle a la BDD
            Coordonnees x = new Coordonnees(128, 138);
            list.Add(x);
            x = new Coordonnees(341, 128);
            list.Add(x);
            x = new Coordonnees(196, 424);
            list.Add(x);
            x = new Coordonnees(577, 348);
            list.Add(x);
            x = new Coordonnees(503, 656);
            list.Add(x);
            x = new Coordonnees(527, 492);
            list.Add(x);
            x = new Coordonnees(423, 765);
            list.Add(x);
            x = new Coordonnees(338, 870);
            list.Add(x);
            return list;

        }
    }
}
