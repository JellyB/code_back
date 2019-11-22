package com.huatu.tiku.course.web.controller.v6;

import com.google.common.collect.Lists;
import com.huatu.common.ErrorResult;
import com.huatu.common.SuccessMessage;
import com.huatu.common.exception.BizException;
import com.huatu.common.utils.reflect.ClassUtils;
import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.bean.vo.One2OneFormDTOV2;
import com.huatu.tiku.course.bean.vo.RecordProcess;
import com.huatu.tiku.course.netschool.api.v6.UserCourseServiceV6;
import com.huatu.tiku.course.service.manager.CourseExercisesProcessLogManager;
import com.huatu.tiku.course.service.v6.CourseBizV6Service;
import com.huatu.tiku.course.service.v6.CourseServiceV6Biz;
import com.huatu.tiku.course.spring.conf.aspect.mapParam.LocalMapParam;
import com.huatu.tiku.course.spring.conf.aspect.mapParam.LocalMapParamHandler;
import com.huatu.tiku.course.util.RequestUtil;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.course.web.controller.util.CourseUtil;
import com.huatu.tiku.springboot.users.support.Token;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * 描述：我的课程接口
 *
 * @author biguodong
 * Create time 2018-11-26 下午5:28
 **/

@Slf4j
@RestController
@RequestMapping("/my")
@ApiVersion("v6")
public class UserCourseControllerV6 {


    @Autowired
    private UserCourseServiceV6 userCourseService;

    @Autowired
    private CourseBizV6Service courseBizV6Service;

    @Autowired
    private CourseServiceV6Biz courseServiceV6Biz;

    @Autowired
    private CourseExercisesProcessLogManager courseExercisesProcessLogManager;

    @Autowired
    private CourseUtil courseUtil;


    /**
     * 获取我的学习日历接口
     * @param userSession
     * @param type
     * @param cv
     * @param terminal
     * @param date
     * @return
     */
    @LocalMapParam(checkToken = true)
    @GetMapping(value = "learnCalendar")
    public Object obtainLearnCalendar(@Token UserSession userSession,
                                      @RequestHeader(value = "cv") String cv,
                                      @RequestHeader(value = "terminal") int terminal,
                                      @RequestParam(value = "date") String date,
                                      @RequestParam(value = "type") String type){
        Map<String,Object> params = LocalMapParamHandler.get();
        return courseBizV6Service.obtainLearnCalender(params);
    }

    /**
     * 我的学习界面 - 课后作业&阶段考试未完成数量
     * @param userSession
     * @param cv
     * @param terminal
     * @return
     */
    @LocalMapParam(checkToken = true)
    @GetMapping(value = "unFinishNum")
    public Object obtainUnFinishedNum(@Token UserSession userSession,
                                      @RequestHeader(value = "cv") String cv,
                                      @RequestHeader(value = "terminal") int terminal){

        return courseExercisesProcessLogManager.getCountByType(userSession.getId(),userSession.getUname());
    }

    /**
     * 全部已读
     * @param userSession
     * @param type
     * @return
     */
    @PutMapping(value = "allRead/{type}")
    public Object allReadCourseWork(@Token UserSession userSession,
                                    @PathVariable(value = "type")String type){

        return courseExercisesProcessLogManager.allReadByType(userSession.getId(), type, userSession.getUname());
    }

    /**
     * 课后作业 单条已读
     * @param userSession
     * @param courseType
     * @return
     */
    @PutMapping(value = "oneRead/courseWork/{courseType}/{courseWareId}")
    public Object readOneCourseWork(@Token UserSession userSession,
                                    @PathVariable(value = "courseType") int courseType,
                                    @PathVariable(value = "courseWareId")long courseWareId){
        return courseExercisesProcessLogManager.readyOneCourseWork(userSession.getId(), courseWareId, courseType);
    }

