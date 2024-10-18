package com.example.spotthedifference;

import java.security.cert.CertificateException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    public static Retrofit getUnsafeRetrofit() {
        try {
            // Création d'un trust manager qui ignore les erreurs SSL
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

            // Installer le trust manager dans un contexte SSL
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Créer un client OkHttp qui utilise ce trust manager
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
            clientBuilder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
            clientBuilder.hostnameVerifier((hostname, session) -> true); // Accepter tous les noms d'hôte

            OkHttpClient client = clientBuilder.build();

            return new Retrofit.Builder()
                    .baseUrl("https://203.55.81.18:7176/") // URL avec HTTPS
                    .client(client) // Utilise le client OkHttp personnalisé
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
