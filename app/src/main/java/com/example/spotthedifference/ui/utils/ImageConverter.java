package com.example.spotthedifference.ui.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Classe utilitaire pour convertir un tableau d'octets en objet Bitmap.
 */
public class ImageConverter implements IImageConverter {

    public ImageConverter() {}

    /**
     * Convertit un tableau d'octets en un objet Bitmap.
     *
     * @param imageBytes Tableau d'octets représentant l'image. Ne doit pas être null ou vide.
     * @return L'objet Bitmap correspondant, ou null si le tableau d'octets est invalide.
     */
    @Override
    public Bitmap convertBytesToBitmap(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            return null;
        }
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }
}
