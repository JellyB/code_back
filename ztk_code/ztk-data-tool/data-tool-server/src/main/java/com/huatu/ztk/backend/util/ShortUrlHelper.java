package com.huatu.ztk.backend.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.UUID;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huatu.ztk.backend.system.dao.SystemDao;
import com.huatu.ztk.commons.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
//import com.niusan.zmkm.ctcn.pay.tenpay.client.TenpayHttpClient;

import java.io.*;


/**
 * Created by ht on 2017/2/24.
 * source去新浪微博申请App Key：690162638
 */
public class ShortUrlHelper {

    private final static String APIKEY="690162638";

    private final static String SINA_APIURL="http://api.t.sina.com.cn/short_url/shorten.json?source=";
    /**
     * 真题组卷url
     */
    public final static String TRUEPAPER_URL="http://tiku.huatu.com/index.php?mod=administration&act=really_chouti&paper_id=";

    /**
     * 模考组卷url
     */
    public final static String TIMING_URL="http://tiku.huatu.com/index.php?mod=administration&act=timing_chouti&from=2&paper_id=";

    /**
     * 估分组卷url
     */
    public final static String ESTIMATE_URL="http://tiku.huatu.com/index.php?mod=administration&act=timing_chouti&from=5&paper_id=";


    /**
     * 得到真题的组卷的短链
     * @param long_url
     * @return
     */
    public static String getShortUrl(String long_url){
        String result = callHttp(SINA_APIURL+APIKEY+"&url_long=" + getLongUrl(long_url));
        JSONArray array = JSONArray.parseArray(result);
        JSONObject jo = array.getJSONObject(0);
        String url_short = jo.getString("url_short");
        return url_short;
    }

    /**
     * 请求，获取短链
     * @param url
     * @return
     */
    private static String callHttp(String url){
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        String result = "";
        Exception exception = null;
        try {
            client = HttpClients.createDefault();
            HttpGet e = new HttpGet(url);
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(3000).setConnectTimeout(3000).build();
            e.setConfig(requestConfig);
            response = client.execute(e);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                result = EntityUtils.toString(resEntity, Charset.forName("utf-8"));
            }
        } catch (Exception e) {
            exception = e;
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

    /**
     * 处理url中的&问题
     * @param url
     * @return
     */
    private static String getLongUrl(String url){
        String  charEncode = java.net.URLEncoder.encode("&");
        String value=url.replaceAll("&",charEncode);
        return value;
    }
    public static void main(String[] args){
        System.out.println(getShortUrl(TIMING_URL+"2003245"));
    }

}
