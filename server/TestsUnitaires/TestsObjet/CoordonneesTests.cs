using Xunit;

namespace TestsUnitaires.TestsObjet
{
    /// <summary>
    /// Tests unitaires pour la classe Coordonnees.
    /// </summary>
    public class CoordonneesTests
    {
        /// <summary>
        /// Vérifie que le constructeur initialise correctement les coordonnées.
        /// </summary>
        [Fact]
        public void Constructor_InitializesCoordinatesCorrectly()
        {
            var coord = new Coordonnees(10, 20);
            Assert.Equal(10, coord.X);
            Assert.Equal(20, coord.Y);
        }

        /// <summary>
        /// Vérifie que les propriétés X et Y peuvent être modifiées correctement.
        /// </summary>
        [Fact]
        public void Properties_SetterWorksCorrectly()
        {
            var coord = new Coordonnees(0, 0);
            coord.X = 15;
            coord.Y = 25;
            Assert.Equal(15, coord.X);
            Assert.Equal(25, coord.Y);
        }
    }
}
