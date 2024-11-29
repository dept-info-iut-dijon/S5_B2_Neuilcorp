using API7D.Metier;
using Xunit;

namespace TestsUnitaires.TestsMetier
{
    public class SessionCodeGeneratorTests
    {

        /// <summary>
        /// Teste que la méthode GenerateUniqueCode retourne un code à six chiffres.
        /// </summary>
        [Fact]
        public void GenerateUniqueCode_ReturnsSixDigitCode()
        {
            var generator = new SessionCodeGenerator();
            int code = generator.GenerateUniqueCode();
            Assert.InRange(code, 100000, 999999);
        }

        /// <summary>
        /// Teste que la méthode GenerateUniqueCode retourne des codes uniques.
        /// </summary>
        [Fact]
        public void GenerateUniqueCode_ReturnsUniqueCodes()
        {
            var generator = new SessionCodeGenerator();
            var codes = new HashSet<int>();

            for (int i = 0; i < 100; i++)
            {
                int code = generator.GenerateUniqueCode();
                codes.Add(code);
            }

            Assert.Equal(100, codes.Count);
        }

        /// <summary>
        /// Teste que la méthode InvalidateCode permet de générer un nouveau code différent après l'invalidation.
        /// </summary>
        [Fact]
        public void InvalidateCode_AllowsGeneratingSameCodeAgain()
        {
            var generator = new SessionCodeGenerator();
            int code = generator.GenerateUniqueCode();
            generator.InvalidateCode(code);
            int newCode = generator.GenerateUniqueCode();
            Assert.NotEqual(code, newCode);
        }


        /// <summary>
        /// Teste que la méthode GenerateUniqueCode ne génère pas un code invalidé.
        /// </summary>
        [Fact]
        public void GenerateUniqueCode_DoesNotGenerateInvalidatedCode()
        {
            var generator = new SessionCodeGenerator();
            int code = generator.GenerateUniqueCode();
            generator.InvalidateCode(code);
            bool stillContains = generator.GenerateUniqueCode() == code;
            Assert.False(stillContains);
        }
    }
}
