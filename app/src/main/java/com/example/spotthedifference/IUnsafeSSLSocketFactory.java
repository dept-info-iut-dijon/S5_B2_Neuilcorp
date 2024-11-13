package com.example.spotthedifference;

import javax.net.ssl.SSLSocketFactory;

public interface IUnsafeSSLSocketFactory {
    /**
     * Retourne une instance de SSLSocketFactory qui ignore les vérifications SSL.
     *
     * @return SSLSocketFactory une instance non sécurisée de SSLSocketFactory
     */
    SSLSocketFactory getUnsafeSSLSocketFactory();
}
