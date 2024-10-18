namespace API7D.Metier;

using API7D.DATA;
using API7D.objet;
using System;
using System.Collections.Generic;

public class DifferanceChecker : IDifferanceChecker
{
    private List<Coordonnees> differences;
    private const int AcceptanceRadius = 20; //Rayon d'acceptation en pixels

    public DifferanceChecker()
    {
        IDifferanceCheckerDATA _data = new DifferanceCheckerDATA();
        this.differences = _data.GetListDifferanceCoordinates();
    }


    public bool IsWithinDifference(Coordonnees coordinate)
    {
        foreach (var difference in differences)
        {
            // distance euclidienne
            double distance = Math.Sqrt(Math.Pow(difference.X - coordinate.X, 2) + Math.Pow(difference.Y - coordinate.Y, 2));

            if (distance <= AcceptanceRadius)
            {
                return true;
            }
        }

        return false;
    }
}
