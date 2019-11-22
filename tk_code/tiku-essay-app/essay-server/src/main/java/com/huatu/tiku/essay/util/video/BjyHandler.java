package com.huatu.tiku.essay.util.video;

import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.cache.BjyRedisKeyConstant;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.util.video.pojo.VideoUploadUrl;
import com.huatu.tiku.essay.vo.video.PlayerToken;
import com.huatu.tiku.essay.vo.video.YunVideoInfo;

import lombok.extern.slf4j.Slf4j;

/**
 * 百家云相关
 */
@Service
@Slf4j
public class BjyHandler {

    String enctype = "application/x-www-form-urlencoded";
    // 获取视频token
    String GET_PLAY_TOKEN = "https://api.baijiayun.com/openapi/video/getPlayerToken";
    // 获取视频信息
    String URL_GET_VIDEO = "https://api.baijiayun.com/openapi/video/getInfo";
    // 获取视频/音频的上传地址
    String URL_VIDEO_UPLOAD = "https://api.baijiayun.com/openapi/video/getUploadUrl";

    @Resource
    private YunUtil yunUtil;

    @Resource
    private RestTemplate restTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据视频ID查询视频信息
     *
     * @param yunVideoId
     * @return
     */
    public YunVideoInfo getYunVideoInfo(Integer yunVideoId) {
        TreeMap<String, Object> treeMap = yunUtil.getParamTree();
        treeMap.put("video_id", yunVideoId);
        JSONObject jsonObject = this.postForJson(URL_GET_VIDEO, treeMap);
        YunVideoInfo yunVideoInfo = new YunVideoInfo();
        yunVideoInfo.setLength(Integer.parseInt(jsonObject.get("length").toString()));
        yunVideoInfo.setVideoStatus(Integer.parseInt(jsonObject.get("status").toString()));
        yunVideoInfo.setPrefaceUrl(String.valueOf(jsonObject.get("preface_url")));
        return yunVideoInfo;
    }

    /**
     * 根据视频ID获取token
     *
     * @param yunVideoId
     * @return
     */
    public String getToken(Integer yunVideoId) {
        if (yunVideoId == null || yunVideoId == 0) {
            return "";
        }
        String videoTokenKey = BjyRedisKeyConstant.getVideoTokenKey(yunVideoId);
        String token = (String) redisTemplate.opsForValue().get(videoTokenKey);
        if (StringUtils.isEmpty(token)) {
            TreeMap<String, Object> treeMap = yunUtil.getParamTree();
            treeMap.put("video_id", yunVideoId);
            treeMap.put("expires_in", 24 * 60 * 60);
            PlayerToken playerToken = this.postForObject(GET_PLAY_TOKEN, treeMap, PlayerToken.class);
            token = playerToken.getToken();
            //百家云token失效24小时，本地token 23小时失效
            redisTemplate.opsForValue().set(videoTokenKey, token);
            redisTemplate.expire(videoTokenKey, 23, TimeUnit.HOURS);
        }

        return token;
    }


    /**
     * @description: 百家云返回json
     * @author duanxiangchao
     * @date 2018/7/16 下午4:04
     */
    public JSONObject postForJson(String url, TreeMap<String, Object> treeMap) {
        HttpHeaders httpHeaders = new HttpHeaders();
        MediaType mediaType = MediaType.parseMediaType(enctype);
        httpHeaders.setContentType(mediaType);
        httpHeaders.add("Accept", MediaType.APPLICATION_JSON.toString());
        HttpEntity<String> formEntity = new HttpEntity<>(yunUtil.parseParams(treeMap), httpHeaders);
        String result = restTemplate.postForObject(url, formEntity, String.class);
        log.info("百家云服务调用接口地址：{},参数：{},返回值：{}", url, treeMap, result);
        JSONObject jsonObject = JSONObject.parseObject(result);
        if ("5002".equals(jsonObject.get("code").toString())) {
            log.info("视频ID有误，百家云视频信息查询失败。请检查视频ID后重试");
            throw new BizException(EssayErrors.ERROR_BJY_VIDEO_ID);
        }
        if (!"0".equals(jsonObject.get("code").toString())) {
            throw new BizException(EssayErrors.ERROR_BJY_SERVER);
        }
        return JSONObject.parseObject(jsonObject.get("data").toString());
    }

    /**
     * @description: 百家云 返回对象
     * @author duanxiangchao
     * @date 2018/7/16 下午4:01
     */
    public <T> T postForObject(String url, TreeMap<String, Object> treeMap, Class<T> clazz) {
        JSONObject jsonObject = this.postForJson(url, treeMap);
        return (T) JSONObject.parseObject(jsonObject.toJSONString(), clazz);
    }

    /**
     * @description: 百家云 返回List
     * @author duanxiangchao
     * @date 2018/7/16 下午4:02
     */
    public <T> List<T> postForObjects(TreeMap<String, Object> treeMap, String url, Class<T> clazz) {
        JSONObject jsonObject = this.postForJson(url, treeMap);
        List<T> result = JSONArray.parseArray(jsonObject.get("data").toString(), clazz);
        return result;
    }

    /**
     * 获取预上传地址
     *
     * @param answerId
     * @param
     * @return
     */
    public VideoUploadUrl getVideoUploadUrl(String answerId) {
        TreeMap<String, Object> treeMap = yunUtil.getParamTree();
        treeMap.put("file_name", "answerId" + answerId);
        treeMap.put("definition", 16); //清晰度  16:标清 1:高清 2:超清 4:720p 8:1080p 多种清晰度用英文逗号分隔
        treeMap.put("audio_with_view", 1); //是否是音频处理
        VideoUploadUrl videoUploadUrl = this.postForObject(URL_VIDEO_UPLOAD, treeMap, VideoUploadUrl.class);
        return videoUploadUrl;
    }

    /**
     * 上传音频
     *
     * @param url
     * @param file
     */
    public void uploadVideo(String url, FileSystemResource file) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("data", file);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        String result = restTemplate.postForObject(url, requestEntity, String.class);
        //判断是否成功
        JSONObject jsonObject = JSONObject.parseObject(result);
        if (!"1".equals(jsonObject.get("code").toString())) {
            throw new BizException(ErrorResult.create(1000001, jsonObject.get("msg").toString()));
        }
    }

    /**
     * 根据视频ID获取可直接播放的url
     *
     * @param videoId
     * @return
     */
    public String getVideoUrl(Integer videoId) {
        String token = getToken(videoId);
        return YunUtil.getVideoUrl(videoId, token);
    }

}
