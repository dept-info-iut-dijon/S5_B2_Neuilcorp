using API7D.DATA;
using API7D.Metier;
using API7D.Services;
using Microsoft.Extensions.FileProviders;
using Microsoft.Extensions.Logging;
using System.Diagnostics;

var builder = WebApplication.CreateBuilder(args);

// Configuration du logging
builder.Services.AddLogging(logging =>
{
    logging.ClearProviders();
    logging.AddConsole();
    logging.AddDebug();
});

// Configuration des services de base
builder.Services.AddControllers();
builder.Services.AddScoped<IImage, Image>();
builder.Services.AddSingleton<SessionService>();
builder.Services.AddSingleton<ImageDATA>();

// Configuration de SignalR
builder.Services.AddSignalR();

// Configuration CORS
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAllOrigins",
        builder =>
        {
            builder.AllowAnyOrigin()
                   .AllowAnyMethod()
                   .AllowAnyHeader();
        });
});

// Configuration de Swagger pour la documentation de l'API
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

var app = builder.Build();

// Configuration du pipeline HTTP
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseCors("AllowAllOrigins");
app.UseAuthorization();
app.MapHub<GameSessionHub>("/gameSessionHub");
app.UseStaticFiles();

// Configuration de la route par défaut pour servir la page upload.html
app.MapGet("/", async context =>
{
    var filePath = Path.Combine(Directory.GetCurrentDirectory(), "wwwroot", "StaticPages", "upload.html");

    if (File.Exists(filePath))
    {
        context.Response.ContentType = "text/html";
        await context.Response.SendFileAsync(filePath);
    }
    else
    {
        context.Response.StatusCode = 404;
        await context.Response.WriteAsync("File not found.");
    }
});

app.MapControllers();

// Ouverture automatique du navigateur au démarrage
Task.Run(() =>
{
    System.Threading.Thread.Sleep(1000);

    Process.Start(new ProcessStartInfo
    {
        FileName = "http://localhost:5195",
        UseShellExecute = true
    });
});

// Démarrer l'application
app.Run();
