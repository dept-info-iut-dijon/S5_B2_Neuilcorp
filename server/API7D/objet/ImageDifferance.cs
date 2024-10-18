namespace API7D.objet
{
    public class ImageDifference
    {
        private string imageUrl;
        private List<Coordonnees> coordonnees;
        public string ImageUrl
        {
            get { return ImageUrl; }
            set { ImageUrl = value; }
        }
        public List<Coordonnees> Differences
        {
            get { return Differences; }
            set { Differences = value; }
        }
    }
}
