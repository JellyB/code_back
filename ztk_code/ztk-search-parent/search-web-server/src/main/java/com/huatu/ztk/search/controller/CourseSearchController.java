package com.huatu.ztk.search.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.search.service.CourseKeywordService;
import com.huatu.ztk.search.service.HotwordService;
import com.huatu.ztk.user.service.UserSessionService;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 课程搜索控制层
 * Created by shaojieyue
 * Created time 2016-09-06 20:38
 */

@RestController
@RequestMapping(value = "/v1/search/course", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class CourseSearchController {
    private static final Logger logger = LoggerFactory.getLogger(CourseSearchController.class);
    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private CourseKeywordService courseKeywordService;

    @Autowired
    private HotwordService hotwordService;

    /**
     * 保存关键字搜索记录
     *
     * @param token
     * @param q     搜索关键字
     * @throws BizException
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public void saveKeywords(@RequestHeader(required = false) String token, @RequestParam String q) throws BizException {
        logger.info("/    GET");
        userSessionService.assertSession(token);
        //取得用户ID
        long userId = userSessionService.getUid(token);
        final int catgory = userSessionService.getCatgory(token);
        courseKeywordService.save(userId, catgory, q);
    }

    /**
     * 查询热搜词及我的关键字搜索记录
     *
     * @param token
     * @throws BizException
     */
    @RequestMapping(value = "/keywords", method = RequestMethod.GET)
    public Object queryKeywords(@RequestHeader(required = false, defaultValue = "") String token) throws BizException {
        logger.info("/keywords    GET");
        Map data = Maps.newHashMap();
        if(StringUtils.isNotBlank(token)){
            userSessionService.assertSession(token);
            //取得用户ID
            long userId = userSessionService.getUid(token);
            final int catgory = userSessionService.getCatgory(token);

            //我的搜索历史
            List<String> mywords = courseKeywordService.queryMyWords(userId, catgory);
            data.put("mywords", mywords);
        }else{
            data.put("mywords", Lists.newArrayList());
        }
        //热搜词
        List<String> hotwords = hotwordService.query(1);
        data.put("hotwords", hotwords);
        return data;
    }

    /**
     * 搜索关键词 up
     * @param token
     * @param keyWord
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "/keywords/up", method = RequestMethod.GET)
    public Object keyWordUp(@RequestHeader String token,
            @RequestParam String keyWord) throws BizException {
        userSessionService.assertSession(token);
        long userId = userSessionService.getUid(token);
        final int catgory = userSessionService.getCatgory(token);
        courseKeywordService.save(userId, catgory, keyWord);
        return SuccessMessage.create("success");
    }

    /**
     * 删除搜索关键字
     *
     * @param token
     * @throws BizException
     */
    @RequestMapping(value = "/keywords", method = RequestMethod.DELETE)
    public Object deleteKeywords(@RequestHeader(required = false) String token, @RequestParam String q) throws BizException {
        logger.info("/keywords    DELETE");
        userSessionService.assertSession(token);
        //取得用户ID
        long userId = userSessionService.getUid(token);
        final int catgory = userSessionService.getCatgory(token);
        courseKeywordService.delete(userId, catgory, q);
        return SuccessMessage.create("删除成功");
    }

    /**
     * 清空某用户搜索记录
     *
     * @param token
     * @throws BizException
     */
    @RequestMapping(value = "/keywords/record", method = RequestMethod.DELETE)
    public Object clearKeywords(@RequestHeader(required = false) String token) throws BizException {
        logger.info("/keywords/record    DELETE");
        userSessionService.assertSession(token);
        //取得用户ID
        long userId = userSessionService.getUid(token);
        final int catgory = userSessionService.getCatgory(token);
        courseKeywordService.clearAllKeywords(userId, catgory);
        return SuccessMessage.create("清空成功");
    }


}
