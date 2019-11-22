package com.huatu.tiku.course.web.controller.v3;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import com.huatu.common.consts.TerminalType;
import com.huatu.common.exception.BizException;
import com.huatu.common.spring.event.EventPublisher;
import com.huatu.common.utils.collection.HashMapBuilder;
import com.huatu.tiku.common.bean.AreaConstants;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.course.bean.CourseListV3DTO;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.fall.CourseServiceV3Fallback;
import com.huatu.tiku.course.netschool.api.v3.CourseServiceV3;
import com.huatu.tiku.course.netschool.api.v3.UserCoursesServiceV3;
import com.huatu.tiku.course.service.CourseBizService;
import com.huatu.tiku.course.service.CourseCollectionBizService;
import com.huatu.tiku.course.service.VersionService;
import com.huatu.tiku.course.util.CourseCacheKey;
import com.huatu.tiku.course.util.RequestUtil;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.course.web.controller.util.CourseUtil;
import com.huatu.tiku.springboot.basic.subject.SubjectEnum;
import com.huatu.tiku.springboot.basic.subject.SubjectService;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static com.huatu.tiku.course.util.ResponseUtil.MOCK_PAGE_RESPONSE;

/**
 * @author hanchao
 * @date 2017/9/13 15:41
 */
@Slf4j
@RestController
@RequestMapping(value = "v3/courses")
public class CourseControllerV3 {

    @Autowired
    private CourseServiceV3 courseServiceV3;
    @Autowired
    private CourseBizService courseBizService;
    @Autowired
    private UserCoursesServiceV3 userCoursesServiceV3;
    @Autowired
    private CourseServiceV3Fallback courseServiceV3Fallback;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private CourseCollectionBizService courseCollectionBizService;
    @Autowired
    private EventPublisher eventPublisher;
    @Autowired
    private VersionService versionService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CourseUtil courseUtil;

    /**
     * 课程合集详情
     *
     * @param shorttitle
     * @return
     */
    @GetMapping("/collection")
    public Object getCollectionDetail(
            @RequestHeader(value = "terminal") Integer terminal,
            @RequestHeader(value = "cv") String cv, @RequestParam String shorttitle,
            @RequestParam int page,
            @Token UserSession userSession) {
        //行为日志收集   格式说明 在云盘上 http://123.103.79.72:8025/index.php?explorer
        log.warn("3$${}$${}$${}$${}$${}$${}", shorttitle, userSession.getId(), userSession.getUname(), String.valueOf(System.currentTimeMillis()), cv, terminal);
        return courseBizService.getCollectionList(shorttitle, page);
    }

    /**
     * 图书列表
     *
     * @param categoryid
     * @param orderid
     * @param page
     * @param userSession
     * @return
     */
    @GetMapping("/books")
    public Object bookList(
            @RequestHeader(value = "terminal") Integer terminal,
            @RequestHeader(value = "cv") String cv,
            @RequestParam(required = false, defaultValue = "1001") int categoryid,
            @RequestParam(required = false, defaultValue = "1") int orderid,
            @RequestParam int page,
            @RequestParam(required = false, defaultValue = "") String keywords,
            @RequestParam(required = false, defaultValue = "1000") int subjectid,
            @Token UserSession userSession) {
        //TODO 此处用以判断是否为IOS内测版本，正式上线后可以删除

        //<editor-fold desc="此处用以判断是否为IOS内测版本，正式上线后可以删除">
        Boolean member = false;
        if (terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD) {
            RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
            try {
                member = connection.sIsMember(CourseCacheKey.IOS_AUDIT_VERSION.getBytes(), cv.getBytes());
            } finally {
                connection.close();
            }
        }
        if (!member) {
            return MOCK_PAGE_RESPONSE;
        }
        //</editor-fold>
        int provinceId = AreaConstants.getNetSchoolProvinceId(userSession.getArea());
        Map<String, Object> params = HashMapBuilder.<String, Object>newBuilder()
                .put("categoryid", categoryid)
                .put("username", userSession.getUname())
                .put("orderid", orderid)
                .put("page", page)
                .put("subjectid", subjectid)
                .put("keywords", keywords)
                .put("provinceid", provinceId).build();
        NetSchoolResponse bookList = courseServiceV3.findBookList(params);
        return ResponseUtil.build(bookList);
    }

