using API7D.DATA;
using API7D.objet;
using API7D.Services;
using Microsoft.AspNetCore.SignalR;
using System.Data.SqlTypes;

namespace API7D.Metier
{
    public class image : IImage
    {
        private IImageDATA _data = new ImageDATA()!;
        public byte[] GetImages(int ID)
        {
            try
            {
                string path = _data.GetImagesDATA(ID);
                byte[] imageBytes = System.IO.File.ReadAllBytes(path);
                return imageBytes;
            }
            catch (Exception ex)
            {
                throw new Exception(ex.ToString());
            }
        }

        public List<byte[]> GetAllImages()
        {
            try
            {
                List<string> path = _data.GetAllImagesDATA();
                List<byte[]> image = new List<byte[]>();
                foreach (string pathItem in path)
                {
                    byte[] imageBytes = System.IO.File.ReadAllBytes(pathItem);
                    image.Add(imageBytes);
                }
                
                return image;
            }
            catch (Exception ex)
            {
                throw new Exception(ex.ToString());
            }
        }

        public void SetImages(ImageDifference image)
        {
            throw new NotImplementedException();
        }

        public (byte[] Image1, byte[] Image2) GetImagePair(int pairId)
        {
            try
            {
                var imagePaths = _data.GetAllImagesWithPairData()
                                      .Where(img => img.ImagePairId == pairId)
                                      .Select(img => img.ImageLink)
                                      .ToList();

                if (imagePaths.Count != 2)
                {
                    throw new Exception($"La paire d'images avec l'ID {pairId} est incomplète ou introuvable.");
                }

                byte[] image1 = File.ReadAllBytes(imagePaths[0]);
                byte[] image2 = File.ReadAllBytes(imagePaths[1]);

                return (image1, image2);
            }
            catch (Exception ex)
            {
                throw new Exception($"Erreur lors de la récupération de la paire d'images avec l'ID {pairId}: {ex.Message}", ex);
            }
        }

        public List<ImageWithPair> GetAllImagesWithPairs()
        {
            var imageData = _data.GetAllImagesWithPairData();
            var imageWithPairs = new List<ImageWithPair>();

            foreach (var (imageId, imagePairId, imageLink) in imageData)
            {
                var base64String = Convert.ToBase64String(File.ReadAllBytes(imageLink));
                imageWithPairs.Add(new ImageWithPair
                {
                    ImageId = imageId,
                    ImagePairId = imagePairId,
                    Base64Image = base64String
                });
            }

            return imageWithPairs;
        }

        public (byte[] Image1, byte[] Image2) ReadyImageToPlayer(string Idsession , SessionService _session)
        {
            var session = _session.GetSessionById(Idsession);
            if (session == null)
            {
                throw new Exception("session invalide");
            }

            if (session.ImagePairId == 0)
            {
                throw new Exception("l'hote n'a pas choisi d'image");
            }
            
            var imagePairId = session.ImagePairId;
            var images = GetImagePair(imagePairId);

            return images;
        }


    }
}
