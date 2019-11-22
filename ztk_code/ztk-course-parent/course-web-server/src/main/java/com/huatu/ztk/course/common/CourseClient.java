package com.huatu.ztk.course.common;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.course.bean.NetSchoolResponse;
import com.huatu.ztk.course.utils.Crypt3Des;
import com.huatu.ztk.course.utils.ParamsUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by shaojieyue
 * Created time 2016-11-23 09:11
 */

@Component
public class CourseClient {
    private static final Logger logger = LoggerFactory.getLogger(CourseClient.class);

    CloseableHttpClient httpClient = null;

    /**
     * 允许发送时间最大阀值
     */
    public static final String APITK_HUATU_COM = "apitk.huatu.com";

    /**
     * apitk网关地址
     */
    public static volatile String APITK_GW = APITK_HUATU_COM;


    /**
     * 域名解析是否在执行
     */
    public static volatile boolean running = false;

    public static final int CONNECTION_GET_TIMEOUT = 10000;//连接池获取连接超时时间
    public static final int CONNECTION_TIMEOUT = 5000;//建立连接超时时间
    public static final int READ_TIMEOUT = 15000;//等待响应超时时间

    static {
        //加载时,解析apitk ip
        //fetchNewApitkGwIp();
    }


    public CourseClient() throws Exception{
        HttpClientBuilder b = HttpClientBuilder.create();
        SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (arg0, arg1) -> true).build();
        b.setSslcontext(sslContext);

        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, new AllowAllHostnameVerifier());
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslSocketFactory)
                .build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        // Increase max total connection to 200
        connectionManager.setMaxTotal(300);
        // Increase default max connection per route to 20
        connectionManager.setDefaultMaxPerRoute(300);
        // Increase max connections for localhost:80 to 50

        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(CONNECTION_GET_TIMEOUT).
                setConnectTimeout(CONNECTION_TIMEOUT).setSocketTimeout(READ_TIMEOUT).build();

        //HttpHost vhuatucom = new HttpHost("v.huatu.com", 80);
        //HttpHost huatuHttpsHost = new HttpHost("apitk.huatu.com", 443, "http");
        //connectionManager.setMaxPerRoute(new HttpRoute(huatuHttpsHost), 100);
        b.setConnectionManager(connectionManager);
        b.setDefaultRequestConfig(requestConfig);
        httpClient = b.build();
    }

    /**
     * 获得json数据
     *
     * @param url
     * @param needDecrypt data是否需要解密
     * @param params
     * @return
     * @throws IOException
     * @throws BizException
     */
    public Object getJson(String url, Map<String, Object> params, boolean needDecrypt) throws IOException, BizException {
        String resultData = getHttpData(url, params);

        if (StringUtils.isBlank(resultData)) {
            return null;
        }

        boolean isJson = true;
        try {
            JsonElement jsonElement = new JsonParser().parse(resultData);
            isJson = jsonElement.isJsonObject();
        } catch (Exception e) {
            logger.info("gson parse data error,url={},params={}", url, params, e);
        }

        Object data = null;
        if (isJson) {  //如果返回数据为json形式
            NetSchoolResponse netSchoolResponse = JsonUtil.toObject(resultData, NetSchoolResponse.class);

            if (netSchoolResponse.getCode() != NetSchoolConfig.SUCCESS_CODE) {
                final ErrorResult errorResult = ErrorResult.create(netSchoolResponse.getCode(), netSchoolResponse.getMsg());
                if (netSchoolResponse.getData() != null) {
                    errorResult.setData(netSchoolResponse.getData());
                }
                throw new BizException(errorResult);
            } else {
                //只取data部分
                data = netSchoolResponse.getData();

                //code为1，但是没有data或者data为空字符串
                if (data == null || (data != null && StringUtils.isBlank(data.toString()))) {
                    return SuccessMessage.create(netSchoolResponse.getMsg());
                }

                //需要解密的
                if (needDecrypt) {
                    String json = Crypt3Des.decryptMode(String.valueOf(data));
                    data = JsonUtil.toMap(json);
                }
            }
        } else {  //加密的json数据
            data = JsonUtil.toMap(Crypt3Des.decryptMode(resultData));
            logger.info("not a json response,{},{},{}...",url,params,needDecrypt);
        }
        return data;
    }

    /**
     * http GET请求数据
     *
     * @param url
     * @return
     * @throws IOException
     */
    public String getHttpData(String url,Map<String,Object> params) throws IOException {
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
                    uriBuilder.addParameter(entry.getKey(),entry.getValue().toString());
                }
            }
        }
        HttpGet httpGet = null;
        URI uri = null;
        try {
            uri = uriBuilder.build();
            //将域名替换成ip
            String newUrl = uri.toString().replace(APITK_HUATU_COM, APITK_GW);
            httpGet = new HttpGet(newUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        String resultData = "";
        try {
            long stime = System.currentTimeMillis();

            //httpGet.setConfig(requestConfig);
            final CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity resEntity = httpResponse.getEntity();


            if (resEntity != null) {
                resultData = EntityUtils.toString(resEntity, Charset.forName("utf-8"));
            }

            long etime = System.currentTimeMillis();
            logger.info("url={} , utime>>{}", uri, etime - stime);
        } catch (ConnectionPoolTimeoutException e) {
            logger.warn("url:{},{},ex", url,params,e);
        } catch (SocketTimeoutException e) {
            logger.warn("url:{},{},ex", url,params,e);
        } catch (UnknownHostException e) {
            //出错重新取ip
            logger.warn("ex", e);
            //fetchNewApitkGwIp();
        }
        return resultData;
    }


    /**
     * 请求参数是加密的json
     *
     * @param parameterMap
     * @param basicUrl
     * @param needDecrypt
     * @return
     * @throws Exception
     */
    public Object getJsonByEncryptJsonParams(Map<String, Object> parameterMap, String basicUrl, boolean needDecrypt) throws Exception {
        String params = ParamsUtils.makeJsonParams(parameterMap);
        //加密
        String encryptparams = Crypt3Des.encryptMode(params);

        logger.info("params={},encrypt={}", params, encryptparams);
        return getJson(basicUrl + "?p=" + encryptparams, null, needDecrypt);
    }

    /**
     * 请求参数是加密的,如aa=1&bb=2
     *
     * @param parameterMap
     * @param basicUrl
     * @param needDecrypt
     * @return
     * @throws Exception
     */
    public Object getJsonByEncryptParams(Map<String, Object> parameterMap, String basicUrl, boolean needDecrypt) throws Exception {
        String params = ParamsUtils.makeParams(parameterMap);
        //加密
        String encryptparams = Crypt3Des.encryptMode(params);

        logger.info("params={},encrypt={}", params, encryptparams);
        return getJson(basicUrl + "?p=" + encryptparams, null, needDecrypt);
    }


    /**
     * 抓取新的apitk网关地址
     */
    /*private static void fetchNewApitkGwIp() {
        if (running) {
            return;
        }
        running = true;//开始执行,更改状态
        new Thread(new Runnable() {//异步线程处理,保证不影响接口调用速度
            @Override
            public void run() {
                for (int i = 0; i < 3; i++) {
                    try {

                        InetAddress address = InetAddress.getByName(APITK_HUATU_COM);
                        if (address == null && address.getHostAddress() == null) {
                            continue;//没有解析出来继续尝试
                        }

                        APITK_GW = address.getHostAddress();
                        logger.info("new apitk gw address={}", APITK_GW);
                        break;
                    } catch (Exception e) {
                    }

                    try {
                        //获取失败则sleep 继续重试
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }

                running = false;//结束执行,释放锁
            }
        }).start();
    }*/


    /**
     * http post请求数据
     *
     * @param url
     * @return
     * @throws IOException
     */
    public String postHttpDataByStringEntity(String url,Map<String,Object> params) throws IOException {
        HttpPost post = new HttpPost(url);
        String resultData = "";
        try {

            //设置连接超时为10秒
            //RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(10000).build();
            //post.setConfig(requestConfig);

            StringEntity entity = new StringEntity(JsonUtil.toJson(params), Charset.forName("utf-8"));

            post.setEntity(entity);

            final CloseableHttpResponse httpResponse = httpClient.execute(post);
            HttpEntity resEntity = httpResponse.getEntity();

            if (resEntity != null) {
                resultData = EntityUtils.toString(resEntity, Charset.forName("utf-8"));
            }
        } catch (Exception e) {
            throw e;
        }
        return resultData;
    }


    /**
     * http post请求数据
     *
     * @param url
     * @return
     * @throws IOException
     */
    public String postHttpDataByFormEntity(String url,Map<String,Object> params) throws IOException {
        HttpPost post = new HttpPost(url);
        String resultData = "";
        try {

            //设置连接超时为10秒
            //RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(10000).build();
            //post.setConfig(requestConfig);

            List<NameValuePair> list = new ArrayList();
            if (MapUtils.isNotEmpty(params)) {
                //添加参数
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    if (entry.getValue() != null) {
                        list.add(new BasicNameValuePair(entry.getKey(),entry.getValue().toString()));
                    }
                }
            }

            post.setEntity(new UrlEncodedFormEntity(list, Charset.forName("utf-8")));

            final CloseableHttpResponse httpResponse = httpClient.execute(post);
            HttpEntity resEntity = httpResponse.getEntity();

            if (resEntity != null) {
                resultData = EntityUtils.toString(resEntity, Charset.forName("utf-8"));
            }
        } catch (Exception e) {
            throw e;
        }
        return resultData;
    }


}