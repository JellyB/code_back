package com.huatu.tiku.essay.web.controller.api.V2;


import com.huatu.common.exception.BizException;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.service.v2.question.EssaySimilarQuestionServiceV2;
import com.huatu.tiku.essay.vo.resp.EssayQuestionAreaVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionTypeVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionVO;
import com.huatu.tiku.essay.service.EssaySimilarQuestionService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 单题
 */
@RestController
@RequestMapping("api/v2/single")
@Slf4j
public class ApiSingleQuestionControllerV2 {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    private EssaySimilarQuestionService essaySimilarQuestionService;

    @Autowired
    private EssaySimilarQuestionServiceV2 essaySimilarQuestionServiceV2;

    /**
     * 根据试题id查询试题详情(V6.3 多个答案)
     * @param questionBaseId
     * @param modeType 1普通答题卡2课后作业答题卡
     * @return
     */
    @LogPrint
    @GetMapping(value = "questionDetail/{questionBaseId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public EssayQuestionVO questionDetail(@Token UserSession userSession,
                                          @PathVariable(name = "questionBaseId") long questionBaseId,
                                          @RequestParam(defaultValue = "1") int modeType) throws BizException{
        log.info("questionBaseId: {}", questionBaseId);
        return essaySimilarQuestionService.findQuestionDetailV2(questionBaseId,userSession.getId(), EssayAnswerCardEnum.ModeTypeEnum.create(modeType));
    }


    /**
     * 查询试题类型(v2  大类下再分小类)
     * 缓存中获取  永久缓存
     * @modify zw
     * @return
     */
    @GetMapping(value = "questionTypeList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<EssayQuestionTypeVO> questionType() {
        return essaySimilarQuestionService.findQuestionTypeV2();
    }





    /**
     * 根据相似题目id查询地区列表
     * @param similarId
     * @return
     */
    @LogPrint
    @GetMapping(value = "areaList/{similarId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<EssayQuestionAreaVO> paperList(@Token UserSession userSession,
                                               @PathVariable(name = "similarId") long similarId) {

        log.info("questionDetailId: {}", similarId);
        return essaySimilarQuestionServiceV2.findAreaList(similarId, userSession.getId(),EssayAnswerCardEnum.ModeTypeEnum.NORMAL);
    }
}
