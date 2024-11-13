package com.example.spotthedifference;

import android.widget.ImageView;

/**
 * Interface contenant toutes les méthodes de MainActivity.
 */
public interface IMainActivity {

    /**
     * Charge une image à partir de son identifiant.
     *
     * @param imageId Identifiant de l'image à charger.
     */
    void loadImage(int imageId);

    /**
     * Envoie les coordonnées au serveur.
     *
     * @param coordonnees Coordonnées à envoyer.
     */
    void sendCoordinatesToServer(Coordonnees coordonnees);

    /**
     * Affiche une boîte de dialogue d'attente.
     */
    void showWaitingDialog();

    /**
     * Masque la boîte de dialogue d'attente.
     */
    void hideWaitingDialog();

    /**
     * Affiche une boîte de dialogue de résultat.
     *
     * @param isSuccess Vrai si le résultat est un succès, sinon faux.
     */
    void showResultDialog(boolean isSuccess);
}