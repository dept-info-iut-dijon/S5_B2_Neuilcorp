using Xunit;
using Moq;
using Microsoft.AspNetCore.Mvc;
using API7D.Controllers;
using API7D.objet;
using System.Collections.Generic;
using API7D.Metier;
using API7D.Services;
using Microsoft.AspNetCore.SignalR;

namespace TestsUnitaires.TestsControllers
{
    public class GameSessionControllerTests
    {
        private readonly GameSessionController _controller;
        private readonly Mock<IHubContext<GameSessionHub>> _mockHubContext;
        private readonly Mock<SessionService> _mockSessionService;
        public GameSessionControllerTests()
        {
            _mockHubContext = new Mock<IHubContext<GameSessionHub>>();
            _mockSessionService = new Mock<SessionService>();
            _controller = new GameSessionController(_mockHubContext.Object, _mockSessionService.Object);
        }

        [Fact]
        public void CreateSession_ReturnsBadRequest_WhenSessionIsNull()
        {
            var result = _controller.CreateSession(null);

            var badRequestResult = Assert.IsType<BadRequestObjectResult>(result.Result);
            Assert.Equal("Les données de session ou les informations sur l'hôte sont invalides.", badRequestResult.Value);
        }

        [Fact]
        public void CreateSession_ReturnsBadRequest_WhenPlayersListIsEmpty()
        {
            var session = new GameSession { Players = new List<Player>() };
            var result = _controller.CreateSession(session);

            var badRequestResult = Assert.IsType<BadRequestObjectResult>(result.Result);
            Assert.Equal("Les données de session ou les informations sur l'hôte sont invalides.", badRequestResult.Value);
        }

        [Fact]
        public void CreateSession_ReturnsOk_WithValidGameSession()
        {
            var session = new GameSession
            {
                Players = new List<Player> { new Player { PlayerId = "1", Name = "Host" } }
            };
            var result = _controller.CreateSession(session);

            var okResult = Assert.IsType<OkObjectResult>(result.Result);
            var createdSession = Assert.IsType<GameSession>(okResult.Value);
            Assert.NotNull(createdSession.SessionId);
            Assert.Single(createdSession.Players);
        }

        [Fact]
        public void GetAllSessions_ReturnsOk_WithSessionsList()
        {
            var session = new GameSession
            {
                Players = new List<Player> { new Player { PlayerId = "1", Name = "Host" } }
            };
            _controller.CreateSession(session);
            var result = _controller.GetAllSessions();

            var okResult = Assert.IsType<OkObjectResult>(result.Result);
            var sessions = Assert.IsType<List<GameSession>>(okResult.Value);
            Assert.NotEmpty(sessions);
        }

        [Fact]
        public void GetSessionById_ReturnsOk_WhenSessionExists()
        {
            var session = new GameSession
            {
                SessionId = "123456",
                Players = new List<Player> { new Player { PlayerId = "1", Name = "Host" } }
            };

            _controller.CreateSession(session);

            var result = _controller.GetSessionById(session.SessionId);

            var okResult = Assert.IsType<OkObjectResult>(result.Result);
            var returnValue = Assert.IsType<GameSession>(okResult.Value);
            Assert.Equal(session.SessionId, returnValue.SessionId);
        }




        [Fact]
        public void GetSessionById_ReturnsNotFound_WhenSessionDoesNotExist()
        {
            var sessionId = "non_existing_session";

            var result = Assert.Throws<Exception>(() => _controller.GetSessionById(sessionId));

            Assert.Equal("session does not exist", result.Message);
        }


        [Fact]
        public void JoinSession_ReturnsBadRequest_WhenPlayerIsNull()
        {
            var result = _controller.JoinSession("123456", null);
            var badRequestResult = Assert.IsType<BadRequestObjectResult>(result);
            Assert.Equal("Informations du joueur invalides.", badRequestResult.Value);
        }

        [Fact]
        public void JoinSession_ReturnsBadRequest_WhenPlayerAlreadyInSession()
        {
            var session = new GameSession
            {
                SessionId = "123456",
                Players = new List<Player> { new Player { PlayerId = "1", Name = "Host" } }
            };
            _controller.CreateSession(session);
            var player = new Player { PlayerId = "1", Name = "Host" };

            var result = _controller.JoinSession(session.SessionId, player);

            var badRequestResult = Assert.IsType<BadRequestObjectResult>(result);
            Assert.Equal("Le joueur est déjà dans la session.", badRequestResult.Value);
        }


        [Fact]
        public void JoinSession_ReturnsOk_WhenPlayerJoinsSuccessfully()
        {
            var session = new GameSession
            {
                SessionId = "123456",
                Players = new List<Player> { new Player { PlayerId = "1", Name = "Host" } }
            };
            _controller.CreateSession(session);
            var player = new Player { PlayerId = "2", Name = "Player 2" };

            var result = _controller.JoinSession(session.SessionId, player);

            var okResult = Assert.IsType<OkObjectResult>(result);
            Assert.Equal($"Le joueur {player.Name} a rejoint la session {session.SessionId}.", okResult.Value);
        }
    }
}
