package com.example.spotthedifference.ui.utils;

import android.content.Context;
import android.util.DisplayMetrics;

public class ScreenUtils {

    /**
     * Calcule la largeur des images dans une grille basée sur le nombre de colonnes.
     *
     * @param context Contexte de l'application.
     * @param columnCount Nombre de colonnes dans la grille.
     * @param padding Espace (en pixels) à déduire pour chaque image.
     * @return Largeur calculée pour chaque image.
     */
    public static int calculateImageWidth(Context context, int columnCount, int padding) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (displayMetrics.widthPixels / columnCount) - padding;
    }

    /**
     * Calcule la hauteur d'une image en fonction de sa largeur et de son ratio.
     *
     * @param imageWidth Largeur de l'image.
     * @param aspectRatio Ratio hauteur/largeur de l'image.
     * @return Hauteur calculée pour l'image.
     */
    public static int calculateImageHeight(int imageWidth, double aspectRatio) {
        return (int) (imageWidth * aspectRatio);
    }
}