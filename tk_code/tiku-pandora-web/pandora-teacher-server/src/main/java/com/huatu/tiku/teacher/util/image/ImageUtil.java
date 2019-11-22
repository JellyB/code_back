package com.huatu.tiku.teacher.util.image;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.huatu.tiku.teacher.util.file.CommonFileUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.lowagie.text.Image;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by huangqingpeng on 2018/11/5.
 */
@Slf4j
public class ImageUtil {
    //缓存试题image用于word写入
    public static final Cache<String, Image> LOWAGIE_IMAGE_CACHE =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(1, TimeUnit.HOURS)//缓存时间
                    .maximumSize(2)
                    .build();

    public static Image getImage(String imageUrl){
        Image image = LOWAGIE_IMAGE_CACHE.getIfPresent(imageUrl);
        if(null == image){
            return save(imageUrl);
        }
        return image;
    }

    /**
     * 缓存字符串中所有的涉及到图片
     * @param content
     */
    public static void save(StringBuilder content){
        Pattern pattern = Pattern.compile("<img[^>]>");
        Matcher matcher = pattern.matcher(content);
        int index = 0;
        while (matcher.find(index)){
            String image = matcher.group();
            new Thread(() -> {
                try {
                    Image src = save(CommonFileUtil.subAttrString(image, "src"));
                    if(null!=src){
                        System.out.println("缓存图片成功，src = " + CommonFileUtil.subAttrString(image, "src"));
                    }
                } catch (BizException e) {
                    e.printStackTrace();
                }
            }).start();
            index = matcher.end();
        }
    }
    private static Image save(String imageUrl) {
        try {
            Image image = Image.getInstance(new URL(imageUrl));
            saveImage(imageUrl,image);
            return image;
        } catch (Exception e) {
            log.error("无效的图片地址：imgUrl ={}", imageUrl);
        }
        return null;
    }

    private static void saveImage(String imageUrl, Image image) {
        LOWAGIE_IMAGE_CACHE.put(imageUrl,image);
    }
}
