package com.example.spotthedifference.ui.util;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;


/**
 * Classe utilitaire pour afficher un Bitmap dans un ImageView.
 */
public class ImageDisplayer
{

    /**
     * Affiche un Bitmap dans le ImageView spécifié.
     * Si le Bitmap est null, un message d'erreur est enregistré pour faciliter le débogage.
     *
     * @param imageView Le ImageView dans lequel l'image doit être affichée.
     * @param bitmap    Le Bitmap de l'image à afficher.
     */
    public static void displayImage(ImageView imageView, Bitmap bitmap)
    {
        if (bitmap != null)
        {
            imageView.setImageBitmap(bitmap);
        } else {
            Log.e("ImageDisplayer", "bitmap is null");
        }
    }
}
