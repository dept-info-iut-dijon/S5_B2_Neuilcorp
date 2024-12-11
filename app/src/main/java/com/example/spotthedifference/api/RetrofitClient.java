package com.example.spotthedifference.api;

import com.example.spotthedifference.config.ServerConfig;

import java.security.cert.CertificateException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Classe RetrofitClient configurée pour fournir un client Retrofit avec des certificats SSL non vérifiés.
 * Note : Utiliser uniquement pour des environnements de développement ou de test.
 */
public class RetrofitClient implements IRetrofitClient {

    private static final String BASE_URL = ServerConfig.RETROFIT_BASE_URL;

    /**
     * Fournit une instance de Retrofit configurée pour ignorer les vérifications SSL.
     *
     * @return Instance de Retrofit sans vérification SSL.
     */
    @Override
    public Retrofit getUnsafeRetrofit() {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
            clientBuilder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
            clientBuilder.hostnameVerifier((hostname, session) -> true);

            OkHttpClient client = clientBuilder.build();

            return new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

        } catch (Exception e) {
            System.err.println("Erreur lors de la configuration du client Retrofit non sécurisé : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la configuration du client Retrofit non sécurisé", e);
        }
    }
}