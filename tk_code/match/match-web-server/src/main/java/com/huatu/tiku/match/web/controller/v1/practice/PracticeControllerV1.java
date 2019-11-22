package com.huatu.tiku.match.web.controller.v1.practice;

import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.match.service.v1.practice.PracticeService;
import com.huatu.tiku.springboot.users.support.Token;
import com.huatu.ztk.commons.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * 描述：模考大赛查看报告接口
 *
 * @author biguodong
 * Create time 2018-10-24 下午5:57
 **/

@RestController
@RequestMapping(value = "practices")
@ApiVersion(value = "v1")
@Slf4j
public class PracticeControllerV1 {

    @Autowired
    private PracticeService practiceService;


    /**
     * 查看报告
     *
     * @param paperId
     * @return
     * @throws BizException
     */
    @LogPrint
    @GetMapping(value = "/{paperId}")
    public Object get(@Token UserSession userSession,
                      @PathVariable(value = "paperId") int paperId,
                      @RequestHeader(defaultValue = "7.1.140") String cv,
                      @RequestHeader(defaultValue = "1") int terminal) throws BizException {
        int userId = userSession.getId();
        String userName = userSession.getUname();
        String token = userSession.getToken();
        return practiceService.getUserAnswerCard(paperId, userId, userName, token, cv, terminal);
    }

}
