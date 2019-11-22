package com.huatu.tiku.match.web.controller.v1.enroll;

import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.match.service.v1.enroll.EnrollService;
import com.huatu.tiku.springboot.users.support.Token;
import com.huatu.ztk.commons.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2018-10-19 上午9:34
 **/
@RestController
@RequestMapping(value = "enroll")
@ApiVersion(value = "v1")
@Slf4j
public class EnrollControllerV1 {

    @Autowired
    private EnrollService enrollService;

    /**
     * 模考大赛报名
     * @param matchId
     * @param positionId 默认-9处理事业单位不选报名地区的问题
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "{matchId}", method = RequestMethod.POST)
    public Object enroll(@Token UserSession userSession,
                         @PathVariable int matchId,
                         @RequestParam(defaultValue = "-9") int positionId,
                         @RequestParam(defaultValue = "-1", required = false) Long schoolId,
                         @RequestParam(defaultValue = "", required = false) String schoolName) throws BizException {

        log.info("matchId={},userName={},positionId={}",matchId,userSession.getUname(), positionId);
        int userId = userSession.getId();
        String userName = userSession.getUname();
        return enrollService.enroll(matchId, userId, userName, positionId, schoolId, schoolName);
    }
}
