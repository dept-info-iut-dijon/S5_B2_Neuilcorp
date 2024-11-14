package com.example.spotthedifference.ui.activities;

import com.example.spotthedifference.models.Player;

import java.util.List;

/// <summary>
/// Interface contenant  toutes les méthodes de WaitingRoomActivity.
/// </summary>
public interface IWaitingRoomActivity {

    /**
     * Charge les détails de la session.
     *
     * @param sessionId Identifiant de la session à charger.
     */
    void loadSessionDetails(String sessionId);

    void displayPlayers(List<Player> players);

    /**
     * Copie le texte fourni dans le presse-papiers.
     *
     * @param text Texte à copier.
     */
    void copyToClipboard(String text);

    /**
     * Supprime la session actuelle et retourne à l'activité d'accueil.
     */
    void deleteSessionAndExit();
}
