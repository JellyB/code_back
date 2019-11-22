package com.huatu.ztk.scm.util;

//import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
//import org.apache.commons.httpclient.HttpClient;
//import org.apache.commons.httpclient.HttpStatus;
//import org.apache.commons.httpclient.NameValuePair;
//import org.apache.commons.httpclient.methods.PostMethod;
//import org.apache.commons.httpclient.params.HttpMethodParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lihua on 15/9/17.
 */
public class MailUtil {
    private static final String EMAILURL = "http://10.13.81.10:10001/mail";
    private static final String SMSURL   = "http://10.10.76.79:8800/sms/send/";
    private static final Logger logger   = LoggerFactory.getLogger(MailUtil.class);

//    public static boolean sendEmail(String appid, String to, String subject, String content) {
//        HttpClient client = null;
//        PostMethod method = null;
//        try {
//            client = new HttpClient();
//            method = new PostMethod(EMAILURL);
//            method.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");
//            method.addParameters(new NameValuePair[] {
//              new NameValuePair("appId", appid),
//              new NameValuePair("to", to),
//                    new NameValuePair("subject", subject),
//                    new NameValuePair("content", content),
//            });
//            client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
//            client.getHttpConnectionManager().getParams().setSoTimeout(5000);
//            method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
//            int http_status = client.executeMethod(method);
//            if (http_status == HttpStatus.SC_OK) {
//                return true;
//            }
//        }catch (Exception e) {
//            logger.info("server=" + e);
//            return false;
//        }finally {
//            method.releaseConnection();
//        }
//        return false;
//    }
//
//
//    public static boolean sendMobileMsg(String number,String message){
//        HttpClient client = null;
//        PostMethod method = null;
//        try {
//            client = new HttpClient();
//            method = new PostMethod(SMSURL);
//            method.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");
//            method.addParameters(new NameValuePair[]{
//                    new NameValuePair("NUM", number),
//                    new NameValuePair("conn", message),
//            });
//            client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
//            client.getHttpConnectionManager().getParams().setSoTimeout(5000);
//            method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
//            int http_status = client.executeMethod(method);
//            if (http_status == HttpStatus.SC_OK) {
//                return true;
//            }
//        }catch (Exception e) {
//            logger.info("server=" + e);
//            return false;
//        }finally {
//            method.releaseConnection();
//        }
//        return false;
//    }


}
