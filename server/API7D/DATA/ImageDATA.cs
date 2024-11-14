using System;
using System.Data;
using System.IO;
using System.Collections.Generic;
using API7D.objet;
using Microsoft.Data.Sqlite;

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

                string query = "SELECT ImageLink FROM images WHERE ImageID = @ID";
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

        public List<string> GetAllImagesDATA()
        {
            List<string> imagePath = new List<string>();

            using (var connection = new SqliteConnection(_connectionString))
            {
                connection.Open();

                string query = "SELECT ImageLink FROM images";
                using (var command = new SqliteCommand(query, connection))
                {
                    using (var reader = command.ExecuteReader())
                    {
                        while (reader.Read())
                        {
                            string read = reader.GetString(0); // Colonne "ImageLink" est à l'index 0
                            imagePath.Add(read);
                        }
                    }
                }
            }

            return imagePath.Count > 0 ? imagePath : throw new Exception("Images not found.");
        }


        /// <summary>
        /// Récupère une paire d'images à partir de l'ID de la paire spécifiée.
        /// </summary>
        /// <param name="imagePaireId">L'ID de la paire d'images.</param>
        /// <returns>Un tuple contenant les deux images sous forme de tableaux de bytes.</returns>
        public (byte[] Image1, byte[] Image2) GetImagePair(int imagePaireId)
        {
            using (var db = new SqliteConnection(_connectionString))
            {
                db.Open();

                // Requête pour récupérer les liens d'images correspondant à la paire
                string query = "SELECT ImageID, ImageLink FROM images WHERE ImagePaire = @ImagePaire";
                using (var command = new SqliteCommand(query, db))
                {
                    command.Parameters.AddWithValue("@ImagePaire", imagePaireId);

                    using (var reader = command.ExecuteReader())
                    {
                        var images = new List<(int ImageID, string ImageLink)>();

                        while (reader.Read())
                        {
                            images.Add((reader.GetInt32(0), reader.GetString(1))); // Récupère ImageID et ImageLink
                        }

                        // Vérifier si la paire contient bien deux images
                        if (images.Count != 2)
                        {
                            throw new Exception("La paire d'images est incomplète.");
                        }

                        byte[] image1 = null;
                        byte[] image2 = null;

                        try
                        {
                            // Charger les images à partir des liens de fichiers
                            image1 = File.ReadAllBytes(images[0].ImageLink);
                            image2 = File.ReadAllBytes(images[1].ImageLink);
                        }
                        catch (IOException ex)
                        {
                            throw new Exception("Erreur lors de la lecture des fichiers d'images.", ex);
                        }

                        return (image1, image2);
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
    }
}
