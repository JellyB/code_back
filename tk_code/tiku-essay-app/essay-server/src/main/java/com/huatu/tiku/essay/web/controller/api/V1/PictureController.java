package com.huatu.tiku.essay.web.controller.api.V1;

import com.google.common.collect.Maps;
import com.huatu.tiku.essay.vo.resp.PictureUrlVO;
import com.huatu.tiku.essay.util.file.UploadFileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static com.huatu.tiku.essay.util.file.FunFileUtils.PICTURE_SAVE_PATH;
import static com.huatu.tiku.essay.util.file.FunFileUtils.PICTURE_SAVE_URL;

/**
 * create by jbzm 171219
 */
@RestController
@Slf4j
@RequestMapping("api/v1/picture")
public class PictureController {

    @Autowired
    private UploadFileUtil uploadFileUtil;
//    @Autowired
//    private RestTemplate restTemplate;
    static  HttpHeaders headers = new HttpHeaders();
 static MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");


    @RequestMapping(value = "upload", method = RequestMethod.POST)
    public Object uploadPicture(MultipartHttpServletRequest request) throws Exception {

        MultipartFile multipartFile = request.getFile("test");
        String fileName = UUID.randomUUID().toString() + ".png";
        uploadFileUtil.ftpUploadFileInputStream(multipartFile.getInputStream(), fileName, PICTURE_SAVE_PATH);
        PictureUrlVO pictureUrlVO = new PictureUrlVO();
        pictureUrlVO.setUrl(PICTURE_SAVE_URL + fileName);
        log.info("picture upload:" + PICTURE_SAVE_URL + fileName);
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        //设置参数
        Map<String, String> hashMap = new LinkedHashMap<String, String>();
        hashMap.put("url", pictureUrlVO.getUrl());
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<Map<String, String>>(hashMap, headers);

        //执行请求
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> resp = restTemplate.exchange("http://39.106.163.36:9999/upload", HttpMethod.POST,requestEntity, String.class);
        String str = resp.getBody();
        log.info("-----result----" + str);

        String utf8 = new String(str.getBytes("iso-8859-1"), "utf-8");
        Map m = Maps.newHashMap();
        m.put("result",utf8);
        return m;
    }

    @RequestMapping(value = "test", method = RequestMethod.GET)
    public Object test(HttpServletRequest request,String url) throws Exception {

        //设置参数
        Map<String, String> hashMap = new LinkedHashMap<String, String>();
        hashMap.put("url", url);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<Map<String, String>>(hashMap, headers);

//执行请求
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> resp = restTemplate.exchange("http://39.106.163.36:9999/upload", HttpMethod.POST,requestEntity, String.class);
String str = resp.getBody();
        log.info("-----result----" + str);

        String utf8 = new String(str.getBytes("iso-8859-1"), "utf-8");
        log.info(utf8);

        return utf8;
    }
}
