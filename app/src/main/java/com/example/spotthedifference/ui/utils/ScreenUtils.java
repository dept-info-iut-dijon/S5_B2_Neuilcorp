package com.example.spotthedifference.ui.utils;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Classe utilitaire pour les calculs liés à l'affichage et au dimensionnement
 * des éléments à l'écran. Fournit des méthodes pour calculer les dimensions
 * des images en fonction des contraintes d'affichage.
 */
public final class ScreenUtils {

    /**
     * Constructeur privé pour empêcher l'instanciation.
     * Cette classe ne contient que des méthodes statiques.
     */
    private ScreenUtils() {
        throw new AssertionError("Cette classe ne doit pas être instanciée");
    }

    /**
     * Calcule la largeur optimale des images dans une grille.
     * La largeur est calculée en divisant la largeur de l'écran par le nombre
     * de colonnes et en soustrayant le padding.
     *
     * @param context     Contexte de l'application pour accéder aux ressources
     * @param columnCount Nombre de colonnes dans la grille (doit être > 0)
     * @param padding     Espace en pixels à réserver autour de chaque image (doit être >= 0)
     * @return Largeur calculée en pixels pour chaque image
     * @throws IllegalArgumentException si columnCount <= 0 ou padding < 0
     * @throws NullPointerException si context est null
     */
    public static int calculateImageWidth(Context context, int columnCount, int padding) {
        if (context == null) {
            throw new NullPointerException("Le contexte ne peut pas être null");
        }
        if (columnCount <= 0) {
            throw new IllegalArgumentException("Le nombre de colonnes doit être supérieur à 0");
        }
        if (padding < 0) {
            throw new IllegalArgumentException("Le padding ne peut pas être négatif");
        }

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (displayMetrics.widthPixels / columnCount) - padding;
    }

    /**
     * Calcule la hauteur d'une image en fonction de sa largeur et de son ratio d'aspect.
     * La hauteur est calculée en multipliant la largeur par le ratio hauteur/largeur.
     *
     * @param imageWidth  Largeur de l'image en pixels (doit être > 0)
     * @param aspectRatio Ratio hauteur/largeur de l'image (doit être > 0)
     * @return Hauteur calculée en pixels pour l'image
     * @throws IllegalArgumentException si imageWidth <= 0 ou aspectRatio <= 0
     */
    public static int calculateImageHeight(int imageWidth, double aspectRatio) {
        if (imageWidth <= 0) {
            throw new IllegalArgumentException("La largeur de l'image doit être supérieure à 0");
        }
        if (aspectRatio <= 0) {
            throw new IllegalArgumentException("Le ratio d'aspect doit être supérieur à 0");
        }

        return (int) (imageWidth * aspectRatio);
    }
}