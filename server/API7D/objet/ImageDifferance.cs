namespace API7D.objet
{
    /// <summary>
    /// Gère les différences entre les images en stockant les coordonnées des différences pour chaque image.
    /// </summary>
    public class ImageDifference
    {
        private readonly Dictionary<int, List<Coordonnees>> differenceImage = new Dictionary<int, List<Coordonnees>>();

        /// <summary>
        /// Initialise une nouvelle instance de la classe ImageDifference.
        /// </summary>
        public ImageDifference()
        {
            differenceImage = new Dictionary<int, List<Coordonnees>>();
        }

        /// <summary>
        /// Initialise une nouvelle instance de la classe ImageDifference avec une liste de coordonnées initiale.
        /// </summary>
        /// <param name="coordinates">Liste de coordonnées représentant les différences dans l'image</param>
        /// <param name="imageId">Identifiant de l'image à laquelle ces différences sont associées</param>
        /// <exception cref="ArgumentException">Si l'imageId existe déjà</exception>
        public ImageDifference(List<Coordonnees> coordinates, int imageId)
        {
            differenceImage = new Dictionary<int, List<Coordonnees>>();
            AddDifference(imageId, coordinates);
        }

        /// <summary>
        /// Obtient les identifiants d'image disponibles.
        /// </summary>
        /// <returns>Collection des identifiants d'images disponibles</returns>
        public IEnumerable<int> GetImageIds()
        {
            return differenceImage.Keys;
        }

        /// <summary>
        /// Ajoute une nouvelle différence d'image.
        /// </summary>
        /// <param name="imageId">Identifiant de l'image.</param>
        /// <param name="coordinates">Liste des coordonnées représentant les différences pour cette image.</param>
        /// <exception cref="ArgumentException">Lance une exception si l'imageId existe déjà.</exception>
        public void AddDifference(int imageId, List<Coordonnees> coordinates)
        {
            if (differenceImage.ContainsKey(imageId))
            {
                throw new ArgumentException($"L'image avec l'identifiant {imageId} existe déjà.");
            }

            differenceImage.Add(imageId, coordinates);
        }

        /// <summary>
        /// Récupère les différences associées à un identifiant d'image.
        /// </summary>
        /// <param name="imageId">Identifiant de l'image.</param>
        /// <returns>Liste des coordonnées représentant les différences.</returns>
        /// <exception cref="KeyNotFoundException">Lance une exception si l'imageId n'existe pas.</exception>
        public List<Coordonnees> GetDifferences(int imageId)
        {
            if (!differenceImage.TryGetValue(imageId, out var coordinates))
            {
                throw new KeyNotFoundException($"Aucune différence trouvée pour l'image avec l'identifiant {imageId}.");
            }

            return coordinates;
        }

        /// <summary>
        /// Supprime les différences associées à un identifiant d'image.
        /// </summary>
        /// <param name="imageId">Identifiant de l'image à supprimer.</param>
        /// <returns>True si la suppression a réussi, False sinon.</returns>
        public bool RemoveDifference(int imageId)
        {
            return differenceImage.Remove(imageId);
        }
    }
}
