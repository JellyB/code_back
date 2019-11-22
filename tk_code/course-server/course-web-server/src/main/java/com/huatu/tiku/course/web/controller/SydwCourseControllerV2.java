package com.huatu.tiku.course.web.controller;

import com.google.common.collect.Maps;
import com.huatu.tiku.common.consts.CatgoryType;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.common.NetSchoolConfig;
import com.huatu.tiku.course.netschool.api.SydwCourseServiceV1;
import com.huatu.tiku.course.netschool.api.UserCoursesServiceV1;
import com.huatu.tiku.course.util.RequestUtil;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 * @author hanchao
 * @date 2017/8/30 18:30
 */
@RestController
@RequestMapping(value = "v2/courses/sydw")
@Slf4j
public class SydwCourseControllerV2 {

    @Autowired
    private SydwCourseServiceV1 sydwCourseService;

    @Autowired
    private UserCoursesServiceV1 userCoursesService;

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
                       @Token UserSession userSession) throws Exception {
        String username = userSession.getUname();
        int catgory = userSession.getCategory();

        final HashMap<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("orderid", orderid);
        paramMap.put("categoryid", transformToNetschool(catgory));
        paramMap.put("dateid", dateid);
        paramMap.put("priceid", priceid);
        paramMap.put("page", page);
        paramMap.put("username", username);
        paramMap.put("shortTitle", shortTitle);

        if(StringUtils.isBlank(shortTitle)){
            //return ResponseUtil.build(sydwCourseService.allCollectionList(paramMap));
            return ResponseUtil.build(NetSchoolResponse.DEFAULT);
        }else{
            return ResponseUtil.build(sydwCourseService.sydwTotalList(paramMap));
        }
    }

    /**
     * 我的直播
     *
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

        final HashMap<String, Object> params = Maps.newHashMap();
        params.put("username", username);
        params.put("order", order);
        params.put("categoryid", transformToNetschool(catgory));
        return ResponseUtil.build(userCoursesService.mySydwListNew(params));
    }


    /**
     * 直播搜索
     *
     * @param page     分页
     * @param keywords 关键词
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "search", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public Object search(@Token UserSession userSession,
                         @RequestParam int page,
                         @RequestParam String keywords,
            @RequestHeader(required = false) String cv,
                         @RequestHeader(required = false) int terminal) throws Exception {
        String username = userSession.getUname();
        int catgory = userSession.getCategory();


        final HashMap<String, Object> parameterMap = Maps.newHashMap();
        parameterMap.put("username", username);
        parameterMap.put("page", page);
        //多个空格转为空字符串
        parameterMap.put("keywords", StringUtils.trimToEmpty(keywords));
        parameterMap.put("categoryid", transformToNetschool(catgory));
        log.warn("8$${}$${}$${}$${}$${}$${}$${}$${}",keywords,catgory,userSession.getId(),userSession.getUname(),String.valueOf(System.currentTimeMillis()),cv,terminal);
        //return ResponseUtil.build(sydwCourseService.allCollectionList(parameterMap));
        return ResponseUtil.build(NetSchoolResponse.DEFAULT);
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
                           @RequestParam String keywords) throws Exception {
        String username = userSession.getUname();
        int catgory = userSession.getCategory();

        final HashMap<String, Object> params = Maps.newHashMap();
        params.put("username", username);
        //多个空格转为空字符串
        params.put("keywords", StringUtils.trimToEmpty(keywords));
        params.put("categoryid", transformToNetschool(catgory));
        return ResponseUtil.build(userCoursesService.mySydwListNew(params));
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
                             @Token UserSession userSession) throws Exception {
        String username = userSession.getUname();
        final HashMap<String, Object> params = Maps.newHashMap();
        params.put("rid", courseId);
        params.put("username", username);
        return ResponseUtil.build(userCoursesService.mySydwCourseDetail(RequestUtil.encryptJsonParams(params)));
    }


    //---------------------------------------------------------------------------------------------------------------------

    /**
     * 获得网校categoryid
     * @param catgory
     * @return
     */
    private int transformToNetschool(int catgory) {
        return catgory == CatgoryType.GONG_WU_YUAN ? NetSchoolConfig.CATEGORY_GWY : NetSchoolConfig.CATEGORY_SHIYE;
    }
}
