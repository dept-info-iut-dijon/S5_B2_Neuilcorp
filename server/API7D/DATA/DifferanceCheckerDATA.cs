using API7D.objet;

namespace API7D.DATA
{
    public class DifferanceCheckerDATA : IDifferanceCheckerDATA
    {
        public List<Coordinate> GetListDifferanceCoordinates()
        {
            List<Coordinate> list = new List<Coordinate>();
            // a remplacer par un fur appelle a la BDD
            Coordinate x = new Coordinate(128, 138);
            list.Add(x);
            return list;

        }
    }
}
