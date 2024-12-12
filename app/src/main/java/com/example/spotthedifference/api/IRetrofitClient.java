package com.example.spotthedifference.api;

import retrofit2.Retrofit;

/**
 * Interface définissant les méthodes de configuration du client Retrofit pour l'application.
 * Cette interface permet d'abstraire la création et la configuration du client HTTP
 * utilisé pour les communications avec le serveur backend.
 *
 * ATTENTION : L'implémentation actuelle utilise une configuration non sécurisée,
 * à utiliser uniquement en développement.
 */
public interface IRetrofitClient {

    /**
     * Obtient une instance de Retrofit configurée pour des requêtes HTTP non sécurisées.
     *
     * ATTENTION : Cette configuration ignore les vérifications SSL et ne doit pas
     * être utilisée en production.
     *
     * @return Une instance de Retrofit sans vérification SSL.
     * @throws RuntimeException si la configuration du client échoue
     */
    Retrofit getUnsafeRetrofit();
}