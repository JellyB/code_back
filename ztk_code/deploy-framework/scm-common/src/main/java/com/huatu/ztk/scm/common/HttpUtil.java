package com.huatu.ztk.scm.common;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * 基于apache httpclient 的工具包
 * User: shaojieyue
 * Date: 10/17/13
 * Time: 11:04 AM
 */
public class HttpUtil {


    /**
     * http post获取资源
     * @param path
     * @param requestPayload
     * @return
     * @throws IOException
     */
    public static String httpPost(String path,String requestPayload) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(path);
        httpPost.setHeader("Content-type","application/json; charset=UTF-8");
        if(requestPayload != null){
            HttpEntity entity = new StringEntity(requestPayload);
            httpPost.setEntity(entity);
        }
        CloseableHttpResponse response = httpclient.execute(httpPost);
        String content = responseToStr(response);
        response.close();
        return content;
    }

    /**
     * http post获取资源
     * @param path
     * @return
     * @throws IOException
     */
    public static String httpPost(String path) throws IOException {
        return httpPost(path,null);
    }

    /**
     * http get获取资源
     * @param path
     * @param params
     * @return
     * @throws java.io.IOException
     */
    public static String httpGet(String path,Map params) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String url = buildUrl(path,params);
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Content-type","application/json; charset=UTF-8");
        CloseableHttpResponse response = httpclient.execute(httpGet);
        String content = responseToStr(response);
        response.close();
        return content;
    }
  /**
   * http get获取资源
   * @param path
   * @return
   * @throws java.io.IOException
   */
  public static String[] httpGetAll(String path) throws IOException {
    CloseableHttpClient httpclient = HttpClients.createDefault();
    String[] res = new String[2];
    HttpGet httpGet = new HttpGet(path);
    httpGet.setHeader("Content-type","application/json; charset=UTF-8");
    CloseableHttpResponse response = httpclient.execute(httpGet);
    res[0]= response.getStatusLine().getStatusCode()+"";
    res[1]=responseToStr(response);
    response.close();

    return res;
  }
    /**
     * http get获取资源
     * @param path
     * @return
     * @throws java.io.IOException
     */
    public static String httpGet(String path) throws IOException {
        return httpGet(path,null);
    }

    /**
     * 将CloseableHttpResponse里的内容转为String
     * @param response
     * @return
     */
    private static String responseToStr(CloseableHttpResponse response){
        String content = null;
        try {
            HttpEntity entity = response.getEntity();
            if(entity!=null){
                content = IOUtils.toString(entity.getContent());
                EntityUtils.consume(entity);
            }else {
                content = response.getStatusLine().toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    /**
     * 创建带参数的url
     * @param path 待访问url
     * @param params 参数
     * @return
     */
    private static String buildUrl(String path,Map<String,String> params){
        URIBuilder uri = new URIBuilder();
        uri.setPath(path);
        if(params!=null){
            Iterator<String> iter = params.keySet().iterator();
            String key = null;
            while(iter.hasNext()){
                key = iter.next();
                uri.setParameter(key,params.get(key));
            }
        }
        return uri.toString();
    }
}
