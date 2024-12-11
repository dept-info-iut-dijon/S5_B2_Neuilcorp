using System;
using System.Data;
using System.IO;
using System.Collections.Generic;
using API7D.objet;
using Microsoft.Data.Sqlite;
using API7D.Metier;
using API7D.Database;

namespace API7D.DATA
{
    public class ImageDATA : IImageDATA
    {
        private readonly string _connectionString;

        /// <summary>
        /// Initialise une nouvelle instance de ImageDATA avec une connexion SQLite.
        /// </summary>
        public ImageDATA()
        {
            _connectionString = Setting.DataBasePath;
        }

        /// <summary>
        /// Récupère le lien d'image correspondant à un ID donné.
        /// </summary>
        /// <param name="ID">L'ID de l'image à récupérer.</param>
        /// <returns>Le chemin d'accès à l'image.</returns>
        public string GetImagesDATA(int ID)
        {
            string imagePath = null;

            using (var connection = new SqliteConnection(_connectionString))
            {
                connection.Open();

                string query = "SELECT ImageLink FROM Image WHERE ImageID = @ID";
                using (var command = new SqliteCommand(query, connection))
                {
                    command.Parameters.AddWithValue("@ID", ID);

                    using (var reader = command.ExecuteReader())
                    {
                        if (reader.Read())
                        {
                            imagePath = reader.GetString(0); // Récupère la valeur de ImageLink
                        }
                    }
                }
            }
            return imagePath ?? throw new Exception("Image not found.");
        }


        /// <summary>
        /// Récupère une liste des images avec leurs données associées ( Id image, ID de la paire d'image)
        /// </summary>
        /// <returns>Une liste de tuple contenant les informations telles que : imageId, imagePairId, ImageLink</returns>
        public List<(int ImageId, int ImagePairId, string ImageLink)> GetAllImagesWithPairData()
        {
            var imagesWithPairs = new List<(int ImageId, int PairId, string ImageLink)>(); // Utilisation des noms correspondants

            using (var connection = new SqliteConnection(_connectionString))
            {
                connection.Open();

                string query = @"SELECT i.ImageID,id.PairID,i.ImageLink FROM Image i LEFT JOIN ImageDifference id ON i.ImageID = id.Image1ID OR i.ImageID = id.Image2ID";

                using (var command = new SqliteCommand(query, connection))
                {
                    using (var reader = command.ExecuteReader())
                    {
                        while (reader.Read())
                        {
                            imagesWithPairs.Add((
                                reader.GetInt32(0),  // ImageID
                                reader.IsDBNull(1) ? 0 : reader.GetInt32(1),  // PairID (null-safe)
                                reader.GetString(2)  // ImageLink
                            ));
                        }
                    }
                }
            }

            return imagesWithPairs;
        }




        /// <summary>
        /// Récupère une paire d'images à partir de l'ID de la paire spécifiée.
        /// </summary>
        /// <param name="imagePaireId">L'ID de la paire d'images.</param>
        /// <returns>Un tuple contenant les deux images sous forme de tableaux de bytes.</returns>
        public (byte[] Image1, byte[] Image2) GetImagePair(int pairId)
        {
            using (var db = new SqliteConnection(_connectionString))
            {
                db.Open();

                // Requête pour récupérer les deux images correspondant à la paire
                string query = @"
            SELECT 
                i.ImageLink 
            FROM 
                ImageDifference id
            INNER JOIN 
                Image i 
            ON 
                id.Image1ID = i.ImageID OR id.Image2ID = i.ImageID
            WHERE 
                id.PairID = @PairID";

                using (var command = new SqliteCommand(query, db))
                {
                    command.Parameters.AddWithValue("@PairID", pairId);

                    using (var reader = command.ExecuteReader())
                    {
                        var imageLinks = new List<string>();

                        while (reader.Read())
                        {
                            imageLinks.Add(reader.GetString(0)); // Récupère ImageLink
                        }

                        // Vérifier si la paire contient bien deux images
                        if (imageLinks.Count != 2)
                        {
                            throw new Exception("La paire d'images est incomplète.");
                        }

                        try
                        {
                            // Charger les images à partir des liens de fichiers
                            byte[] image1 = File.ReadAllBytes(imageLinks[0]);
                            byte[] image2 = File.ReadAllBytes(imageLinks[1]);

                            return (image1, image2);
                        }
                        catch (IOException ex)
                        {
                            throw new Exception("Erreur lors de la lecture des fichiers d'images.", ex);
                        }
                    }
                }
            }
        }