    /**
     * 录播课程列表
     *
     * @param categoryid
     * @param orderid
     * @param page
     * @param userSession
     * @return
     */
    @GetMapping("/recordings")
    public Object recordingList(
            @RequestHeader("cv") String cv,
            @RequestHeader("terminal") int terminal, @RequestParam(required = false, defaultValue = "1001") int categoryid,
            @RequestParam(required = false, defaultValue = "1") int orderid,
            @RequestParam int page,
            @RequestParam(required = false, defaultValue = "") String keywords,
            @RequestParam(required = false, defaultValue = "1000") int subjectid,
            @Token UserSession userSession) {
        int provinceId = AreaConstants.getNetSchoolProvinceId(userSession.getArea());
        Map<String, Object> params = HashMapBuilder.<String, Object>newBuilder()
                .put("categoryid", categoryid)
                .put("username", userSession.getUname())
                .put("orderid", orderid)
                .put("page", page)
                .put("subjectid", subjectid)
                .put("keywords", keywords)
                .put("provinceid", provinceId).build();
        NetSchoolResponse recordingList = courseServiceV3.findRecordingList(params);
        courseServiceV3Fallback.setRecordingList(params, recordingList);
        //行为日志收集   格式说明 在云盘上 http://123.103.79.72:8025/index.php?explorer
        log.warn("2$${}$${}$${}$${}$${}$${}$${}$${}", categoryid, subjectid, userSession.getId(), userSession.getUname(), keywords, String.valueOf(System.currentTimeMillis()), cv, terminal);
        return ResponseUtil.build(recordingList);
    }

    /**
     * 获取直播课程列表
     *
     * @param orderid
     * @param page
     * @param priceid
     * @param userSession
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @GetMapping("/lives")
    public Object liveList(
            @RequestHeader("cv") String cv,
            @RequestHeader("terminal") int terminal,
            @RequestParam(required = false, defaultValue = "0") int orderid,
            @RequestParam int page,
            @RequestParam(required = false, defaultValue = "1000") int priceid,
            @RequestParam(required = false, defaultValue = "") String keywords,
            @RequestParam(required = false, defaultValue = "") String category,//老版本的从session中映射，新版本的需要客户端自己传过来，直接做适配即可
            @Token UserSession userSession) throws InterruptedException, ExecutionException, BizException {
        int top = subjectService.top(userSession.getSubject());
        int categoryid = 1000;
        if (StringUtils.isBlank(category) || !StringUtils.isNumeric(category)) {
            //老版本未传递category
            SubjectEnum[] enums = SubjectEnum.values();
            for (SubjectEnum subjectEnum : enums) {
                if (subjectEnum.code() == top) {
                    categoryid = subjectEnum.categoryid();
                    break;
                }
            }
        } else {
            //新版本直接解析客户端传递过来的值
            categoryid = Optional.ofNullable(Ints.tryParse(category)).orElse(1000);
        }
        Map<String, Object> params = HashMapBuilder.<String, Object>newBuilder()
                .put("orderid", orderid)
                .put("page", page)
                .put("keywords", keywords)
                .put("categoryid", categoryid)
                .put("priceid", priceid)
                .put("cv", cv)
                .put("terminal", terminal)
                .build();
        //CourseListV3DTO courseListV3 = courseBizService.getCourseListV3(params);
        CourseListV3DTO courseListV3 = new CourseListV3DTO();
        courseListV3.setCache(true);
        courseListV3.setDegrade(true);
        courseListV3.setNext(0);
        courseListV3.setResult(Lists.newArrayList());
        courseListV3.setCacheTimestamp(System.currentTimeMillis());
        log.warn("1$${}$${}$${}$${}$${}$${}$${}$${}", categoryid, -1, userSession.getId(), userSession.getUname(), "", String.valueOf(System.currentTimeMillis()), cv, terminal);
        return courseListV3;
    }


    /**
     * 课程详情页
     *
     * @param userSession
     * @param rid
     * @return
     */
    @GetMapping(value = "/{rid}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object getCourseDetail(@Token UserSession userSession,
                                  @RequestHeader("cv") String cv,
                                  @RequestHeader("terminal") int terminal,
                                  @PathVariable int rid) throws BizException, ExecutionException, InterruptedException {
        log.warn("4$${}$${}$${}$${}$${}$${}", rid, userSession.getId(), userSession.getUname(), String.valueOf(System.currentTimeMillis()), cv, terminal);
        return courseBizService.getCourseDetailV3(rid, userSession.getUname());
    }

    /**
     * 课程详情页
     *
     * @param rid
     * @return
     */
    @GetMapping(value = "/{rid}", produces = MediaType.TEXT_HTML_VALUE + ";charset=utf-8")
    public Object getCourseHtml(@PathVariable int rid) throws BizException, ExecutionException, InterruptedException {
        return courseBizService.getCourseHtml(rid);
    }


