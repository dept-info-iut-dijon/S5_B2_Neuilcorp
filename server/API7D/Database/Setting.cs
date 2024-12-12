namespace API7D.Database
{
    public class Setting
    {
        private static readonly string databasePath = "Data Source=./Database/DataBase_Image.db";
        public Setting() { }

        public static string DataBasePath
            { get { return databasePath; } }
    }
}
