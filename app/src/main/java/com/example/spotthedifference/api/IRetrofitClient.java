package com.example.spotthedifference.api;

import retrofit2.Retrofit;

/**
 * Interface contenant toutes les méthodes de RetrofitClient.
 */
public interface IRetrofitClient {

    /**
     * Obtient une instance de Retrofit configurée pour des requêtes HTTP non sécurisées.
     *
     * @return Une instance de Retrofit sans vérification SSL.
     */
    Retrofit getUnsafeRetrofit();
}