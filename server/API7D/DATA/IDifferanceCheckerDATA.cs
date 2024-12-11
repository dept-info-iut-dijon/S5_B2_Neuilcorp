using API7D.objet;

namespace API7D.DATA
{

    /// <summary>
    /// Interface définissant les méthodes pour gérer les coordonnées des différences
    /// Permet de récuperer les coordonnées des différences pour une image donnée
    /// </summary>
    public interface IDifferanceCheckerDATA
    {

        /// <summary>
        /// Récupère une liste de coordonnées de différences pour une image donnée
        /// </summary>
        /// <param name="id">Identifiant de l'image</param>
        /// <returns>Une liste d'objet Coordonnees représentant les emplacements des différences</returns>
        public List<Coordonnees> GetListDifferanceCoordinatesFromId(int id);

        /// <summary>
        /// Récupère toutes les différences pour toutes les paires d'images.
        /// </summary>
        /// <returns>Un dictionnaire où chaque clé correspond à l'identifiant d'une image</returns>
        public Dictionary<int, List<Coordonnees>> getAllDifferance();

    }
}