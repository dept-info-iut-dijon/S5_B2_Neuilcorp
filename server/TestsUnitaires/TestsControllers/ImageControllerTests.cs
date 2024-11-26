﻿using API7D.Controllers;
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
            byte[] imageBytes = new byte[] { 0x01, 0x02, 0x03 }; 

            var imageServiceMock = new Mock<IImage>();
            imageServiceMock.Setup(service => service.GetImages(imageId)).Returns(imageBytes);

            var controller = new ImageControlleur(null, null, imageServiceMock.Object);
            var result = controller.GetImage(imageId); 
            var actionResult = Assert.IsType<ActionResult<byte[]>>(result);
            var fileResult = Assert.IsType<FileContentResult>(actionResult.Result);

            Assert.Equal("application/octet-stream", fileResult.ContentType);
            Assert.Equal(imageBytes, fileResult.FileContents);
        }




        [Fact]
        public void GetImage_ReturnsNotFound_WhenImageDoesNotExist()
        {
            int imageId = 999;

            var imageServiceMock = new Mock<IImage>();
            imageServiceMock.Setup(service => service.GetImages(imageId)).Returns((byte[])null);

            var controller = new ImageControlleur(null, null, imageServiceMock.Object);

            var result = controller.GetImage(imageId);

            Assert.IsType<NotFoundObjectResult>(result.Result);
            var notFoundResult = result.Result as NotFoundObjectResult;
            Assert.Equal($"Image {imageId} non trouvée.", notFoundResult.Value);
        }


    }
}
