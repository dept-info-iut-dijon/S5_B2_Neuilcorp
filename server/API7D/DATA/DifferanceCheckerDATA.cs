using System.Collections.Generic;
using Microsoft.Data.Sqlite;
using API7D.objet;

namespace API7D.DATA
{
    public class DifferanceCheckerDATA : IDifferanceCheckerDATA
    {
        private readonly string _connectionString;

        public DifferanceCheckerDATA()
        {
            // Connexion à une base de données SQLite locale
            _connectionString = "Data Source=./Database/DataBase_Image.db";
        }

        /// <summary>
        /// Récupère la liste des coordonnées des différences pour un ID donné.
        /// </summary>
        /// <param name="id">ID de la paire d'images</param>
        /// <returns>Liste des coordonnées des différences</returns>
        public List<Coordonnees> GetListDifferanceCoordinatesFromId(int id)
        {
            var differanceCoordinates = new List<Coordonnees>();

            using (var connection = new SqliteConnection(_connectionString))
            {
                connection.Open();

                string query = @"
                    SELECT X, Y FROM Difference WHERE PairID = @PairID";

                using (var command = new SqliteCommand(query, connection))
                {
                    command.Parameters.AddWithValue("@PairID", id);

                    using (var reader = command.ExecuteReader())
                    {
                        while (reader.Read())
                        {
                            int x = reader.GetInt32(0);
                            int y = reader.GetInt32(1);
                            differanceCoordinates.Add(new Coordonnees(x, y));
                        }
                    }
                }
            }

            return differanceCoordinates;
        }

        /// <summary>
        /// Récupère toutes les différences pour toutes les paires d'images.
        /// </summary>
        /// <returns>Dictionnaire avec l'ID de la paire comme clé et la liste de coordonnées comme valeur</returns>
        public Dictionary<int, List<Coordonnees>> getAllDifferance()
        {
            var allDifferances = new Dictionary<int, List<Coordonnees>>();

            using (var connection = new SqliteConnection(_connectionString))
            {
                connection.Open();

                string query = @"SELECT PairID, X, Y FROM Difference";

                using (var command = new SqliteCommand(query, connection))
                {
                    using (var reader = command.ExecuteReader())
                    {
                        while (reader.Read())
                        {
                            int pairId = reader.GetInt32(0);
                            int x = reader.GetInt32(1);
                            int y = reader.GetInt32(2);

                            if (!allDifferances.ContainsKey(pairId))
                            {
                                allDifferances[pairId] = new List<Coordonnees>();
                            }

                            allDifferances[pairId].Add(new Coordonnees(x, y));
                        }
                    }
                }
            }

            return allDifferances;
        }
    }
}
