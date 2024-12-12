using API7D.objet;
using Xunit;

namespace TestsUnitaires.TestsObjet
{
    /// <summary>
    /// Tests unitaires pour la classe Player.
    /// </summary>
    public class PlayerTests
    {
        /// <summary>
        /// Vérifie que le constructeur avec paramètres initialise correctement un joueur.
        /// </summary>
        [Fact]
        public void Constructor_InitializesCorrectly_WithParameters()
        {
            var player = new Player("123", "John Doe");

            Assert.Equal("123", player.PlayerId);
            Assert.Equal("John Doe", player.Name);
        }

        [Fact]
        public void Constructor_InitializesCorrectly_WithoutParameters()
        {
            var player = new Player();

            Assert.Null(player.PlayerId);
            Assert.Null(player.Name);
        }

        [Fact]
        public void SetPlayerId_SetsCorrectly()
        {
            var player = new Player();
            player.PlayerId = "456";

            Assert.Equal("456", player.PlayerId);
        }

        [Fact]
        public void SetName_SetsCorrectly()
        {
            var player = new Player();
            player.Name = "Jane Doe";

            Assert.Equal("Jane Doe", player.Name);
        }
    }
}
