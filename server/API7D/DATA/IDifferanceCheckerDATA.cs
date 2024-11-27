using API7D.objet;

namespace API7D.DATA
{
    public interface IDifferanceCheckerDATA
    {
        public List<Coordonnees> GetListDifferanceCoordinatesFromId(int id);
        public Dictionary<int, List<Coordonnees>> getAllDifferance();

    }
}