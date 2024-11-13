package com.example.spotthedifference;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

/**
 * Classe utilitaire pour afficher une image dans une vue.
 */
public class ImageDisplayer implements IImageDisplayer {
    /**
     * Affiche une image dans une vue.
     *
     * @param imageView La vue dans laquelle afficher l'image.
     * @param bitmap    L'image à afficher.
     */
    @Override
    public void displayImage(ImageView imageView, Bitmap bitmap) {
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            Log.e("ImageDisplayer", "bitmap is null");
        }
    }
}
