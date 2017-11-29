package ru.soldatenko.demo3;

import org.junit.rules.ExternalResource;

public class Demo3Rule extends ExternalResource {
    @Override
    protected void before() throws Throwable {
        if (System.getProperty("javax.net.ssl.trustStore") == null) {
            System.setProperty("javax.net.ssl.trustStoreType", "jks");
            System.setProperty("javax.net.ssl.trustStore", "demo3.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "123456");
        }
    }

    public void setUseDemo2ClientCertificate(boolean enabled) {
        if (enabled) {
            //System.setProperty("javax.net.debug", "ssl");
            System.setProperty("javax.net.ssl.keyStoreType", "pkcs12");
            System.setProperty("javax.net.ssl.keyStore", "demo2-private.p12");
            System.setProperty("javax.net.ssl.keyStorePassword", "123456");
        } else {
            System.clearProperty("javax.net.ssl.keyStoreType");
            System.clearProperty("javax.net.ssl.keyStore");
            System.clearProperty("javax.net.ssl.keyStorePassword");
        }
    }
}
