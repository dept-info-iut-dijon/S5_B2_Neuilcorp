package com.example.spotthedifference;

import java.util.List;

/// <summary>
/// Interface contenant toutes les méthodes de GameSession.
/// </summary>
public interface IGameSession {

    /**
     * Obtient l'identifiant de la session de jeu.
     *
     * @return L'ID de la session.
     */
    String getSessionId();

    /**
     * Définit l'identifiant de la session de jeu.
     *
     * @param sessionId L'ID de la session à définir.
     */
    void setSessionId(String sessionId);

    /**
     * Obtient la liste des joueurs participant à la session de jeu.
     *
     * @return Liste des joueurs.
     */
    List<Player> getPlayers();
}
