package com.example.spotthedifference.config;

/**
 * Classe centralisant les configurations du serveur.
 */
public class ServerConfig {
    // URL de base pour SignalR
    public static final String SIGNALR_URL = "http://203.55.81.18:5195/gameSessionHub";

    // URL de base pour Retrofit
    public static final String RETROFIT_BASE_URL = "https://203.55.81.18:7176/";

    private ServerConfig() {
    }
}
