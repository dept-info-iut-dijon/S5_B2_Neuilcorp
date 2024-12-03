using API7D.DATA;
using API7D.Metier;
using API7D.Services;
using Microsoft.Extensions.FileProviders;
using Microsoft.Extensions.Logging;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddLogging(logging =>
{
    logging.ClearProviders();
    logging.AddConsole();
    logging.AddDebug();
    //logging.AddFilter("Microsoft.AspNetCore", LogLevel.Information);
});

// Add services to the container.
builder.Services.AddControllers();
builder.Services.AddScoped<IImage, image>();
builder.Services.AddSingleton<SessionService>();
builder.Services.AddSingleton<ImageDATA>(); // Enregistrement de ImageDATA

// Ajout de SignalR pour la communication en temps réel
builder.Services.AddSignalR();

// Ajout du CORS
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

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

//app.UseHttpsRedirection();

// Utilisation de CORS
app.UseCors("AllowAllOrigins");

app.UseAuthorization();

// Configurer le Hub SignalR
app.MapHub<GameSessionHub>("/gameSessionHub");

// Configuration pour servir des fichiers statiques depuis le dossier "StaticPages"
app.UseStaticFiles(new StaticFileOptions
{
    FileProvider = new PhysicalFileProvider(
        Path.Combine(Directory.GetCurrentDirectory(), "StaticPages")),
    RequestPath = "/static"
});


app.MapControllers();

app.Run();