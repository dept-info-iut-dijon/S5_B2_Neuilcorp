using System;
using System.Data;
using System.IO;
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

                string query = "SELECT ImageLink FROM Images WHERE ImageId = @ID";
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
        /// Ajoute une nouvelle image dans la base de données.
        /// </summary>
        /// <param name="image">L'objet image contenant les informations de l'image.</param>
        public void SetImagesDATA(ImageDifference image)
        {
            // a implementer au sprint 4
            /*using (var connection = new SqliteConnection(_connectionString))
            {
                connection.Open();

                string query = "INSERT INTO Images (ImageId, ImagePaire, ImageLink) VALUES (@ImageId, @ImagePaire, @ImageLink)";
                using (var command = new SqliteCommand(query, connection))
                {
                    command.Parameters.AddWithValue("@ImageId", image.ImageId);
                    command.Parameters.AddWithValue("@ImagePaire", image.ImagePaire);
                    command.Parameters.AddWithValue("@ImageLink", image.ImageLink);

                    command.ExecuteNonQuery();
                }
            }*/
        }
    }
}