    /**
     * 课程播放接口
     *
     * @param userSession
     * @param rid
     * @return
     */
    @GetMapping("/{rid}/secrinfo")
    public Object getCourseSecrInfo(@Token UserSession userSession,
                                    @PathVariable int rid,
                                    @RequestParam(required = false, defaultValue = "0") int isTrial,
                                    @RequestParam(required = false, defaultValue = "0") int fatherId,
                                    @RequestHeader("terminal") int terminal,
                                    @RequestHeader(value = "cv", defaultValue = "0") String cv
    ) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("rid", rid);
        params.put("username", userSession.getUname());
        params.put("fatherId", fatherId);
        params.put("isTrial", isTrial);

        NetSchoolResponse netSchoolResponse = courseServiceV3.getCourseSecrInfo(params);
        Object response = ResponseUtil.build(netSchoolResponse, true);
        //发布课程播放事件
        courseUtil.pushPlayEvent(userSession, netSchoolResponse, response);
        //添加课程进度
        courseUtil.addStudyProcessIntoSecrInfo(response, userSession.getToken(), cv, terminal);
        return response;
    }

    /**
     * 课程播放接口
     *
     * @param userSession
     * @param rid
     * @return
     */
    @GetMapping("/{rid}/teachers")
    public Object findCourseTeachers(@Token UserSession userSession,
                                     @PathVariable int rid) {
        return ResponseUtil.build(courseServiceV3.findTeachersByCourse(rid));
    }

    /**
     * 课程大纲
     *
     * @param userSession
     * @param rid
     * @return
     */
    @GetMapping("/{rid}/timetable")
    public Object findCourseTimetable(@Token UserSession userSession,
                                      @PathVariable int rid) {
        return ResponseUtil.build(courseServiceV3.findTimetable(rid));
    }


    /**
     * 课程讲义
     *
     * @param rid
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "{rid}/handouts", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public Object handout(@PathVariable int rid,
                          @Token UserSession userSession) throws Exception {
        final HashMap<String, Object> parameterMap = Maps.newHashMap();
        parameterMap.put("rid", rid);
        parameterMap.put("username", userSession.getUname());
        return ResponseUtil.build(courseServiceV3.getHandouts(parameterMap), true);
    }


    /**
     * 用户隐藏课程操作
     *
     * @param userSession
     * @param courseIds
     * @param orderIds
     * @return
     */
    @PostMapping("/hide")
    public Object hideUserCourse(@Token UserSession userSession,
                                 @RequestParam String courseIds,
                                 @RequestParam String orderIds) {
        String username = userSession.getUname();
        final HashMap<String, Object> params = HashMapBuilder.newBuilder()
                .put("netclassid", courseIds)
                .put("orderId", orderIds)
                .put("username", username)
                .buildUnsafe();
        log.warn("9$${}$${}$${}$${}$${}", courseIds, userSession.getId(), username, String.valueOf(System.currentTimeMillis()), orderIds);
        return ResponseUtil.build(userCoursesServiceV3.hideCourse(RequestUtil.encryptParams(params)));
    }


    /**
     * 取消隐藏课程
     *
     * @param userSession
     * @param courseIds
     * @param orderIds
     * @return
     * @throws Exception
     */
    @DeleteMapping("/hide")
    public Object showCourse(@Token UserSession userSession,
                             @RequestParam String courseIds,
                             @RequestParam String orderIds) throws Exception {
        String username = userSession.getUname();
        final HashMap<String, Object> params = Maps.newHashMap();
        params.put("username", username);
        params.put("netclassid", courseIds);
        params.put("orderId", orderIds);
        return ResponseUtil.build(userCoursesServiceV3.showCourse(RequestUtil.encryptParams(params)));
    }


    /**
     * 我的套餐课-包含的课程
     *
     * @param courseId
     * @return
     * @throws Exception
     */
    @GetMapping(value = "{courseId}/suit")
    public Object suitDetail(@PathVariable int courseId,
                             @Token UserSession userSession) throws Exception {
        String username = userSession.getUname();
        final HashMap<String, Object> params = Maps.newHashMap();
        params.put("username", username);
        params.put("rid", courseId);
        log.warn("10$${}$${}$${}$${}$${}", courseId, userSession.getId(), username, String.valueOf(System.currentTimeMillis()));
        return ResponseUtil.build(userCoursesServiceV3.getMyPackCourseDetail(RequestUtil.encryptJsonParams(params)));
    }

}
