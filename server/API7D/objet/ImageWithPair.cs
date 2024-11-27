namespace API7D.objet
{
    public class ImageWithPair
    {
        private int imageId;
        private int imagePairId;
        private string base64Image;
        public int ImageId
        {
            get { return imageId; }
            set { imageId = value; }
        }
        public int ImagePairId
        {
            get { return imagePairId; }
            set { imagePairId = value; }
        }
        public string Base64Image
        {
            get { return base64Image; }
            set { base64Image = value; }
        }
    }
}