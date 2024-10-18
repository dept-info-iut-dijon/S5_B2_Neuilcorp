using System;
using System.Collections.Generic;

namespace API7D.Metier
{
    public class SessionCodeGenerator
    {
        private readonly HashSet<int> generatedCodes;
        private readonly Random random;

        public SessionCodeGenerator()
        {
            generatedCodes = new HashSet<int>(); // plus perfomant qu'une list
            random = new Random();
        }

        /// <summary>
        /// permet de genrer des code de session a 6 chiffre unique
        /// </summary>
        /// <returns></returns>
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
        /// supprime ce code des code generer 
        /// </summary>
        /// <param name="code"> le code</param>
        public void InvalidateCode(int code)
        {
            generatedCodes.Remove(code);
        }
    }

}
