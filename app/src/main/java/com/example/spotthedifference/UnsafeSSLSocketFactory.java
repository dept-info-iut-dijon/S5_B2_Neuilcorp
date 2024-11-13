package com.example.spotthedifference;

import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class UnsafeSSLSocketFactory implements IUnsafeSSLSocketFactory {

    /**
     * Retourne une instance de SSLSocketFactory qui ignore les vérifications SSL.
     *
     * @return SSLSocketFactory une instance non sécurisée de SSLSocketFactory
     */
    @Override
    public SSLSocketFactory getUnsafeSSLSocketFactory() {
        try {
            // Création d'un TrustManager qui ignore toutes les vérifications de certificats
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null; // Accepte tous les certificats
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                            // Pas de vérification
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                            // Pas de vérification
                        }
                    }
            };

            // Initialisation du contexte SSL avec le TrustManager qui ignore les vérifications
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
