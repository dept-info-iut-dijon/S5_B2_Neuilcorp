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
            x = new Coordinate(341, 128);
            list.Add(x);
            x = new Coordinate(196, 424);
            list.Add(x);
            x = new Coordinate(577, 348);
            list.Add(x);
            x = new Coordinate(503, 656);
            list.Add(x);

            return list;

        }
    }
}
