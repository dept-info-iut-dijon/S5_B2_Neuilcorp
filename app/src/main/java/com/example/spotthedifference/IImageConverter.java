package com.example.spotthedifference;

import android.graphics.Bitmap;

/// <summary>
/// Interface contenant toutes les méthodes de ImageConverter.
/// </summary>
public interface IImageConverter {

    /**
     * Convertit un tableau d'octets en un objet Bitmap.
     *
     * @param imageBytes Tableau d'octets représentant l'image.
     * @return L'objet Bitmap correspondant.
     */
    Bitmap convertBytesToBitmap(byte[] imageBytes);
}
