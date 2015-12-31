/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.dolezel.fatrat.applet.util;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class TrustAllCertificates {
    public static void setup() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                        System.out.println("authType is " + authType);
                        System.out.println("cert issuers");
                        for (int i = 0; i < certs.length; i++) {
                            System.out.println("\t" + certs[i].getIssuerX500Principal().getName());
                            System.out.println("\t" + certs[i].getIssuerDN().getName());
                       }
                    }
                }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            
        }
        
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(
                new HostnameVerifier() {
                @Override
                    public boolean verify(String hostname, SSLSession sslSession) {
                        return true;
                    }
                }
            );
        } catch (Exception e) {
            
        }
    }
}
