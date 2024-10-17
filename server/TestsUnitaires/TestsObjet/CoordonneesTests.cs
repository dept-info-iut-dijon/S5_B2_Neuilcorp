using Xunit;

namespace TestsUnitaires.TestsObjet
{
    public class CoordonneesTests
    {
        [Fact]
        public void Constructor_InitializesCoordinatesCorrectly()
        {
            var coord = new Coordonnees(10, 20);
            Assert.Equal(10, coord.X);
            Assert.Equal(20, coord.Y);
        }

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
