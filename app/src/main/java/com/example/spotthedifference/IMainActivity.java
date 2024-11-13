package com.example.spotthedifference;

import android.graphics.Bitmap;
import android.widget.ImageView;

/// <summary>
/// Interface contenant toutes les méthodes de MainActivity.
/// </summary>
public interface IMainActivity {

    /**
     * Charge une image à partir de son identifiant.
     *
     * @param imageId Identifiant de l'image à charger.
     */
    void loadImage(int imageId);

    /**
     * Affiche une image dans un ImageView donné.
     *
     * @param imageView ImageView dans lequel afficher l'image.
     * @param bitmap    Bitmap de l'image à afficher.
     */
    void displayImage(ImageView imageView, Bitmap bitmap);

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