    /**
     * 阶段测试 单条已读
     * @param userSession
     * @param courseId
     * @param syllabusId
     * @return
     */
    @PutMapping(value = "oneRead/periodTest/{syllabusId}/{courseId}")
    public Object readOneCourseWork(@Token UserSession userSession,
                                    @PathVariable(value = "courseId")Long courseId,
                                    @PathVariable(value = "syllabusId")Long syllabusId){
         courseExercisesProcessLogManager.readyOnePeriod(syllabusId, courseId,userSession.getUname());
         return null;
    }

    /**
     * 课后作业&阶段考试列表
     * @param userSession
     * @return
     */
    @GetMapping(value = "courseWork/detailList")
    public Object studyList(@Token UserSession userSession,
                            @RequestParam(value = "page", defaultValue = "1")int page,
                            @RequestParam(value = "size", defaultValue = "20") int size){

        return courseExercisesProcessLogManager.courseWorkList(userSession.getId(), page, size);
    }

    /**
     * 数据纠正
     * @param userSession
     * @param secret
     * @return
     */
    @PostMapping(value = "dataCorrect")
    public Object dataCorrect(@Token UserSession userSession, @RequestHeader("secret") String secret){
        courseExercisesProcessLogManager.dataCorrect(userSession.getId(), secret);
        return SuccessMessage.create("操作成功！");
    }

    /**
     * 数据纠正
     * @param userSession
     * @param secret
     * @return
     */
    @PostMapping(value = "dataFix")
    public Object dataFix(@Token UserSession userSession, @RequestHeader("secret") String secret){
        courseExercisesProcessLogManager.dataFix(userSession.getId());
        return SuccessMessage.create("操作成功！");
    }

    @PostMapping(value = "dataCorrect/switch")
    public Object dataCorrectSwitch(@Token UserSession userSession, @RequestHeader("switch") String str){
        courseExercisesProcessLogManager.dataCorrectSwitch(userSession.getId(), str);
        return SuccessMessage.create("操作成功！");
    }



    /**
     * 阶段测试列表
     * @param userSession
     * @param page
     * @param size
     * @return
     */
    @LocalMapParam(checkToken = true)
    @GetMapping(value = "periodTest/detailList")
    public Object periodTestList(@Token UserSession userSession,
                            @RequestParam(value = "page", defaultValue = "1")int page,
                            @RequestParam(value = "pageSize", defaultValue = "20") int size){
    	 Map<String,Object> params = LocalMapParamHandler.get();
    	 params.put("userId", userSession.getId());
        return courseServiceV6Biz.periodTestList(params);
    }

    /**
     * 我的课后作业报告
     * @param userSession
     * @param cv
     * @param terminal
     * @param cardId
     * @return
     */
    @GetMapping(value = "/courseWork/{id}")
    @LocalMapParam(checkToken = true)
    public Object testReport(@Token UserSession userSession,
                             @RequestHeader(value = "cv", defaultValue = "1.0") String cv,
                             @RequestHeader(value = "terminal") int terminal,
                             @PathVariable(value = "id") long cardId){
        return courseServiceV6Biz.courseWorkReport(userSession, terminal, cardId);
    }


    /**
     * 听课记录 & 课后作业 学习报告
     * @param userSession
     * @param terminal
     * @param cv
     * @param bjyRoomId
     * @param classId
     * @param netClassId
     * @param courseWareId
     * @param videoType
     * @return
     */
    @GetMapping(value = "/learnReport")
    @LocalMapParam(checkToken = true)
    public Object learnReport(@Token UserSession userSession,
                              @RequestHeader(value = "terminal") int terminal,
                              @RequestHeader(value = "cv", defaultValue = "1.0") String cv,
                              @RequestParam(value = "bjyRoomId", defaultValue = "") String bjyRoomId,
                              @RequestParam(value = "classId") long classId,
                              @RequestParam(value = "netClassId") long netClassId,
                              @RequestParam(value = "lessonId") long courseWareId,
                              @RequestParam(value = "videoType") int videoType,
                              @RequestParam(value = "exerciseCardId") long exerciseCardId,
                              @RequestParam(value = "reportStatus",defaultValue = "1") int reportStatus,
                              @RequestParam(value = "syllabusId") long syllabusId){

        return courseServiceV6Biz.learnReport(userSession, bjyRoomId, classId, netClassId, courseWareId, videoType, exerciseCardId, syllabusId, terminal, cv);

    }

