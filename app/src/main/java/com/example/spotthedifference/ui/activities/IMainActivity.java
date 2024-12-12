package com.example.spotthedifference.ui.activities;

import com.example.spotthedifference.models.Coordonnees;

/**
 * Interface définissant le contrat pour l'activité principale du jeu.
 * Gère les interactions utilisateur et la communication avec le serveur
 * pendant une partie.
 */
public interface IMainActivity {

    /**
     * Envoie les coordonnées sélectionnées par l'utilisateur au serveur.
     *
     * @param coordonnees  Coordonnées sélectionnées sur l'image
     * @param sessionId    Identifiant de la session en cours
     * @param playerId     Identifiant du joueur actif
     * @param imagePairId  Identifiant de la paire d'images en cours
     */
    void sendCoordinatesToServer(Coordonnees coordonnees, String sessionId, 
            String playerId, String imagePairId);

    /**
     * Affiche une boîte de dialogue d'attente pendant la synchronisation ou
     * le traitement en arrière-plan.
     */
    void showWaitingDialog();

    /**
     * Masque la boîte de dialogue d'attente lorsque le traitement en arrière-plan est terminé.
     */
    void hideWaitingDialog();

    /**
     * Affiche une boîte de dialogue pour indiquer le résultat de la tentative
     * de l'utilisateur (succès ou échec).
     *
     * @param isSuccess Indique si le résultat est un succès (true) ou un échec (false).
     */
    void showResultDialog(boolean isSuccess);
}