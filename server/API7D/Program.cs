using API7D.DATA;
using API7D.Metier;
using API7D.Services;
using Microsoft.Extensions.FileProviders;
using Microsoft.Extensions.Logging;
using System.Diagnostics;  // Importation nécessaire pour Process

var builder = WebApplication.CreateBuilder(args);

// Configuration des services
builder.Services.AddLogging(logging =>
{
    logging.ClearProviders();
    logging.AddConsole();
    logging.AddDebug();
});

builder.Services.AddControllers(); // Ajouter les contrôleurs
builder.Services.AddScoped<IImage, image>();
builder.Services.AddSingleton<SessionService>();
builder.Services.AddSingleton<ImageDATA>();

// Ajouter SignalR pour la communication en temps réel
builder.Services.AddSignalR();

// Ajouter le CORS pour permettre les requêtes depuis différentes origines
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAllOrigins",
        builder =>
        {
            builder.AllowAnyOrigin()    // Permet toutes les origines
                   .AllowAnyMethod()    // Permet toutes les méthodes (GET, POST, etc.)
                   .AllowAnyHeader();   // Permet tous les en-têtes
        });
});

// Swagger configuration
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

var app = builder.Build();

// Pipeline de requêtes HTTP
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}


// Activer CORS
app.UseCors("AllowAllOrigins");

// Configurer l'autorisation
app.UseAuthorization();

// Configurer le Hub SignalR
app.MapHub<GameSessionHub>("/gameSessionHub");


// Activer les fichiers statiques (servir HTML/CSS/JS depuis wwwroot)
app.UseStaticFiles();


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

// Configurer les routes des contrôleurs
app.MapControllers();

// Lancer la page HTML automatiquement après le démarrage
Task.Run(() =>
{
    // Délai pour s'assurer que le serveur est bien démarré
    System.Threading.Thread.Sleep(1000);

    // Ouvrir le navigateur par défaut à l'URL locale
    Process.Start(new ProcessStartInfo
    {
        FileName = "http://localhost:5195",  // Modifier si nécessaire (en fonction de votre configuration)
        UseShellExecute = true
    });
});

// Démarrer l'application
app.Run();
