package com.example.spotthedifference.ui.activities;

import com.example.spotthedifference.models.Coordonnees;

/**
 * Interface contenant toutes les méthodes de gestion des interactions dans MainActivity.
 */
public interface IMainActivity {

    /**
     * Envoie les coordonnées sélectionnées par l'utilisateur au serveur, associées
     * aux informations de session et d'utilisateur.
     *
     * @param coordonnees Coordonnées sélectionnées sur l'image.
     * @param sessionId   Identifiant unique de la session en cours.
     * @param playerId    Identifiant unique du joueur qui a sélectionné les coordonnées.
     * @param imagePairId     Identifiant unique de l'image en cours de traitement.
     */
    void sendCoordinatesToServer(Coordonnees coordonnees, String sessionId, String playerId, String imagePairId);

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