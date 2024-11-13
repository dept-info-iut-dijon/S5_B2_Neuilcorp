package com.example.spotthedifference.data.network;

import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/**
 * Classe utilitaire fournissant un `SSLSocketFactory` non sécurisé,
 * permettant d'ignorer les vérifications de certificats SSL.
 */
public class UnsafeSSLSocketFactory {

    /**
     * Retourne une instance de `SSLSocketFactory` qui ignore les erreurs SSL,
     * acceptant ainsi tous les certificats sans vérification.
     *
     * @return SSLSocketFactory configuré pour accepter toutes les connexions SSL sans vérification de certificat.
     */
    public static SSLSocketFactory getUnsafeSSLSocketFactory() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null; // Accept all certificates
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                            // No check
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                            // No check
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
