package com.huatu.tiku.course.web.controller;

import com.huatu.common.ErrorResult;
import com.huatu.common.Result;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.course.common.AuditListType;
import com.huatu.tiku.course.service.IosBffService;
import com.huatu.tiku.course.service.VersionService;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.springboot.users.support.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * 用于ios审核
 * 课程控制层
 * @author hanchao
 * @date 2017/8/30 10:08
 */
@RestController
@RequestMapping(value = "v1/courses/ios")
public class CourseAuditControllerV1 {

    @Autowired
    private VersionService versionService;

    @Autowired
    private IosBffService bffService;

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
                       @Token UserSession userSession,
                       @RequestParam(defaultValue = AuditListType.LIVE + "") int listType) throws Exception {
        int catgory = userSession.getCategory();
        String username = userSession.getUname();

        if (versionService.isIosAudit(catgory, terminal, cv)) {
            if (page == 1) {
                return bffService.getList(username, catgory, listType);
            } else {
                ErrorResult errorResult = ErrorResult.create(0, "数据为空");
                throw new BizException(errorResult);
            }
        }
        return null;
    }


    /**
     * 我的直播
     * @param order
     * @param listType
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "myList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public Object getMyList(@Token UserSession userSession,
                            @RequestParam int order,
                            @RequestParam(defaultValue = AuditListType.LIVE + "") int listType) throws Exception {
        String username = userSession.getUname();
        int catgory = userSession.getCategory();

        return bffService.getMyList(username, order, catgory, listType);
    }
}
