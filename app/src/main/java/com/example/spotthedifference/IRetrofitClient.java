package com.example.spotthedifference;

import retrofit2.Retrofit;

public interface IRetrofitClient {

    // Méthode pour obtenir une instance Retrofit configurée
    Retrofit getUnsafeRetrofit();
}
