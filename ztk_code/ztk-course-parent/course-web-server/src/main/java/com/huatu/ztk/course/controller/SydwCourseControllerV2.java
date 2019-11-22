package com.huatu.ztk.course.controller;

import com.google.common.collect.Maps;
import com.huatu.ztk.commons.CatgoryType;
import com.huatu.ztk.course.common.NetSchoolConfig;
import com.huatu.ztk.course.common.NetSchoolSydwUrl;
import com.huatu.ztk.course.service.CourseService;
import com.huatu.ztk.user.service.UserSessionService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;


/**
 * 课程
 * Created by linkang on 11/25/16.
 */

@RestController
@RequestMapping(value = "v2/courses/sydw")
public class SydwCourseControllerV2 {
    private static final Logger logger = LoggerFactory.getLogger(SydwCourseControllerV2.class);

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserSessionService userSessionService;

    /**
     * 全部直播列表接口
     *
     * @param orderid    排序属性
     * @param dateid     考试日期筛选
     * @param priceid    按照价格筛选
     * @param page       分页数
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "list", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public Object list(@RequestParam int orderid,
                       @RequestParam int dateid,
                       @RequestParam int priceid,
                       @RequestParam int page,
                       @RequestParam(required = false) String shortTitle,
                       @RequestHeader(required = false) String token) throws Exception {
        userSessionService.assertSession(token);
        String username = userSessionService.getUname(token);
        int catgory = userSessionService.getCatgory(token);

        final HashMap<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("orderid", orderid);
        paramMap.put("categoryid", getNetSchoolCategoryId(catgory));
        paramMap.put("dateid", dateid);
        paramMap.put("priceid", priceid);
        paramMap.put("page", page);
        paramMap.put("username", username);
        paramMap.put("shortTitle", shortTitle);

        return courseService.getJson(paramMap, getListUrl(shortTitle), false);
    }

    private String getListUrl(String shortTitle) {
        return StringUtils.isBlank(shortTitle) ? NetSchoolSydwUrl.SYDW_ALL_COLLECTION_LIST : NetSchoolSydwUrl.SYDW_TOTAL_LIST;
    }

    /**
     * 我的直播
     *
     * @param token
     * @param order 需要显示的课程，1：全部课程，2：未开始课程，3：进行中课程，4：已结束课程
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "myList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public Object mylist(@RequestHeader(required = false) String token,
                         @RequestParam int order,
                         @RequestHeader int terminal,
                         @RequestHeader String cv) throws Exception {
        userSessionService.assertSession(token);
        String username = userSessionService.getUname(token);
        int catgory = userSessionService.getCatgory(token);

        final HashMap<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("username", username);
        paramMap.put("order", order);
        paramMap.put("categoryid", getNetSchoolCategoryId(catgory));
        return courseService.getJson(paramMap, NetSchoolSydwUrl.SYDW_MY_LIVE_SUIT_LIST, false);
    }

    /**
     * 直播搜索
     *
     * @param token
     * @param page     分页
     * @param keywords 关键词
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "search", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public Object search(@RequestHeader(required = false) String token,
                         @RequestParam int page,
                         @RequestParam String keywords) throws Exception {
        userSessionService.assertSession(token);
        String username = userSessionService.getUname(token);
        int catgory = userSessionService.getCatgory(token);


        final HashMap<String, Object> parameterMap = Maps.newHashMap();
        parameterMap.put("username", username);
        parameterMap.put("page", page);
        //多个空格转为空字符串
        parameterMap.put("keywords", StringUtils.trimToEmpty(keywords));
        parameterMap.put("categoryid", getNetSchoolCategoryId(catgory));

        return courseService.getJson(parameterMap, NetSchoolSydwUrl.SYDW_ALL_COLLECTION_LIST, false);
    }


    /**
     * 我的直播列表搜索接口
     *
     * @param token
     * @param keywords 关键词
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "mySearch", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public Object mySearch(@RequestHeader(required = false) String token,
                           @RequestParam String keywords) throws Exception {
        userSessionService.assertSession(token);
        String username = userSessionService.getUname(token);
        int catgory = userSessionService.getCatgory(token);

        final HashMap<String, Object> parameterMap = Maps.newHashMap();
        parameterMap.put("username", username);
        //多个空格转为空字符串
        parameterMap.put("keywords", StringUtils.trimToEmpty(keywords));
        parameterMap.put("categoryid", getNetSchoolCategoryId(catgory));
        return courseService.getJson(parameterMap, NetSchoolSydwUrl.SYDW_MY_LIVE_SUIT_LIST, false);
    }

    /**
     * 套餐包含的课程
     *
     * @param courseId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "{courseId}/suit", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public Object suitDetail(@PathVariable int courseId,
                             @RequestHeader(required = false) String token) throws Exception {
        userSessionService.assertSession(token);
        String username = userSessionService.getUname(token);
        final HashMap<String, Object> parameterMap = Maps.newHashMap();
        parameterMap.put("rid", courseId);
        parameterMap.put("username", username);
        return courseService.getJsonByEncryptJsonParams(parameterMap, NetSchoolSydwUrl.SYDW_MY_LIVE_SUIT_DETAIL, false);
    }

    /**
     * 获得网校categoryid
     *
     * @param catgory
     * @return
     */
    private int getNetSchoolCategoryId(int catgory) {
        return catgory == CatgoryType.GONG_WU_YUAN ? NetSchoolConfig.CATEGORY_GWY : NetSchoolConfig.CATEGORY_SHIYE;
    }
}
