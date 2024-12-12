using API7D.objet;

namespace API7D.DATA
{

    /// <summary>
    /// Interface définissant les méthodes pour gérer les coordonnées des différences.
    /// Permet de récupérer les coordonnées des différences pour une image donnée.
    /// </summary>
    public interface IDifferenceCheckerData
    {

        /// <summary>
        /// Récupère une liste de coordonnées de différences pour une image donnée.
        /// </summary>
        /// <param name="id">Identifiant de l'image</param>
        /// <returns>Une liste d'objets Coordonnees représentant les emplacements des différences</returns>
        public List<Coordonnees> GetListDifferanceCoordinatesFromId(int id);

        /// <summary>
        /// Récupère toutes les différences pour toutes les paires d'images.
        /// </summary>
        /// <returns>Un dictionnaire où la clé est l'ID de la paire d'images et la valeur est la liste des coordonnées des différences</returns>
        public Dictionary<int, List<Coordonnees>> GetAllDifferences();

    }
}