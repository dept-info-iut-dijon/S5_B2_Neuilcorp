using API7D.DATA;
using API7D.objet;
using Xunit;

namespace TestsUnitaires.TestsMetier
{
    public class DifferanceCheckerTests
    {
        private readonly DifferanceCheckerDATA _differanceChecker;

        public DifferanceCheckerTests()
        {
            _differanceChecker = new DifferanceCheckerDATA();
        }

        [Fact]
        public void GetListDifferanceCoordinates_ReturnsCorrectNumberOfCoordinates()
        {
            var result = _differanceChecker.GetListDifferanceCoordinates();

            Assert.NotNull(result);
            Assert.Equal(8, result.Count);
        }

        [Fact]
        public void GetListDifferanceCoordinates_ContainsExpectedCoordinates()
        {
            var result = _differanceChecker.GetListDifferanceCoordinates();

            Assert.Contains(result, coord => coord.X == 128 && coord.Y == 138);
            Assert.Contains(result, coord => coord.X == 341 && coord.Y == 128);
            Assert.Contains(result, coord => coord.X == 196 && coord.Y == 424);
            Assert.Contains(result, coord => coord.X == 577 && coord.Y == 348);
            Assert.Contains(result, coord => coord.X == 503 && coord.Y == 656);
            Assert.Contains(result, coord => coord.X == 527 && coord.Y == 492);
            Assert.Contains(result, coord => coord.X == 423 && coord.Y == 765);
            Assert.Contains(result, coord => coord.X == 338 && coord.Y == 870);
        }
    }
}
