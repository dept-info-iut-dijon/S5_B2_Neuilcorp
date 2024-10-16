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

        public int GenerateUniqueCode()
        {
            int code;

            // Continue generating until we find a unique code
            do
            {
                code = random.Next(100000, 999999); // Génère un code à 6 chiffres
            } while (generatedCodes.Contains(code));

            generatedCodes.Add(code); // Ajoute le code à l'ensemble des codes générés
            return code;
        }

        public void InvalidateCode(int code)
        {
            // Permet de libérer un code s'il n'est plus utilisé
            generatedCodes.Remove(code);
        }
    }

}