    /**
     * 学习报告刷新
     * @param userSession
     * @param terminal
     * @param cv
     * @param bjyRoomId
     * @param courseWareId
     * @param videoType
     * @param liveStatus
     * @param studyReport
     * @return
     */
    @GetMapping(value = "/learnReport/{videoType}/{coursewareId}")
    public Object learnReportFresh (@Token UserSession userSession,
                              @RequestHeader(value = "terminal") int terminal,
                              @RequestHeader(value = "cv", defaultValue = "1.0") String cv,
                              @PathVariable(value = "videoType") int videoType,
                              @PathVariable(value = "coursewareId") long courseWareId,
                              @RequestParam(value = "bjyRoomId", defaultValue = "") String bjyRoomId,
                              @RequestParam(value = "liveStatus", defaultValue = "0") int liveStatus,
                              @RequestParam(value = "studyReport", defaultValue = "0") int studyReport){

        return courseUtil.dealLearnReportBranchInfo(videoType, courseWareId, bjyRoomId, userSession.getId(), liveStatus, studyReport);
    }




    /**
     * 获取已过期课程
     * @param userSession
     * @param page
     * @param pageSize
     * @return
     */
    @LocalMapParam(checkToken = true)
    @GetMapping(value = "expiredCourses")
    public Object obtainExpiredCourses(@Token UserSession userSession,
                                       @RequestHeader(value = "terminal") int terminal,
                                       @RequestParam(value = "page", defaultValue = "1") int page,
                                       @RequestParam(value = "pageSize", defaultValue = "20") int pageSize){

        Map<String,Object> params = LocalMapParamHandler.get();
        NetSchoolResponse netSchoolResponse = userCourseService.obtainExpiredCourses(params);
        return ResponseUtil.build(netSchoolResponse);
    }


    /**
     * 我的课程筛选列表
     * @param userSession
     * @return
     */
    @LocalMapParam(checkToken = true)
    @GetMapping(value = "filteredCourses")
    public Object obtainFilteredCourses(@Token UserSession userSession){
        Map<String,Object> params = LocalMapParamHandler.get();
        NetSchoolResponse netSchoolResponse = userCourseService.obtainCourseFilterList(params);
        return ResponseUtil.build(netSchoolResponse);
    }

    /**
     * 我的课程接口
     * @param userSession
     * @param examStatus
     * @param keyWord
     * @param priceStatus
     * @param recentlyStudy
     * @param studyStatus
     * @param teacherId
     * @param page
     * @param pageSize
     * @return
     */
    @LocalMapParam(checkToken = true)
    @GetMapping(value = "courses")
    public Object obtainMineCourses(
                                    @Token UserSession userSession,
                                    @RequestParam(value = "examStatus", required = false) String examStatus,
                                    @RequestParam(value = "isDelete", required = false, defaultValue = "0") int isDelete,
                                    @RequestParam(value = "keyWord", required = false) String keyWord,
                                    @RequestParam(value = "priceStatus", required = false) String priceStatus,
                                    @RequestParam(value = "recentlyStudy", defaultValue = "", required = false) String recentlyStudy,
                                    @RequestParam(value = "studyStatus", required = false) String studyStatus,
                                    @RequestParam(value = "teacherId", required = false) String teacherId,
                                    @RequestParam(value = "page", defaultValue = "1", required = false) int page,
                                    @RequestParam(value = "pageSize", defaultValue = "20", required = false) int pageSize){
        Map<String,Object> params = LocalMapParamHandler.get();
        Object object =  courseBizV6Service.obtainMineCourses(params);
        if(null == object){
            ErrorResult errorResult = ErrorResult.create(1000010, "当前请求的人数过多，请在5分钟后重试", Lists.newArrayList());
            throw new BizException(errorResult);
        }else{
            return object;
        }
    }


