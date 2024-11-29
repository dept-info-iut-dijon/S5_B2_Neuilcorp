using System;
using System.Data;
using System.IO;
using System.Collections.Generic;
using API7D.objet;
using Microsoft.Data.Sqlite;
using API7D.Metier;

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
            // Connexion à une base de données SQLite locale
            _connectionString = "Data Source=./Database/DataBase_Image.db";
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
        public void SetImagesDATA(ImageDifference image)
        {
            /*using (var connection = new SqliteConnection(_connectionString))
            {
                connection.Open();

                string query = "INSERT INTO images (ImageID, ImagePaire, ImageLink) VALUES (@ImageID, @ImagePaire, @ImageLink)";
                using (var command = new SqliteCommand(query, connection))
                {
                    command.Parameters.AddWithValue("@ImageID", image.ImageId);
                    command.Parameters.AddWithValue("@ImagePaire", image.ImagePaire);
                    command.Parameters.AddWithValue("@ImageLink", image.ImageLink);

                    command.ExecuteNonQuery();
                }
            }*/
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
