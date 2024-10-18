
// SignalRClient.java
        package com.example.spotthedifference;

import android.util.Log;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class SignalRClient {
    private HubConnection hubConnection;

    public SignalRClient(String serverUrl) {
        hubConnection = HubConnectionBuilder.create(serverUrl)
                .setHttpClientBuilderCallback(builder -> {
                    OkHttpClient client = new OkHttpClient.Builder()
                            .sslSocketFactory(UnsafeSSLSocketFactory.getUnsafeSSLSocketFactory(), new UnsafeSSLSocketFactory().getTrustManager())
                            .hostnameVerifier((hostname, session) -> true)
                            .addInterceptor(chain -> {
                                Request request = chain.request().newBuilder().build();
                                return chain.proceed(request);
                            })
                            .build();
                    builder.sslSocketFactory(client.sslSocketFactory(), client.x509TrustManager());
                    builder.hostnameVerifier(client.hostnameVerifier());
                    builder.addInterceptor(client.interceptors().get(0));
                })
                .build();
    }

    public void start() {
        try {
            hubConnection.start().blockingAwait();
            Log.d("SignalRClient", "SignalR connection started.");
        } catch (Exception e) {
            Log.e("SignalRClient", "Error starting SignalR connection: " + e.getMessage());
        }
    }

    public void stop() {
        try {
            hubConnection.stop();
            Log.d("SignalRClient", "SignalR connection stopped.");
        } catch (Exception e) {
            Log.e("SignalRClient", "Error stopping SignalR connection: " + e.getMessage());
        }
    }

    public HubConnection getHubConnection() {
        return hubConnection;
    }
}