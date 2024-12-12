package com.example.spotthedifference.models;

/**
 * Classe représentant une image avec son identifiant et celui de sa paire.
 * Utilisée pour transmettre les données d'image depuis le serveur vers le client.
 * Cette classe est principalement utilisée pour la sérialisation/désérialisation JSON.
 */
public class ImageWithPair {

    /**
     * Identifiant unique de l'image.
     * Utilisé pour identifier spécifiquement cette image dans le système.
     */
    private int imageId;

    /**
     * Identifiant de la paire d'images à laquelle cette image appartient.
     * Permet de lier deux images qui doivent être comparées.
     */
    private int imagePairId;

    /**
     * Représentation de l'image en format Base64.
     * Permet de transmettre l'image via JSON sans nécessiter de transfert de fichier binaire.
     */
    private String base64Image;

    /**
     * Constructeur par défaut nécessaire pour la sérialisation JSON.
     */
    public ImageWithPair() {
        // Constructeur vide nécessaire pour Jackson/Gson
    }

    /**
     * Récupère l'identifiant unique de l'image.
     *
     * @return l'identifiant unique de l'image
     */
    public int getImageId() {
        return imageId;
    }

    /**
     * Définit l'identifiant unique de l'image.
     * Utilisé principalement pour la désérialisation JSON.
     *
     * @param imageId le nouvel identifiant de l'image
     */
    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    /**
     * Récupère l'identifiant de la paire d'images.
     *
     * @return l'identifiant de la paire d'images
     */
    public int getImagePairId() {
        return imagePairId;
    }

    /**
     * Définit l'identifiant de la paire d'images.
     * Utilisé principalement pour la désérialisation JSON.
     *
     * @param imagePairId le nouvel identifiant de la paire d'images
     */
    public void setImagePairId(int imagePairId) {
        this.imagePairId = imagePairId;
    }

    /**
     * Récupère l'image encodée en Base64.
     * Cette chaîne Base64 représente le contenu binaire de l'image et est utilisée
     * pour la transmission et l'affichage.
     *
     * @return l'image encodée en Base64
     */
    public String getBase64Image() {
        return base64Image;
    }

    /**
     * Définit l'image encodée en Base64.
     * Utilisé principalement pour la désérialisation JSON.
     *
     * @param base64Image la nouvelle image encodée en Base64
     */
    public void setBase64Image(String base64Image) {
        this.base64Image = base64Image;
    }
}