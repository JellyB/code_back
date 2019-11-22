package com.huatu.ztk.user.utils;

import org.apache.commons.collections.MapUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Created by linkang on 4/12/17.
 */
public class HttpClientUtils {
    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);

    public static String sendHttpGetRequest(String url, Map<String, Object> params) {
        URIBuilder uriBuilder = null;
        try {
            uriBuilder = new URIBuilder(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        if (MapUtils.isNotEmpty(params)) {
            //添加参数
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (entry.getValue() != null) {
                    uriBuilder.addParameter(entry.getKey(), entry.getValue().toString());
                }
            }
        }

        String newUrl = "";
        try {
            newUrl = uriBuilder.build().toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        logger.info("request url={}", newUrl);

        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        String result = "";

        //尝试两次
        try {
            client = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(newUrl);
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(4000)
                    .setConnectTimeout(3000)
                    .build();

            httpGet.setConfig(requestConfig);

            response = client.execute(httpGet);

            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                result = EntityUtils.toString(resEntity, Charset.forName("utf-8"));
            }
        } catch (Exception e) {
            logger.error("ex", e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            if (client != null) {
                try {
                    client.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return result;
    }
}
