using API7D.DATA;
using API7D.Metier;
using API7D.Services;
using Microsoft.Extensions.FileProviders;
using Microsoft.Extensions.Logging;

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

// Activer les fichiers statiques (servir HTML/CSS/JS depuis wwwroot)
app.UseStaticFiles();

// Activer CORS
app.UseCors("AllowAllOrigins");

// Configurer l'autorisation
app.UseAuthorization();

// Configurer le Hub SignalR
app.MapHub<GameSessionHub>("/gameSessionHub");

// Route par défaut pour servir la page HTML
app.MapGet("/", async context =>
{
    // URL ICI
    await context.Response.SendFileAsync("wwwroot/index.html");
});

// Configurer les routes des contrôleurs
app.MapControllers();

// Démarrer l'application
app.Run();
