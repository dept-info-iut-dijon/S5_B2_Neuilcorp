using Xunit;
using Moq;
using API7D.Controllers;
using API7D.Metier;
using API7D.objet;
using Microsoft.AspNetCore.Mvc;

namespace TestsUnitaires.TestsControllers
{
    public class DifferenceControllerTests
    {
        [Fact]
        public void CheckDifference_Found()
        {
            var mockChecker = new Mock<IDifferanceChecker>();
            mockChecker.Setup(c => c.IsWithinDifference(It.IsAny<Coordonnees>())).Returns(true);
            var controller = new DifferanceController();

            var coordonnees = new Coordonnees(128, 138);

            var result = controller.CheckDifference(coordonnees);

            var okResult = Assert.IsType<OkObjectResult>(result);
            Assert.True((bool)okResult.Value);
        }

        [Fact]
        public void CheckDifference_ReturnsBadRequest()
        {
            var mockChecker = new Mock<IDifferanceChecker>();
            mockChecker.Setup(c => c.IsWithinDifference(It.IsAny<Coordonnees>())).Throws(new Exception());
            var controller = new DifferanceController(mockChecker.Object);

            var coordonnees = new Coordonnees(128, 138);

            var result = controller.CheckDifference(coordonnees);

            var badRequestResult = Assert.IsType<BadRequestObjectResult>(result);
            Assert.Equal("Erreur dans le traitement des coordonnées", badRequestResult.Value);
        }

    }
}
