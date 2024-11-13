package com.example.spotthedifference;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Interface contenant toutes les méthodes de ImageDisplayer.
 */
public interface IImageDisplayer {

    /**
     * Affiche une image dans un ImageView donné.
     *
     * @param imageView ImageView dans lequel afficher l'image.
     * @param bitmap    Bitmap de l'image à afficher.
     */
    void displayImage(ImageView imageView, Bitmap bitmap);
}
