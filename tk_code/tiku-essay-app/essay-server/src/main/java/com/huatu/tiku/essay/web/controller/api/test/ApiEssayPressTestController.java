package com.huatu.tiku.essay.web.controller.api.test;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.service.EssayMockExamService;
import com.huatu.tiku.essay.util.LogPrint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by x6 on 2018/4/3.
 *
 * 压力测试相关接口
 */
@RestController
@RequestMapping("api/v1/press/test")
@Slf4j
public class ApiEssayPressTestController {

    @Autowired
    EssayMockExamService essayMockExamService;

    /**
     * 批改完成方法测试
     */
    @LogPrint
    @PostMapping(value="finish")
    public Object finish() throws BizException {
        //finish：用户id and 得分 and 试卷id
        String finish = "and53and746";

        essayMockExamService.correctFinish(finish, EssayAnswerCardEnum.ModeTypeEnum.NORMAL);
        return "模考试卷批改结束";
    }


}
