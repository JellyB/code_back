package com.huatu.ztk.backend.teacher.service;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.huatu.ztk.backend.question.service.QuestionService;
import com.huatu.ztk.backend.teacher.utils.FileTypeUtils;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.user.common.AvatarFileType;
import com.huatu.ztk.user.common.UserErrors;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Author: xuhuiqiang
 * Time: 2017-05-17  15:32 .
 */
@Service
public class avatarUpload {
    public static final Logger logger = LoggerFactory.getLogger(avatarUpload.class);

    @Autowired
    private QuestionService questionService;


    //文件上传的最大大小300k
    private static final long MAX_FILE_SIZE = 1024 * 300;

    //上传地址
    private static final String AVATAR_UPLOAD_URL = "http://192.168.100.31:12458/upload";

    //头像url前缀
    private static final String AVATAR_BASE_URL = "http://tiku.huatu.com/cdn/zimg";

    public String upload(String str) throws IOException, BizException {
        Map<String,Object> result = JsonUtil.toMap(str);
        String data = "";
        String name = "";
        String url = null;
        if(result.get("ztk")!=null&&result.get("name")!=null){
            data = String.valueOf(result.get("ztk"));
            name = String.valueOf(result.get("name"));
            String[] sourceStrArray = data.split(";base64,");//提取64位码
            String imgBase = sourceStrArray[1];
            url = uploadAvatar(imgBase,name);
        }
        return url;
    }

    /**
     *
     * @param str
     * @return
     * @throws BizException
     * @throws IOException
     */
    public String uploadAvatar(String str,String name) throws BizException, IOException {
        questionService.GenerateImage(str,name);
        File file = new File(name);
        BufferedInputStream image = new BufferedInputStream(new FileInputStream(file));

        //定义返回头像url
        String url = "";

        //限制上传头像大小
        if (image.available() > MAX_FILE_SIZE) {
            throw new BizException(UserErrors.AVATAR_FILE_TOO_LARGE);
        }

        //限制上传头像图片类型
        final AvatarFileType avatarFileType = checkAvatarType(image);
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        String result = "";
        try {
            //allowed_type = {'jpeg', 'jpg', 'png', 'gif', 'webp'}
            final ContentType contentType = ContentType.create(avatarFileType.getSuffix());
            InputStreamEntity inputStreamEntity = new InputStreamEntity(image,image.available(), contentType);
            client = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(AVATAR_UPLOAD_URL);
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(3000).setConnectTimeout(3000).build();
            httpPost.setConfig(requestConfig);
            //封装请求内容
            httpPost.setEntity(inputStreamEntity);
            //执行请求
            response = client.execute(httpPost);
            //获取请求相应对象
            HttpEntity respEntity = response.getEntity();
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new BizException(UserErrors.UPLOAD_AVATAR_FAIL);
            }
            result = EntityUtils.toString(respEntity, Charset.forName("ISO-8859-1"));
            logger.info("result={}",result);
            JsonParser parser = new JsonParser();
            final JsonObject jsonObject = parser.parse(result).getAsJsonObject();
            if (!jsonObject.getAsJsonPrimitive("ret").getAsBoolean()) {//上传失败
                throw new BizException(UserErrors.UPLOAD_AVATAR_FAIL);
            }

            String fileName = jsonObject.getAsJsonObject("info").getAsJsonPrimitive("md5").getAsString();
            logger.info("filename={}",fileName);
            //组装完整url
            url = makeUrl(fileName);
            logger.info("url={}",url);
        } catch (Exception e) {
            logger.error("upload fail={}", e);
            throw new BizException(UserErrors.UPLOAD_AVATAR_FAIL);
        }
        logger.info("url={}",url);
        return url;
    }


    /**
     * 限制上传头像图片类型
     *
     * @throws BizException
     */
    private AvatarFileType checkAvatarType(BufferedInputStream inputStream) throws BizException {
        AvatarFileType type = null;
        try {
            type = FileTypeUtils.getType(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (type == null) {
            throw new BizException(UserErrors.IMG_TYPE_NOT_SUPPORT);
        }
        return type;
    }


    /**
     * 简单组装头像url
     *
     * @param md5Str
     * @return
     */
    private String makeUrl(String md5Str) {
        return AVATAR_BASE_URL + "/" + md5Str;
    }


}
