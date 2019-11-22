package com.huatu.tiku.course.web.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.ErrorResult;
import com.huatu.common.Result;
import com.huatu.common.consts.TerminalType;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.common.consts.CatgoryType;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.common.NetSchoolConfig;
import com.huatu.tiku.course.netschool.api.CourseServiceV1;
import com.huatu.tiku.course.netschool.api.SydwCourseServiceV1;
import com.huatu.tiku.course.netschool.api.UserCoursesServiceV1;
import com.huatu.tiku.course.service.CourseBizService;
import com.huatu.tiku.course.service.VersionService;
import com.huatu.tiku.course.util.RequestUtil;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.springboot.users.support.Token;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hanchao
 * @date 2017/8/18 16:14
 */
@RestController
@RequestMapping(value = "v1/courses")
public class CourseControllerV1 {
    public static final String IOS_NEW_VERSION = "2.4.8";
    public static final String ANDROID_NEW_VERSION = "2.4.9";

    @Autowired
    private SydwCourseServiceV1 sydwCourseService;
    @Autowired
    private CourseServiceV1 courseService;
    @Autowired
    private UserCoursesServiceV1 userCoursesService;

    @Autowired
    private VersionService versionService;

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
                       @Token UserSession userSession) throws Exception {
        HashMap<String, Object> params = Maps.newHashMap();
        params.put("orderid", orderid);
        params.put("categoryid", categoryid);
        params.put("dateid", dateid);
        params.put("priceid", priceid);
        params.put("page", page);
        params.put("username", userSession.getUname());
        params.put("shortTitle", shortTitle);

        int category = userSession.getCategory();
        if (userSession.getCategory() == CatgoryType.SHI_YE_DAN_WEI) {
            return ResponseUtil.build(sydwCourseService.sydwTotalList(params));
        }

        boolean newVersion = isNewVersion(category, terminal, cv);
        if (newVersion && StringUtils.isBlank(shortTitle)) {
            //拆分接口
            return courseBizService.getCourseListV2(userSession.getUname(),params);
        } else if (newVersion && StringUtils.isNoneBlank(shortTitle)) {
            return ResponseUtil.build(courseService.collectionDetail(params));
        } else{
            //return ResponseUtil.build(courseService.totalList(params));
            Map<String,Object> data = Maps.newHashMap();
            data.put("result", Lists.newArrayList());
            data.put("next", 0);
            return ResponseUtil.build(new NetSchoolResponse(Result.SUCCESS_CODE, "", data));
        }
    }

    /**
     * 我的直播
     * @param order 需要显示的课程，1：全部课程，2：未开始课程，3：进行中课程，4：已结束课程
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "myList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public Object mylist(@Token UserSession userSession,
                         @RequestParam int order,
                         @RequestHeader int terminal,
                         @RequestHeader String cv) throws Exception {
        String username = userSession.getUname();
        int catgory = userSession.getCategory();

        HashMap<String, Object> params = Maps.newHashMap();
        params.put("username", username);
        params.put("order", order);
        params.put("categoryid", transformToNetschool(catgory));
        return ResponseUtil.build(getMyListByDevice(params,userSession.getCategory(),terminal,cv));
    }


    /**
     * 查询我的隐藏课程列表
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "hideList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public Object hideList(@Token UserSession userSession) throws Exception {
        String username = userSession.getUname();
        int catgory = userSession.getCategory();

        final HashMap<String, Object> params = Maps.newHashMap();
        params.put("username", username);
        params.put("categoryid", transformToNetschool(catgory));
        return ResponseUtil.build(userCoursesService.myHideList(params));
    }

    /**
     * (添加隐藏课程)
     *
     * @param courseIds 课程id
     * @param orderIds  订单 id
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "hide", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.POST)
    public Object hideCourse(@Token UserSession userSession,
                             @RequestParam String courseIds,
                             @RequestParam String orderIds) throws Exception {
        String username = userSession.getUname();
        final HashMap<String, Object> params = Maps.newHashMap();
        params.put("netclassid", courseIds);
        params.put("orderId", orderIds);
        params.put("username", username);
        return ResponseUtil.build(userCoursesService.hideCourse(RequestUtil.encryptParams(params)));
    }


    /**
     * 显示隐藏课程
     *
     * @param courseIds 课程id
     * @param orderIds  订单 id
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "hide", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.DELETE)
    public Object showCourse(@Token UserSession userSession,
                             @RequestParam String courseIds,
                             @RequestParam String orderIds) throws Exception {
        String username = userSession.getUname();
        final HashMap<String, Object> params = Maps.newHashMap();
        params.put("netclassid", courseIds);
        params.put("orderId", orderIds);
        params.put("username", username);
        return ResponseUtil.build(userCoursesService.showCourse(RequestUtil.encryptParams(params)));
    }

    /**
     * 直播搜索
     *
     * @param page     分页
     * @param keywords 关键词
     * @param terminal 终端类型
     * @param cv 版本号
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "search", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public Object search(@Token UserSession userSession,
                         @RequestParam int page,
                         @RequestParam String keywords,
                         @RequestHeader int terminal,
                         @RequestParam(required = false) String shortTitle,
                         @RequestHeader String cv) throws Exception {
        String username = userSession.getUname();
        int catgory = userSession.getCategory();

        if (versionService.isIosAudit(catgory, terminal, cv)) {
            ErrorResult errorResult = ErrorResult.create(Result.SUCCESS_CODE, "数据为空",ResponseUtil.MOCK_PAGE_RESPONSE);
            throw new BizException(errorResult);
        }

        final HashMap<String, Object> params = Maps.newHashMap();
        params.put("username", username);
        params.put("page", page);
        //多个空格转为空字符串
        params.put("keywords", StringUtils.trimToEmpty(keywords));
        params.put("categoryid", transformToNetschool(catgory));

        return ResponseUtil.build(getListByDevice(params,userSession.getCategory(),terminal,cv,shortTitle));
    }


    /**
     * 我的直播列表搜索接口
     *
     * @param keywords 关键词
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "mySearch", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public Object mySearch(@Token UserSession userSession,
                           @RequestHeader String cv,
                           @RequestHeader int terminal,
                           @RequestParam String keywords) throws Exception {
        String username = userSession.getUname();
        int catgory = userSession.getCategory();

        final HashMap<String, Object> params = Maps.newHashMap();
        params.put("username", username);
        //多个空格转为空字符串
        params.put("keywords", StringUtils.trimToEmpty(keywords));
        params.put("categoryid", transformToNetschool(catgory));
        return ResponseUtil.build(getMyListByDevice(params,userSession.getCategory(),terminal,cv));
    }


    /**
     * 课程详情页接口
     *
     * @param courseId 课程id
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "{courseId}/detail", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public Object courseDetail(@PathVariable int courseId,
                               @Token UserSession userSession ) throws Exception {
        String username = userSession.getUname();

        return courseBizService.getCourseDetailV2(courseId,username);
    }

    /**
     * 我的直播课程详情页
     * 跳转到课程直播页面
     *
     * @param courseId 课程id
     * @param terminal 终端类型，用来区分调用的接口
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "{courseId}/live", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public Object myCourseDetail(@Token UserSession userSession,
                                 @PathVariable int courseId,
                                 @RequestHeader int terminal) throws Exception {
        String username = userSession.getUname();

        final HashMap<String, Object> parameterMap = Maps.newHashMap();
        parameterMap.put("username", username);
        //通过username与网校关联，网校接口的userid不起作用
        parameterMap.put("userid", -1);
        parameterMap.put("NetClassId", courseId);

        if (terminal == TerminalType.ANDROID || terminal == TerminalType.ANDROID_IPAD) {
            return ResponseUtil.build(courseService.myAndroidDetail(RequestUtil.encryptJsonParams(parameterMap)));
        } else if (terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD) {
            return ResponseUtil.build(courseService.myIosDetail(RequestUtil.encryptJsonParams(parameterMap)));
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
        return ResponseUtil.build(courseService.getHandouts(parameterMap),true);
    }

    /**
     * 套餐包含的课程
     * @param courseId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "{courseId}/suit", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public Object suitDetail(@PathVariable int courseId,
                             @Token UserSession userSession) throws Exception {
        String username = userSession.getUname();
        final HashMap<String, Object> params = Maps.newHashMap();
        params.put("rid", courseId);
        params.put("username", username);
        return ResponseUtil.build(userCoursesService.myCourseDetail(RequestUtil.encryptJsonParams(params)));
    }



















    //-------------------------------------------------------------------------------------------

    private NetSchoolResponse getCourseDetail(Map<String,Object>params, int catgory){
        return catgory == CatgoryType.GONG_WU_YUAN ?
                courseService.courseDetail(params) : sydwCourseService.courseDetail(params);
    }

    /**
     * 根据设备信息不同返回不通的我的直播
     * @param params
     * @param catgory
     * @param terminal
     * @param cv
     * @return
     */
    private NetSchoolResponse getMyListByDevice(Map<String,Object> params,int catgory, int terminal, String cv){
        if (catgory == CatgoryType.SHI_YE_DAN_WEI) {
            return userCoursesService.mySydwList(params);
        }
        if(isNewVersion(catgory,terminal, cv)){
            return userCoursesService.myListNew(params);
        } else{
            //return userCoursesService.myList(params);
            Map<String,Object> data = Maps.newHashMap();
            data.put("result", Lists.newArrayList());
            return new NetSchoolResponse(Result.SUCCESS_CODE, "", data);
        }
    }

    /**
     * 获得网校categoryid
     * @param catgory
     * @return
     */
    private int transformToNetschool(int catgory) {
        return catgory == CatgoryType.GONG_WU_YUAN ? NetSchoolConfig.CATEGORY_GWY : NetSchoolConfig.CATEGORY_SHIYE;
    }
    /**
     * 根据设备返回不同的课程列表数据
     * @param params
     * @param catgory
     * @param terminal
     * @param cv
     * @param shortTitle
     * @return
     */
    private NetSchoolResponse getListByDevice(Map<String,Object> params, int catgory, int terminal, String cv, String shortTitle) {
        if (catgory == CatgoryType.SHI_YE_DAN_WEI) {
            return sydwCourseService.sydwTotalList(params);
        }

        boolean newVersion = isNewVersion(catgory, terminal, cv);
        if (newVersion && StringUtils.isBlank(shortTitle)) {
            return courseService.allCollectionList(params);
        } else if (newVersion && StringUtils.isNoneBlank(shortTitle)) {
            return courseService.collectionDetail(params);
        } else{
            return courseService.totalList(params);
        }

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
}
