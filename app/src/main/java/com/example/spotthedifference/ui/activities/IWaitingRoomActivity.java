package com.example.spotthedifference.ui.activities;

import com.example.spotthedifference.models.Player;

import java.util.List;

/**
 * Interface définissant le contrat pour l'activité de salle d'attente.
 * Gère l'affichage et la gestion des joueurs avant le début d'une partie.
 */
public interface IWaitingRoomActivity {

    /**
     * Charge les détails de la session depuis le serveur.
     *
     * @param sessionId Identifiant de la session à charger
     */
    void loadSessionDetails(String sessionId);

    /**
     * Affiche la liste des joueurs dans l'interface utilisateur.
     *
     * @param players Liste des joueurs à afficher
     */
    void displayPlayers(List<Player> players);

    /**
     * Copie le texte fourni dans le presse-papiers du système.
     *
     * @param text Texte à copier
     */
    void copyToClipboard(String text);

    /**
     * Supprime la session actuelle et redirige vers l'accueil.
     * Le comportement varie selon que l'utilisateur est l'hôte ou non.
     */
    void deleteSessionAndExit();
}
