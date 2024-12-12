package com.example.spotthedifference.config;

/**
 * Classe utilitaire centralisant les configurations du serveur pour l'application.
 * Cette classe contient les URLs des différents services backend utilisés par l'application.
 * 
 * Note de sécurité : Les URLs sont actuellement en clair dans le code.
 * En production, ces valeurs devraient être :
 * - Chiffrées ou obfusquées
 * - Configurables selon l'environnement (dev, prod, etc.)
 * - Idéalement stockées dans un fichier de configuration sécurisé
 */
public final class ServerConfig {
    
    /**
     * URL du hub SignalR pour la communication en temps réel.
     * Utilisé pour les fonctionnalités de jeu en temps réel comme
     * la synchronisation des sessions et les notifications.
     */
    public static final String SIGNALR_URL = "http://203.55.81.18:5195/gameSessionHub";

    /**
     * URL de base pour les appels API REST via Retrofit.
     * Utilisé pour toutes les opérations CRUD de l'application.
     * 
     * Note : L'URL utilise HTTPS mais le certificat n'est pas vérifié
     * en développement (voir RetrofitClient).
     */
    public static final String RETROFIT_BASE_URL = "https://203.55.81.18:7176/";

    /**
     * Constructeur privé pour empêcher l'instanciation.
     * Cette classe ne doit contenir que des constantes statiques.
     */
    private ServerConfig() {
        // Empêche l'instanciation
    }
}
