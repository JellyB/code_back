package com.huatu.tiku.interview.util;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @Author ZhenYang
 * @Date Created in 2018/1/12 16:52
 * @Description 自定义信任管理器
 */
public class MyX509TrustManager implements X509TrustManager {

    public void checkClientTrusted(X509Certificate[] arg0, String arg1)
            throws CertificateException {

    }

    public void checkServerTrusted(X509Certificate[] arg0, String arg1)
            throws CertificateException {

    }

    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }

}