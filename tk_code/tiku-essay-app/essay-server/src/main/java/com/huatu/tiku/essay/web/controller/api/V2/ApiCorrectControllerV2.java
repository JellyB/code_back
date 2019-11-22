package com.huatu.tiku.essay.web.controller.api.V2;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.essayEnum.CorrectModeEnum;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.service.correct.CorrectServiceV2;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.springboot.users.support.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 描述：
 *
 * @author biguodong Create time 2019-07-11 7:54 PM
 **/
@RestController
@RequestMapping(value = "/api/v2/correct")
public class ApiCorrectControllerV2 {

    @Autowired
    private CorrectServiceV2 correctServiceV2;

    /**
     * @param answerId
     * @param delayStatus
     * @param type        answerCardTypeEnum
     * @return
     * @throws BizException
     */
    @LogPrint
    @PostMapping(value = "convert")
    public Object convert(@RequestParam(value = "answerId") Long answerId,
                          @RequestParam(value = "delayStatus") Integer delayStatus,
                          @RequestParam(value = "type") Integer type,
                          @Token UserSession userSession)
            throws BizException {

        return correctServiceV2.convert(answerId, type, delayStatus,userSession, EssayAnswerCardEnum.ModeTypeEnum.NORMAL);
    }
}
