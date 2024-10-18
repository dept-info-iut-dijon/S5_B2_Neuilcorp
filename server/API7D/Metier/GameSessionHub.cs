using Microsoft.AspNetCore.SignalR;
using System.Threading.Tasks;
using System.Collections.Generic;
namespace API7D.Metier
{

    /// <summary>
    /// web Socket pas terminer
    /// </summary>
    public class GameSessionHub : Hub
    {
        // Méthode pour joindre un groupe de session (pour chaque session, un groupe est créé)
        public async Task JoinSessionGroup(string sessionId)
        {
            await Groups.AddToGroupAsync(Context.ConnectionId, sessionId);
        }

        // Méthode pour notifier que le joueur a rejoint la session
        public async Task PlayerJoined(string sessionId, string playerName)
        {
            await Clients.Group(sessionId).SendAsync("PlayerJoined", playerName);
        }
    }

}
