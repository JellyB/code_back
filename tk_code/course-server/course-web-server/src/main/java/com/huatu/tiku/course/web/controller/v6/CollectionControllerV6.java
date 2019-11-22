package com.huatu.tiku.course.web.controller.v6;

import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.v6.CollectionServiceV6;
import com.huatu.tiku.course.spring.conf.aspect.mapParam.LocalMapParam;
import com.huatu.tiku.course.spring.conf.aspect.mapParam.LocalMapParamHandler;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 描述：我的课程收藏接口
 *
 * @author biguodong
 * Create time 2018-11-30 上午10:37
 **/

@Slf4j
@RestController
@RequestMapping("/courses/collection")
@ApiVersion("v6")
public class CollectionControllerV6 {

    @Autowired
    private CollectionServiceV6 collectionService;


    /**
     * 收藏列表
     * @param userSession
     * @return
     */
    @LocalMapParam(checkToken = true)
    @GetMapping(value = "list")
    public Object list(@Token UserSession userSession,
                       @RequestHeader(value = "terminal") int terminal,
                       @RequestParam(value = "page", defaultValue = "1", required = false) int page,
                       @RequestParam(value = "pageSize", defaultValue = "20", required = false) int pageSize){
        Map<String,Object> params = LocalMapParamHandler.get();
        NetSchoolResponse netSchoolResponse = collectionService.list(params);
        return ResponseUtil.build(netSchoolResponse);
    }


    /**
     * App课程列表
     * @param classIds
     * @param userSession
     * @return
     */
    @LocalMapParam(checkToken = true)
    @PutMapping(value = "cancel")
    public Object cancel( @Token UserSession userSession,
                          @RequestHeader(value = "terminal") int terminal,
                          @RequestParam(value = "classIds") String classIds){
        Map<String,Object> params = LocalMapParamHandler.get();
        NetSchoolResponse netSchoolResponse = collectionService.cancel(params);
        return ResponseUtil.build(netSchoolResponse);
    }


    /**
     * App课程列表
     * @param classId
     * @param userSession
     * @return
     */
    @LocalMapParam(checkToken = true)
    @PutMapping(value = "add")
    public Object add(@Token UserSession userSession,
                      @RequestHeader(value = "terminal") int terminal,
                      @RequestParam(value = "classId") String classId){
        Map<String,Object> params = LocalMapParamHandler.get();
        NetSchoolResponse netSchoolResponse = collectionService.add(params);
        return ResponseUtil.build(netSchoolResponse);
    }






}
