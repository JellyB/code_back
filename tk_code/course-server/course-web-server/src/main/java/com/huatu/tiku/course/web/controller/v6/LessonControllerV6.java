package com.huatu.tiku.course.web.controller.v6;

import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.v6.LessonServiceV6;
import com.huatu.tiku.course.spring.conf.aspect.mapParam.LocalMapParam;
import com.huatu.tiku.course.spring.conf.aspect.mapParam.LocalMapParamHandler;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 描述：课件接口
 *
 * @author biguodong
 * Create time 2019-01-17 下午6:22
 **/



@Slf4j
@RestController
@RequestMapping("/lesson")
@ApiVersion("v6")
public class LessonControllerV6 {


    @Autowired
    private LessonServiceV6 lessonService;

    /**
     * 图书扫码听课详情
     * @param lessionId
     * @return
     */
    @GetMapping("/{lessionId}")
    @LocalMapParam
    public Object playLesson(@PathVariable(value = "lessionId") String lessionId){
        Map<String,Object> params = LocalMapParamHandler.get();
        NetSchoolResponse netSchoolResponse = lessonService.playLesson(params);
        return ResponseUtil.build(netSchoolResponse);
    }


    /**
     * 课件收藏列表
     * @param userSession
     * @param terminal
     * @param cv
     * @return
     */
    @LocalMapParam
    @GetMapping(value = "collection")
    public Object collectionList(@Token UserSession userSession,
                                 @RequestHeader(value = "terminal") int terminal,
                                 @RequestHeader(value = "cv") String cv,
                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                 @RequestParam(value = "pageSize", defaultValue = "20") int pageSize){
        Map<String,Object> params = LocalMapParamHandler.get();
        NetSchoolResponse netSchoolResponse = lessonService.collections(params);
        return ResponseUtil.build(netSchoolResponse);
    }

    /**
     * 课件添加收藏
     * @param userSession
     * @param terminal
     * @param cv
     * @return
     */
    @LocalMapParam
    @PostMapping(value = "collection")
    public Object collectionAdd( @Token UserSession userSession,
                                 @RequestHeader(value = "terminal") int terminal,
                                 @RequestHeader(value = "cv") String cv,
                                 @RequestParam(value = "classId") int classId,
                                 @RequestParam(value = "lessonId") int lessonId,
                                 @RequestParam(value = "syllabusId") int syllabusId,
                                 @RequestParam(value = "videoType") int videoType){
        Map<String,Object> params = LocalMapParamHandler.get();
        NetSchoolResponse netSchoolResponse = lessonService.collectionAdd(params);
        return ResponseUtil.build(netSchoolResponse);
    }


    /**
     * 课件取消收藏
     * @param userSession
     * @param terminal
     * @param cv
     * @param syllabusId
     * @return
     */
    @LocalMapParam
    @DeleteMapping(value = "collection")
    public Object collectionCancel( @Token UserSession userSession,
                                 @RequestHeader(value = "terminal") int terminal,
                                 @RequestHeader(value = "cv") String cv,
                                 @RequestParam(value = "syllabusId") String syllabusId){
        Map<String,Object> params = LocalMapParamHandler.get();
        NetSchoolResponse netSchoolResponse = lessonService.collectionCancel(params);
        return ResponseUtil.build(netSchoolResponse);
    }

    /**
     * 检查课件是否收藏
     * @param userSession 用户session
     * @param terminal terminal
     * @param cv 版本号
     * @param syllabusId 大纲id
     * @return
     */
    @LocalMapParam
    @GetMapping(value = "/isCollection")
    public Object isCollected(@Token UserSession userSession,
                              @RequestHeader(value = "terminal") int terminal,
                              @RequestHeader(value = "cv") String cv,
                              @RequestParam(value = "syllabusId") int syllabusId){
        Map<String,Object> params = LocalMapParamHandler.get();
        NetSchoolResponse netSchoolResponse = lessonService.isCollection(params);
        return ResponseUtil.build(netSchoolResponse);
    }
}