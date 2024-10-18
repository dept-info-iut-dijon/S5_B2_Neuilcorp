using API7D.Controllers;
using Microsoft.AspNetCore.Mvc;
using Moq;
using System.IO;
using Xunit;

namespace TestsUnitaires.TestsControllers
{
    public class ImageControlleurTests
    {
        private readonly ImageControlleur _controller;
        private readonly string _testImagePath = Path.Combine(Directory.GetCurrentDirectory(), "Image");

        public ImageControlleurTests()
        {
            _controller = new ImageControlleur();

            if (!Directory.Exists(_testImagePath))
            {
                Directory.CreateDirectory(_testImagePath);
            }
        }

        [Fact]
        public void GetImage_ReturnsFile_WhenImageExists()
        {
            int imageId = 1;
            string imagePath = Path.Combine(_testImagePath, $"{imageId}.png");

            byte[] imageBytes = new byte[] { 0x01, 0x02, 0x03 };
            File.WriteAllBytes(imagePath, imageBytes);

            var result = _controller.GetImage(imageId);

            var fileResult = Assert.IsType<FileContentResult>(result.Result);
            Assert.Equal("application/octet-stream", fileResult.ContentType);
            Assert.Equal(imageBytes, fileResult.FileContents);

            File.Delete(imagePath);
        }

        [Fact]
        public void GetImage_ReturnsNotFound_WhenImageDoesNotExist()
        {
            int imageId = 999;

            var result = _controller.GetImage(imageId);

            Assert.IsType<NotFoundResult>(result.Result);
        }
    }
}
