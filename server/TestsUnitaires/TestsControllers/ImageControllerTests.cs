using API7D.Controllers;
using API7D.Metier;
using API7D.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.SignalR;
using Moq;
using System.IO;
using Xunit;

namespace TestsUnitaires.TestsControllers
{
    public class ImageControlleurTests
    {
        private readonly ImageControlleur _controller;
        private readonly Mock<IImage> _mockImageService;
        private readonly string _testImagePath = Path.Combine(Directory.GetCurrentDirectory(), "Image");
        private readonly Mock<SessionService> _mockSessionService;
        private readonly Mock<IHubContext<GameSessionHub>> _mockHubContext;

        public ImageControlleurTests()
        {
            _mockImageService = new Mock<IImage>();
            _mockSessionService = new Mock<SessionService>();
            _mockHubContext = new Mock<IHubContext<GameSessionHub>>();

            _controller = new ImageControlleur(_mockHubContext.Object, _mockSessionService.Object, _mockImageService.Object);

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
