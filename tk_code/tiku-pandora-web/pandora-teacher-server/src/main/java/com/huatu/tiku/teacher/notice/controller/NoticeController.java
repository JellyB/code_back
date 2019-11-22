package com.huatu.tiku.teacher.notice.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.teacher.notice.constant.PushResponse;
import com.huatu.tiku.teacher.notice.service.fall.NoticeFeignFallback;
import com.huatu.tiku.teacher.notice.service.feign.NoticeFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
 * 描述：
 *
 * @author biguodong
 * Create time 2018-11-06 下午4:39
 **/

@RestController
@RequestMapping(value = "notice")
@Slf4j
public class NoticeController {

    @Autowired
    private NoticeFeignClient noticeFeignClient;

    @Autowired
    private NoticeFeignFallback noticeFeignFallback;



    /**
     * 获取我的消息列表数据
     * @param userId
     * @param page
     * @param size
     * @return
     */
    @GetMapping(value = "{userId}")
    public Object list(@PathVariable(value = "userId") int userId,
                       @RequestParam(value = "page", defaultValue = "1") int page,
                       @RequestParam(value = "size", defaultValue = "20") int size){

        Map<String, Object> params = Maps.newHashMap();
        params.put("userId", userId);
        params.put("page", page);
        params.put("size", size);
        log.info("user noticeList obtain form proxy");
        PushResponse pushResponse = noticeFeignClient.noticeList(params);
        if(null != pushResponse.getData()){
            noticeFeignFallback.put(userId, pushResponse);
        }
        log.info("notice list:{}", JSONObject.toJSONString(pushResponse.getData()));
        return pushResponse.getData();
    }

    /**
     * 获取我的消息未读数
     * @param userId
     * @return
     */
    @GetMapping(value = "unRead/{userId}")
    public Object list(@PathVariable(value = "userId") int userId){
        Map<String, Object> params = Maps.newHashMap();
        params.put("userId", userId);
        log.info("user unRead notice count obtain form proxy");
        PushResponse pushResponse = noticeFeignClient.unReadCount(params);
        return pushResponse.getData();
    }

    @PutMapping("read/{noticeId}")
    public Object read(@PathVariable long noticeId) throws BizException{
        Map<String, Object> params = Maps.newHashMap();
        params.put("noticeId", noticeId);
        log.info("user read notice form proxy");
        PushResponse pushResponse = noticeFeignClient.hasRead(params);
        return pushResponse.getData();
    }
}
