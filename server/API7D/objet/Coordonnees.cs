namespace API7D.objet
{
    /// <summary>
    /// Classe représentant les coordonnées X et Y.
    /// </summary>
    public class Coordonnees
    {
        /// <summary>
        /// Obtient ou définit la coordonnée X.
        /// </summary>
        public int X { get; set; }

        /// <summary>
        /// Obtient ou définit la coordonnée Y.
        /// </summary>
        public int Y { get; set; }

        /// <summary>
        /// Initialise une nouvelle instance de la classe Coordonnees.
        /// </summary>
        /// <param name="x">La coordonnée X</param>
        /// <param name="y">La coordonnée Y</param>
        public Coordonnees(int x, int y)
        {
            this.X = x;
            this.Y = y;
        }
    }
}
