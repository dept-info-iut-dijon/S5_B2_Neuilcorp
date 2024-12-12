using API7D.objet;
using Xunit;

namespace TestsUnitaires.TestsObjet
{
    /// <summary>
    /// Tests unitaires pour la classe GameSession.
    /// </summary>
    public class GameSessionTests
    {
        /// <summary>
        /// Vérifie que le constructeur initialise correctement une session de jeu.
        /// </summary>
        [Fact]
        public void Constructor_InitializesCorrectly()
        {
            var session = new GameSession
            {
                SessionId = "123456",
                Players = new List<Player> { new Player { PlayerId = "1", Name = "Host" } },
                GameCompleted = false,
                GameTimer = true
            };

            Assert.Equal("123456", session.SessionId);
            Assert.Single(session.Players);
            Assert.Equal("Host", session.Players[0].Name);
            Assert.False(session.GameCompleted);
            Assert.True(session.GameTimer);
        }

        /// <summary>
        /// Vérifie qu'un joueur peut être ajouté à une session.
        /// </summary>
        [Fact]
        public void AddPlayer_AddsPlayerToSession()
        {
            var session = new GameSession
            {
                SessionId = "123456",
                Players = new List<Player> { new Player { PlayerId = "1", Name = "Host" } }
            };

            var newPlayer = new Player { PlayerId = "2", Name = "Player2" };
            session.Players.Add(newPlayer);

            Assert.Equal(2, session.Players.Count);
            Assert.Contains(session.Players, p => p.PlayerId == "2" && p.Name == "Player2");
        }

        [Fact]
        public void RemovePlayer_RemovesPlayerFromSession()
        {
            var session = new GameSession
            {
                SessionId = "123456",
                Players = new List<Player> { new Player { PlayerId = "1", Name = "Host" } }
            };

            var playerToRemove = session.Players[0];
            session.Players.Remove(playerToRemove);

            Assert.Empty(session.Players);
        }

        [Fact]
        public void GameCompleted_IsSetCorrectly()
        {
            var session = new GameSession { GameCompleted = false };

            session.GameCompleted = true;

            Assert.True(session.GameCompleted);
        }

        [Fact]
        public void GameTimer_IsSetCorrectly()
        {
            var session = new GameSession { GameTimer = false };

            session.GameTimer = true;

            Assert.True(session.GameTimer);
        }
    }
}
