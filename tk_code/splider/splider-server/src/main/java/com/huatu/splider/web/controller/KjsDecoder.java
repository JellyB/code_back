package com.huatu.splider.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * #T51nGz0ne$
 * @author hanchao
 * @date 2018/2/28 13:48
 */
@RestController
@RequestMapping("/17kjs")
@Slf4j
public class KjsDecoder {
    private String sKey = "ts1ngz0ne2015NO1";//key，可自行修改
    private String ivParameter = "0102030405060708";//偏移量,可自行修改
    @Autowired
    private OkHttpClient okHttpClient;

    @PostMapping("/decode")
    public Object decode(@RequestBody String body) throws Exception {
        if (body.startsWith("body=")) {
            body = body.substring(5, body.length());
        }
        body = body.replaceAll("\\\\n", "");
        body = body.replaceAll("\\\\", "");
        return JSON.parse(decrypt(body));
    }

    //代理17kjs,方便输出日志
    @RequestMapping("/**")
    public void proxy(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println();
        System.out.println();
        System.out.println("=============================================================");


        StringBuffer url = new StringBuffer("http://api.17kjs.com");
        url.append(request.getRequestURI().replaceFirst("/17kjs", ""));

        String method = request.getMethod();

        Request.Builder builder = new Request.Builder().url(url.toString());
        ServletServerHttpRequest nativeRequest = new ServletServerHttpRequest(request);

        System.out.println("request url : "+url);


        do {
            if (method.equalsIgnoreCase("GET")) {
                builder = builder.get();
                break;
            }

            String body = IOUtils.toString(nativeRequest.getBody());
            String contentType = StringUtils.join(nativeRequest.getHeaders().get(HttpHeaders.CONTENT_TYPE),";");

            try {
                //输出请求
                //先判断是否json体

                JSONObject requestJson = JSON.parseObject(body, JSONObject.class);
                System.out.println("request body : "+decryptPretty(requestJson.getString("body")));

            } catch(Exception e){
                System.out.println("requeset body : "+new String(body));
                e.printStackTrace();
            }

            okhttp3.RequestBody requestBody = okhttp3.RequestBody.create(MediaType.parse(contentType),body);

            if(method.equalsIgnoreCase("POST")){
                builder = builder.post(requestBody);
            }else if(method.equalsIgnoreCase("PUT")){
                builder = builder.put(requestBody);
            }else if(method.equalsIgnoreCase("PATCH")){
                builder = builder.patch(requestBody);
            }else if(method.equalsIgnoreCase("DELETE")){
                builder = builder.delete(requestBody);
            }else{
                throw new IllegalStateException("未开发");
            }

        } while (false);

        HttpHeaders headers = nativeRequest.getHeaders();

        System.out.println("requeset headers : "+JSON.toJSONString(headers,SerializerFeature.PrettyFormat));

//        builder.headers(Headers.of(headers.keySet().stream().filter(x -> {
//            return  !x.equalsIgnoreCase(HttpHeaders.CONTENT_TYPE) &&
//                    !x.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH) &&
//                    !x.equalsIgnoreCase(HttpHeaders.HOST) &&
//                    !x.equalsIgnoreCase(HttpHeaders.ACCEPT_ENCODING);
//        }).collect(Collectors.toMap(Function.identity(),k -> {
//            List<String> vs = headers.get(k);
//            System.out.println(k+","+StringUtils.join(vs,";"));
//            return StringUtils.join(vs,";");
//        }))));

        Response responseProxy = okHttpClient.newCall(builder.build()).execute();
        byte[] responseBody = responseProxy.body().bytes();

        try {
            //输出响应
            JSONObject requestJson = JSON.parseObject(responseBody, JSONObject.class);
            System.out.println("response body : "+decryptPretty(requestJson.getString("body")));
        } catch(Exception e){
            e.printStackTrace();
        }


        System.out.println("response headers : "+JSON.toJSONString(responseProxy.headers().toMultimap(),SerializerFeature.PrettyFormat));


        responseProxy.headers().names().forEach(k -> {
            response.setHeader(k,StringUtils.join(headers.get(k),";"));
        });
        response.setStatus(responseProxy.code());
        IOUtils.copy(new ByteArrayInputStream(responseBody),response.getOutputStream());
        responseProxy.close();
        response.getOutputStream().flush();


        System.out.println("=============================================================");
        System.out.println();
        System.out.println();
    }

    private String decryptPretty(String sSrc) throws Exception {
        return JSON.toJSONString(JSON.parseObject(decrypt(sSrc)), SerializerFeature.PrettyFormat);
    }

    //解密
    private String decrypt(String sSrc) throws Exception {
        try {
            byte[] raw = sKey.getBytes("ASCII");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] encrypted1 = new BASE64Decoder().decodeBuffer(sSrc);// 先用base64解密
            byte[] original = cipher.doFinal(encrypted1);
            String originalString = new String(original, "utf-8");
            return originalString;
        } catch (Exception ex) {
            return null;
        }
    }

    // 加密
    private String encrypt(String sSrc) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        byte[] raw = sKey.getBytes();
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());// 使用CBC模式，需要一个向量iv，可增加加密算法的强度
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(sSrc.getBytes("utf-8"));
        return new BASE64Encoder().encode(encrypted);// 此处使用BASE64做转码。
    }
}
