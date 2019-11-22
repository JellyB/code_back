package com.huatu.ztk.course.controller;

import com.google.common.collect.Maps;
import com.huatu.ztk.commons.CatgoryType;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.TerminalType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.course.common.NetSchoolConfig;
import com.huatu.ztk.course.common.NetSchoolSydwUrl;
import com.huatu.ztk.course.common.NetSchoolUrl;
import com.huatu.ztk.course.service.CourseService;
import com.huatu.ztk.course.service.biz.CourseBizService;
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
@RequestMapping(value = "v1/courses")
public class CourseController {
    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);
    public static final String IOS_NEW_VERSION = "2.3.4";
    public static final String ANDROID_NEW_VERSION = "2.3.3";

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private CourseBizService courseBizService;

    /**
     * 全部直播列表接口
     *
     * @param orderid    排序属性
     * @param categoryid 网校考试类型
     * @param dateid     考试日期筛选
     * @param priceid    按照价格筛选
     * @param page       分页数
     * @param terminal   终端类型
     * @param cv         版本号
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "list", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public Object list(@RequestParam int orderid,
                       @RequestParam int categoryid,
                       @RequestParam int dateid,
                       @RequestParam int priceid,
                       @RequestParam int page,
                       @RequestHeader int terminal,
                       @RequestHeader String cv,
                       @RequestParam(required = false) String shortTitle,
                       @RequestHeader(required = false) String token) throws Exception {
        userSessionService.assertSession(token);
        int catgory = userSessionService.getCatgory(token);
        String username = userSessionService.getUname(token);

        final HashMap<String, Object> paramMap = Maps.newLinkedHashMap();
        paramMap.put("orderid", orderid);
        paramMap.put("categoryid", categoryid);
        paramMap.put("dateid", dateid);
        paramMap.put("priceid", priceid);
        paramMap.put("page", page);
        paramMap.put("username", username);
        paramMap.put("shortTitle", shortTitle);

        if (catgory == CatgoryType.SHI_YE_DAN_WEI) {
            return courseService.getJson(paramMap, NetSchoolSydwUrl.SYDW_TOTAL_LIST, false);
        }

        boolean newVersion = isNewVersion(catgory, terminal, cv);
        if (newVersion && StringUtils.isBlank(shortTitle)) {
            return courseBizService.getCourseList(username,paramMap);
        } else if (newVersion && StringUtils.isNoneBlank(shortTitle)) {
            //return courseService.getJson(paramMap, NetSchoolUrl.COLLECTION_DETAIL, false);
            return courseBizService.getCollectionList(username,shortTitle,page);
        }else{
            return courseService.getJson(paramMap, NetSchoolUrl.TOTAL_LIST, false);
        }
    }

    private String getListUrl(int catgory, int terminal, String cv,String shortTitle) {
        if (catgory == CatgoryType.SHI_YE_DAN_WEI) {
            return NetSchoolSydwUrl.SYDW_TOTAL_LIST;
        }

        String url = NetSchoolUrl.TOTAL_LIST;
        boolean newVersion = isNewVersion(catgory, terminal, cv);
        if (newVersion && StringUtils.isBlank(shortTitle)) {
            url = NetSchoolUrl.ALL_COLLECTION_LIST;
        } else if (newVersion && StringUtils.isNoneBlank(shortTitle)) {
            url = NetSchoolUrl.COLLECTION_DETAIL;
        }
        return url;
    }

    private boolean isNewVersion(int catgory, int terminal, String cv) {
        if (catgory == CatgoryType.GONG_WU_YUAN) {
            boolean iosNewVersion = (terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD)
                    && cv.compareTo(IOS_NEW_VERSION) >= 0;

            boolean androidNewVersion = (terminal == TerminalType.ANDROID || terminal == TerminalType.ANDROID_IPAD)
                    && cv.compareTo(ANDROID_NEW_VERSION) >= 0;

            return iosNewVersion || androidNewVersion;
        } else {
            return false;
        }
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
        return courseService.getJson(paramMap, getMyListUrl(catgory,terminal, cv), false);
    }

    private String getMyListUrl(int catgory, int terminal, String cv) {
        if (catgory == CatgoryType.SHI_YE_DAN_WEI) {
            return NetSchoolSydwUrl.SYDW_MY_LIST;
        }

        String url = isNewVersion(catgory, terminal, cv) ? NetSchoolUrl.MY_LIVE_SUIT_LIST : NetSchoolUrl.MY_LIST;

        return url;
    }

    /**
     * 查询我的隐藏课程列表
     *
     * @param token
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "hideList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public Object hideList(@RequestHeader(required = false) String token) throws Exception {
        userSessionService.assertSession(token);
        String username = userSessionService.getUname(token);
        int catgory = userSessionService.getCatgory(token);

        final HashMap<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("username", username);
        paramMap.put("categoryid", getNetSchoolCategoryId(catgory));
        return courseService.getJson(paramMap, NetSchoolUrl.HIDE_LIST, false);
    }

    /**
     * 我的直播列表隐藏课程(添加隐藏课程)
     *
     * @param token
     * @param courseIds 课程id
     * @param orderIds  订单 id
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "hide", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.POST)
    public Object hideCourse(@RequestHeader(required = false) String token,
                             @RequestParam String courseIds,
                             @RequestParam String orderIds) throws Exception {
        userSessionService.assertSession(token);
        String username = userSessionService.getUname(token);
        final HashMap<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("netclassid", courseIds);
        paramMap.put("orderId", orderIds);
        paramMap.put("username", username);
        return courseService.getJsonByEncryptParams(paramMap, NetSchoolUrl.HIDE_COURSE, false);
    }

    /**
     * 显示隐藏课程
     *
     * @param token
     * @param courseIds 课程id
     * @param orderIds  订单 id
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "hide", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.DELETE)
    public Object showCourse(@RequestHeader(required = false) String token,
                             @RequestParam String courseIds,
                             @RequestParam String orderIds) throws Exception {
        userSessionService.assertSession(token);
        String username = userSessionService.getUname(token);
        final HashMap<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("netclassid", courseIds);
        paramMap.put("orderId", orderIds);
        paramMap.put("username", username);
        return courseService.getJsonByEncryptParams(paramMap, NetSchoolUrl.SHOW_COURSE, false);
    }


    /**
     * 直播搜索
     *
     * @param token
     * @param page     分页
     * @param keywords 关键词
     * @param terminal 终端类型
     * @param cv 版本号
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "search", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public Object search(@RequestHeader(required = false) String token,
                         @RequestParam int page,
                         @RequestParam String keywords,
                         @RequestHeader int terminal,
                         @RequestParam(required = false) String shortTitle,
                         @RequestHeader String cv) throws Exception {
        userSessionService.assertSession(token);
        String username = userSessionService.getUname(token);
        int catgory = userSessionService.getCatgory(token);

        if (courseService.isIosAudit(catgory, terminal, cv)) {
            ErrorResult errorResult = ErrorResult.create(0, "数据为空");
            errorResult.setData(JsonUtil.toMap("{\"result\": [],\"next\": 0}"));
            throw new BizException(errorResult);
        }


        final HashMap<String, Object> parameterMap = Maps.newHashMap();
        parameterMap.put("username", username);
        parameterMap.put("page", page);
        //多个空格转为空字符串
        parameterMap.put("keywords", StringUtils.trimToEmpty(keywords));
        parameterMap.put("categoryid", getNetSchoolCategoryId(catgory));

        return courseService.getJson(parameterMap, getListUrl(catgory, terminal, cv, shortTitle), false);
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
                           @RequestHeader String cv,
                           @RequestHeader int terminal,
                           @RequestParam String keywords) throws Exception {
        userSessionService.assertSession(token);
        String username = userSessionService.getUname(token);
        int catgory = userSessionService.getCatgory(token);

        final HashMap<String, Object> parameterMap = Maps.newHashMap();
        parameterMap.put("username", username);
        //多个空格转为空字符串
        parameterMap.put("keywords", StringUtils.trimToEmpty(keywords));
        parameterMap.put("categoryid", getNetSchoolCategoryId(catgory));
        return courseService.getJson(parameterMap, getMyListUrl(catgory,terminal, cv), false);
    }


    /**
     * 课程详情页接口
     *
     * @param courseId 课程id
     * @param token
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "{courseId}/detail", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public Object courseDetail(@PathVariable int courseId,
                               @RequestHeader(required = false) String token) throws Exception {
        userSessionService.assertSession(token);
        String username = userSessionService.getUname(token);
        int catgory = userSessionService.getCatgory(token);

        if(catgory == CatgoryType.GONG_WU_YUAN){
            return courseBizService.getCourseDetail(courseId,username);
        }else{
            final HashMap<String, Object> parameterMap = Maps.newHashMap();
            parameterMap.put("rid", courseId);
            parameterMap.put("username", username);
            return courseService.getJsonByEncryptParams(parameterMap, NetSchoolSydwUrl.SYDW_COURSE_DATAIL, true);
        }
    }


    /**
     * 我的直播课程详情页
     * 跳转到课程直播页面
     *
     * @param token
     * @param courseId 课程id
     * @param terminal 终端类型，用来区分调用的接口
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "{courseId}/live", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public Object myCourseDetail(@RequestHeader(required = false) String token,
                                 @PathVariable int courseId,
                                 @RequestHeader int terminal) throws Exception {
        userSessionService.assertSession(token);
        String username = userSessionService.getUname(token);

        final HashMap<String, Object> parameterMap = Maps.newHashMap();
        parameterMap.put("username", username);
        //通过username与网校关联，网校接口的userid不起作用
        parameterMap.put("userid", -1);
        parameterMap.put("NetClassId", courseId);

        if (terminal == TerminalType.ANDROID || terminal == TerminalType.ANDROID_IPAD) {
            return courseService.getJsonByEncryptJsonParams(parameterMap, NetSchoolUrl.MY_COURSE_DATAIL_ANDROID, false);
        } else if (terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD) {
            return courseService.getJsonByEncryptJsonParams(parameterMap, NetSchoolUrl.MY_COURSE_DATAIL_IOS, false);
        }
        return null;
    }

    /**
     * 课程讲义
     * @param courseId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "{courseId}/handouts", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public Object handout(@PathVariable int courseId) throws Exception {
        final HashMap<String, Object> parameterMap = Maps.newHashMap();
        parameterMap.put("rid", courseId);
        return courseService.getJson(parameterMap, NetSchoolUrl.HANDOUT_LIST, true);
    }

    /**
     * 套餐包含的课程
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
        return courseService.getJsonByEncryptJsonParams(parameterMap, NetSchoolUrl.MY_LIVE_SUIT_DETAIL, false);
    }

    /**
     * 获得网校categoryid
     * @param catgory
     * @return
     */
    private int getNetSchoolCategoryId(int catgory) {
        return catgory == CatgoryType.GONG_WU_YUAN ? NetSchoolConfig.CATEGORY_GWY : NetSchoolConfig.CATEGORY_SHIYE;
    }
}
