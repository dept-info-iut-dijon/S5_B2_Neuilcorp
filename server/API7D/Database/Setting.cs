namespace API7D.Database
{
    /// <summary>
    /// Fournit les paramètres de configuration pour l'accès à la base de données.
    /// </summary>
    public class Setting
    {
        private static readonly string _databasePath = "Data Source=./Database/DataBase_Image.db";

        /// <summary>
        /// Obtient le chemin de connexion à la base de données SQLite.
        /// </summary>
        /// <returns>La chaîne de connexion à la base de données</returns>
        public static string DataBasePath
        { get { return _databasePath; } }
    }
}
