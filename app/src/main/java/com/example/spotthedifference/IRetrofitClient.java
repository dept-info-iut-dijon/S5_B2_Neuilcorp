package com.example.spotthedifference;

import retrofit2.Retrofit;

/// <summary>
/// Interface contenant toutes les méthodes de RetrofitClient.
/// </summary>
public interface IRetrofitClient {

    /**
     * Obtient une instance de Retrofit configurée pour des requêtes HTTP.
     *
     * @return Une instance de Retrofit non sécurisée (sans vérification SSL).
     */
    Retrofit getUnsafeRetrofit();
}