    /**
     * 一键清除我的已过期课程
     * @param cv
     * @param userSession
     * @return
     */
    @LocalMapParam
    @GetMapping(value = "clearExpired")
    public Object clearExpiredCourses(@RequestHeader(value = "cv") String cv,
                                      @Token UserSession userSession){
        Map<String,Object> params = LocalMapParamHandler.get();
        NetSchoolResponse netSchoolResponse = userCourseService.clearExpiredCourses(params);
        return ResponseUtil.build(netSchoolResponse);
    }


    /**
     * 课程所属考试接口
     * @param userSession
     * @return
     */
    @LocalMapParam
    @GetMapping(value = "cateList")
    public Object cateList(@Token(check = false, defaultValue = "") UserSession userSession,
                           @RequestHeader(value = "cv") String cv,
                           @RequestHeader(value = "terminal") int terminal){
        Map<String,Object> params = LocalMapParamHandler.get();
        NetSchoolResponse netSchoolResponse = userCourseService.cateList(params);
        return ResponseUtil.build(netSchoolResponse);
    }


    /**
     * 列表设置考试类型
     * @param userSession
     * @return
     */
    @LocalMapParam
    @PutMapping(value = "category")
    public Object setCategory(@Token UserSession userSession,
                              @RequestParam(value = "setList") String setList){
        Map<String,Object> params = LocalMapParamHandler.get();
        NetSchoolResponse netSchoolResponse = userCourseService.setCategory(params);
        return ResponseUtil.build(netSchoolResponse);
    }

    /**
     * 直播学习记录上报,只上报给 php
     * @param userSession
     * @param syllabusId
     * @return
     */
    @LocalMapParam
    @PostMapping(value = "liveRecord")
    public Object saveLiveRecord(@Token UserSession userSession,
                                 @RequestHeader(value = "terminal") int terminal,
                                 @RequestHeader(value = "cv") String cv,
                                 @RequestParam(defaultValue = "0") int syllabusId){

        Map<String,Object> params = LocalMapParamHandler.get();
        NetSchoolResponse netSchoolResponse = userCourseService.saveLiveRecord(params);
        return ResponseUtil.build(netSchoolResponse);

    }

    /**
     * 一对一信息提交
     * @param userSession           用户 token
     * @param cv
     * @param terminal
     * @return
     */
    @PostMapping(value = "/1v1/{courseId}")
    public Object save1V1Table(@Token UserSession userSession,
                          @RequestHeader(value = "terminal") int terminal,
                          @RequestHeader(value = "cv") String cv,
                          @RequestBody @Valid One2OneFormDTOV2 one2OneFormDTOV2,
                          @PathVariable int courseId){

        Map<String,Object> params = ClassUtils.getBeanProperties(one2OneFormDTOV2);
        params.put("userName",userSession.getUname());
        params.put("rid",courseId);
        params.put("terminal", terminal);
        log.info("one2One post params:{},terminal:{},cv:{}", params,terminal,cv);
        String p = RequestUtil.encrypt(params);
        NetSchoolResponse netSchoolResponse = userCourseService.one2One(p);
        return ResponseUtil.build(netSchoolResponse);
    }


    /**
     * 一对一信息获取
     * @param OrderNum
     * @param @PathVariable int courseId
     * @return
     * @throws BizException
     */
    @LocalMapParam
    @GetMapping(value = "/1v1/{courseId}")
    public Object get1V1Table(@RequestParam(value = "OrderNum") String OrderNum,
                              @RequestHeader(value = "terminal") int terminal,
                              @PathVariable int courseId) throws BizException{

        Map<String,Object> params = LocalMapParamHandler.get();
        params.put("rid",courseId);
        NetSchoolResponse netSchoolResponse = userCourseService.obtainOne2One(RequestUtil.encrypt(params), terminal);
        return ResponseUtil.build(netSchoolResponse);
    }

    @PostMapping("playBackV2")
    public Object playBack(@Token UserSession userSession,
                           @RequestBody RecordProcess recordProcess){
        recordProcess.setUserId(userSession.getId());
        recordProcess.setSubject(1);
        recordProcess.setUserName(userSession.getUname());
        courseExercisesProcessLogManager.dealRecordProcess(recordProcess);
        return "success";
    }
}
