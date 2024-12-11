using API7D.DATA;
using API7D.Metier;
using API7D.Services;
using Microsoft.Extensions.FileProviders;
using Microsoft.Extensions.Logging;
using System.Diagnostics;  // Importation n�cessaire pour Process

var builder = WebApplication.CreateBuilder(args);

// Configuration des services
builder.Services.AddLogging(logging =>
{
    logging.ClearProviders();
    logging.AddConsole();
    logging.AddDebug();
});

builder.Services.AddControllers(); // Ajouter les contr�leurs
builder.Services.AddScoped<IImage, image>();
builder.Services.AddSingleton<SessionService>();
builder.Services.AddSingleton<ImageDATA>();

// Ajouter SignalR pour la communication en temps r�el
builder.Services.AddSignalR();

// Ajouter le CORS pour permettre les requ�tes depuis diff�rentes origines
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAllOrigins",
        builder =>
        {
            builder.AllowAnyOrigin()    // Permet toutes les origines
                   .AllowAnyMethod()    // Permet toutes les m�thodes (GET, POST, etc.)
                   .AllowAnyHeader();   // Permet tous les en-t�tes
        });
});

// Swagger configuration
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

var app = builder.Build();

// Pipeline de requ�tes HTTP
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

// Configurer les routes des contr�leurs
app.MapControllers();

// Lancer la page HTML automatiquement apr�s le d�marrage
Task.Run(() =>
{
    // D�lai pour s'assurer que le serveur est bien d�marr�
    System.Threading.Thread.Sleep(1000);

    // Ouvrir le navigateur par d�faut � l'URL locale
    Process.Start(new ProcessStartInfo
    {
        FileName = "http://localhost:5195",  // Modifier si n�cessaire (en fonction de votre configuration)
        UseShellExecute = true
    });
});

// D�marrer l'application
app.Run();
