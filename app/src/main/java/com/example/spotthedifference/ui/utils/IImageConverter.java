package com.example.spotthedifference.ui.utils;

import android.graphics.Bitmap;

/**
 * Interface contenant toutes les méthodes de ImageConverter.
 */
public interface IImageConverter {

    /**
     * Convertit un tableau d'octets en un objet Bitmap.
     *
     * @param imageBytes Tableau d'octets représentant l'image. Doit être une représentation valide d'une image.
     * @return L'objet Bitmap correspondant, ou null si la conversion échoue.
     */

    Bitmap convertBytesToBitmap(byte[] imageBytes);
}
