using System;
using System.Collections.Generic;

namespace API7D.Metier
{
    /// <summary>
    /// Générateur de codes uniques pour les sessions de jeu.
    /// </summary>
    public class SessionCodeGenerator
    {
        private readonly HashSet<int> generatedCodes;
        private readonly Random random;

        /// <summary>
        /// Initialise une nouvelle instance du générateur de codes de session.
        /// </summary>
        public SessionCodeGenerator()
        {
            generatedCodes = new HashSet<int>();
            random = new Random();
        }

        /// <summary>
        /// Génère un code de session unique à 6 chiffres.
        /// </summary>
        /// <returns>Un code unique entre 100000 et 999999</returns>
        /// <remarks>Les codes générés sont stockés pour éviter les doublons</remarks>
        public int GenerateUniqueCode()
        {
            int code;

            do
            {
                code = random.Next(100000, 999999);
            } while (generatedCodes.Contains(code));

            generatedCodes.Add(code);
            return code;
        }

        /// <summary>
        /// Invalide un code de session en le retirant de la liste des codes générés.
        /// </summary>
        /// <param name="code">Le code à invalider</param>
        public void InvalidateCode(int code)
        {
            generatedCodes.Remove(code);
        }
    }

}
