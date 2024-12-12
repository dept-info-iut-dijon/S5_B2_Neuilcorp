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
 * Implémentation de IRetrofitClient configurée pour le développement.
 * Cette classe fournit un client Retrofit avec des certificats SSL non vérifiés.
 *
 * AVERTISSEMENT DE SÉCURITÉ :
 * Cette implémentation désactive les vérifications SSL et ne doit JAMAIS être utilisée en production.
 * Elle est destinée uniquement aux environnements de développement et de test.
 */
public class RetrofitClient implements IRetrofitClient {

    private static final String BASE_URL = ServerConfig.RETROFIT_BASE_URL;

    /**
     * Fournit une instance de Retrofit configurée pour ignorer les vérifications SSL.
     *
     * @return Instance de Retrofit sans vérification SSL.
     * @throws RuntimeException si la configuration du client échoue
     */
    @Override
    public Retrofit getUnsafeRetrofit() {
        try {
            TrustManager[] trustAllCerts = createTrustAllCerts();
            SSLContext sslContext = configureSslContext(trustAllCerts);
            OkHttpClient client = createUnsafeOkHttpClient(sslContext, trustAllCerts);
            
            return new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

        } catch (Exception e) {
            String errorMessage = "Erreur lors de la configuration du client Retrofit non sécurisé";
            System.err.println(errorMessage + " : " + e.getMessage());
            throw new RuntimeException(errorMessage, e);
        }
    }

    private TrustManager[] createTrustAllCerts() {
        return new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, 
                            String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, 
                            String authType) throws CertificateException {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };
    }

    private SSLContext configureSslContext(TrustManager[] trustManagers) throws Exception {
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustManagers, new java.security.SecureRandom());
        return sslContext;
    }

    private OkHttpClient createUnsafeOkHttpClient(SSLContext sslContext, 
            TrustManager[] trustManagers) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        clientBuilder.sslSocketFactory(sslContext.getSocketFactory(), 
                (X509TrustManager) trustManagers[0]);
        clientBuilder.hostnameVerifier((hostname, session) -> true);
        return clientBuilder.build();
    }
}