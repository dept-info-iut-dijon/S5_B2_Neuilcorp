package com.example.spotthedifference.models;

/**
 * Classe représentant une image avec son identifiant et celui de sa paire.
 * Utilisée pour transmettre les données d'image depuis le serveur vers le client.
 */
public class ImageWithPair {

    private int imageId;
    private int imagePairId;
    private String base64Image;

    /**
     * Récupère l'identifiant unique de l'image.
     *
     * @return identifiant de l'image
     */
    public int getImageId() {
        return imageId;
    }

    /**
     * Récupère l'identifiant de la paire d'images.
     *
     * @return identifiant de la paire d'images
     */
    public int getImagePairId() {
        return imagePairId;
    }

    /**
     * Récupère l'image encodée en Base64.
     * Cette chaîne Base64 représente le contenu de l'image et est utilisée pour l'afficher.
     *
     * @return image encodée en Base64
     */
    public String getBase64Image() {
        return base64Image;
    }
}