        /// <summary>
        /// Ajoute une nouvelle image dans la base de données.
        /// </summary>
        /// <param name="image">L'objet image contenant les informations de l'image.</param>
        public void SetImagesDATA(string path1, string path2, List<Coordonnees> difference)
        {
            // Validate inputs
            if (string.IsNullOrEmpty(path1) || string.IsNullOrEmpty(path2))
                throw new ArgumentException("Image paths cannot be null or empty");
            if (difference == null)
                throw new ArgumentNullException(nameof(difference));

            // Ensure connection string is valid
            if (string.IsNullOrEmpty(_connectionString))
                throw new InvalidOperationException("Connection string is not initialized");

            using (var connection = new SqliteConnection(_connectionString))
            {
                try
                {
                    // Ensure connection is closed before opening
                    if (connection.State != System.Data.ConnectionState.Closed)
                        connection.Close();

                    connection.Open();

                    using (var transaction = connection.BeginTransaction())
                    {
                        try
                        {
                            // Insert first image and get its ID
                            string query1 = "INSERT INTO Image (ImageLink) VALUES (@ImageLink1); SELECT last_insert_rowid();";
                            int image1ID;
                            using (var command1 = new SqliteCommand(query1, connection, transaction))
                            {
                                command1.Parameters.AddWithValue("@ImageLink1", path1);
                                image1ID = Convert.ToInt32(command1.ExecuteScalar());
                            }

                            // Insert second image and get its ID
                            string query2 = "INSERT INTO Image (ImageLink) VALUES (@ImageLink2); SELECT last_insert_rowid();";
                            int image2ID;
                            using (var command2 = new SqliteCommand(query2, connection, transaction))
                            {
                                command2.Parameters.AddWithValue("@ImageLink2", path2);
                                image2ID = Convert.ToInt32(command2.ExecuteScalar());
                            }

                            // Insert image pair and get pair ID
                            string query3 = "INSERT INTO ImageDifference (Image1ID, Image2ID) VALUES (@Image1ID, @Image2ID); SELECT last_insert_rowid();";
                            int pairId;
                            using (var command3 = new SqliteCommand(query3, connection, transaction))
                            {
                                command3.Parameters.AddWithValue("@Image1ID", image1ID);
                                command3.Parameters.AddWithValue("@Image2ID", image2ID);
                                pairId = Convert.ToInt32(command3.ExecuteScalar());
                            }

                            // Insert differences using a single command with parameters
                            string query4 = "INSERT INTO Difference (X, Y, PairID) VALUES (@X, @Y, @PairID)";
                            using (var command4 = new SqliteCommand(query4, connection, transaction))
                            {
                                command4.Parameters.Add("@X", SqliteType.Integer);
                                command4.Parameters.Add("@Y", SqliteType.Integer);
                                command4.Parameters.Add("@PairID", SqliteType.Integer);

                                foreach (var coord in difference)
                                {
                                    command4.Parameters["@X"].Value = coord.X;
                                    command4.Parameters["@Y"].Value = coord.Y;
                                    command4.Parameters["@PairID"].Value = pairId;
                                    command4.ExecuteNonQuery();
                                }
                            }

                            transaction.Commit();
                        }
                        catch (Exception ex)
                        {
                            transaction.Rollback();
                            throw new Exception($"Error during database transaction: {ex.Message}", ex);
                        }
                    }
                }
                catch (SqliteException ex)
                {
                    throw new Exception($"SQLite connection error: {ex.Message}", ex);
                }
                finally
                {
                    if (connection.State == System.Data.ConnectionState.Open)
                        connection.Close();
                }
            }
        }


        public List<string> GetAllImagesDATA()
        {
            var imagePath = new List<string>();

            using (var connection = new SqliteConnection(_connectionString))
            {
                connection.Open();

                string query = "SELECT ImageLink FROM Image";
                using (var command = new SqliteCommand(query, connection))
                {
                    using (var reader = command.ExecuteReader())
                    {
                        while (reader.Read())
                        {
                            imagePath.Add(reader.GetString(0)); // Récupère ImageLink
                        }
                    }
                }
            }

            return imagePath.Count > 0 ? imagePath : throw new Exception("Images not found.");
        }

    }
}
