package com.huatu.ztk.course.controller;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.course.common.AuditListType;
import com.huatu.ztk.course.service.CourseAuditService;
import com.huatu.ztk.course.service.CourseService;
import com.huatu.ztk.user.service.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * 用于ios审核
 * 课程控制层
 */
@RestController
@RequestMapping(value = "v1/courses/ios")
public class CourseAuditController {
    private static final Logger logger = LoggerFactory.getLogger(CourseAuditController.class);

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private CourseAuditService courseAuditService;


    /**
     * 全部直播列表
     *
     * @param listType
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
                       @RequestHeader(required = false) String token,
                       @RequestParam(defaultValue = AuditListType.LIVE + "") int listType) throws Exception {
        userSessionService.assertSession(token);
        int catgory = userSessionService.getCatgory(token);
        String username = userSessionService.getUname(token);

        if (courseService.isIosAudit(catgory, terminal, cv)) {
            if (page == 1) {
                return courseAuditService.getList(username, catgory, listType);
            } else {
                ErrorResult errorResult = ErrorResult.create(0, "数据为空");
                errorResult.setData(JsonUtil.toMap("{\"result\": [],\"next\": 0}"));
                throw new BizException(errorResult);
            }
        }
        return null;
    }


    /**
     * 我的直播
     * @param token
     * @param order
     * @param listType
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "myList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public Object getMyList(@RequestHeader(required = false) String token,
                         @RequestParam int order,
                         @RequestParam(defaultValue = AuditListType.LIVE + "") int listType) throws Exception {
        userSessionService.assertSession(token);
        String username = userSessionService.getUname(token);
        int catgory = userSessionService.getCatgory(token);

        return courseAuditService.getMyList(username, order, catgory, listType);
    }
}
