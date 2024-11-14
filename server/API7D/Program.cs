using API7D.DATA;
using API7D.Metier;
using API7D.Services;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddControllers();
builder.Services.AddSingleton<SessionService>();
builder.Services.AddSingleton<ImageDATA>(); // Enregistrement de ImageDATA

// Ajout de SignalR pour la communication en temps r�el
builder.Services.AddSignalR();

// Ajout du CORS
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

app.MapControllers();

app.Run();