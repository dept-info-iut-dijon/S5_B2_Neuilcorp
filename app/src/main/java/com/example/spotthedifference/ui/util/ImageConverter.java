package com.example.spotthedifference.ui.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Classe utilitaire pour la conversion d'images.
 */
public class ImageConverter
{

    /**
     * Convertit une image en tableau de bytes.
     *
     * @param imageBytes L'image à convertir.
     * @return Le tableau de bytes représentant l'image.
     */
    public static Bitmap convertBytesToBitmap(byte[] imageBytes)
    {
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }
}
