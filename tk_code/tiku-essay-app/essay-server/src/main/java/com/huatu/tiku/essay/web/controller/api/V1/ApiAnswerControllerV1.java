package com.huatu.tiku.essay.web.controller.api.V1;

import com.google.common.base.Stopwatch;
import com.huatu.common.consts.TerminalType;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.vo.file.YoutuVO;
import com.huatu.tiku.essay.vo.resp.*;
import com.huatu.tiku.essay.service.UserAnswerService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.huatu.tiku.essay.constant.status.QuestionTypeConstant.PAPER;
import static com.huatu.tiku.essay.constant.status.QuestionTypeConstant.SINGLE_QUESTION;


/**
 * Created by x6 on 2017/11/23.
 * 用户答题
 */
@RestController
@Slf4j
@RequestMapping("api/v1/answer")
public class ApiAnswerControllerV1 {

    @Autowired
    UserAnswerService userAnswerService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 创建答题卡
     *
     * @return
     */
    @LogPrint
    @PostMapping(value = "answerCard", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseVO createAnswerCard(@Token UserSession userSession,
                                       @RequestHeader int terminal,
                                       @RequestHeader String cv,
                                       @RequestBody CreateAnswerCardVO createAnswerCardVO) {
        return userAnswerService.createAnswerCard(userSession.getId(), createAnswerCardVO, terminal, EssayAnswerCardEnum.ModeTypeEnum.NORMAL);

    }

    /**
     * 查询批改列表
     *
     * @param type
     * @return
     */
    @LogPrint
    @GetMapping(value = "correctDetailList/{type}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PageUtil correctList(@Token UserSession userSession,
                                @RequestHeader int terminal,
                                @RequestHeader String cv,
                                @PathVariable(value = "type", required = true) Integer type,
                                @RequestParam(name = "page", defaultValue = "1") int page,
                                @RequestParam(name = "pageSize", defaultValue = "20") int pageSize) {

        List<Sort.Order> orders = new ArrayList<Sort.Order>();
        orders.add(new Sort.Order(Sort.Direction.ASC, "bizStatus"));
        orders.add(new Sort.Order(Sort.Direction.DESC, "correctDate"));

        Pageable pageRequest = new PageRequest(page - 1, pageSize, new Sort(orders));
        List<EssayAnswerVO> l = userAnswerService.correctList(userSession.getId(), type, pageRequest,EssayAnswerCardEnum.ModeTypeEnum.NORMAL);

        long c = userAnswerService.countCorrectList(userSession.getId(), type,EssayAnswerCardEnum.ModeTypeEnum.NORMAL);

        PageUtil p = PageUtil.builder().result(l).next(c > page * pageSize ? 1 : 0).build();
        return p;
    }

    /**
     * （单题，套题）临时保存或交卷 调用该接口
     *
     * @return
     */
    @LogPrint
    @PostMapping(value = "paperAnswerCard", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object commit(@Token UserSession userSession,
                         @RequestHeader int terminal,
                         @RequestHeader String cv,
                         @RequestBody PaperCommitVO paperCommitVO) {
        if (paperCommitVO == null) {
            throw new BizException(EssayErrors.ESSAY_PARAM_ILLEGAL);
        }
        return userAnswerService.paperCommit(userSession, paperCommitVO, terminal, cv,EssayAnswerCardEnum.ModeTypeEnum.NORMAL);


    }

    /**
     * 查询批改详情
     *
     * @param type
     * @return
     */
    @LogPrint
    @GetMapping(value = "correctDetail/{type}/{answerId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<EssayQuestionVO> correctDetail(
            @Token UserSession userSession,
            @RequestHeader int terminal,
            @RequestHeader String cv,
            @PathVariable(value = "type", required = true) int type,
            @PathVariable(value = "answerId", required = true) int answerId) {
        List<EssayQuestionVO> essayQuestionVOS = userAnswerService.answerDetail(userSession.getId(), type, answerId, terminal, cv);

        //判断客户端版本,处理批改详情中的新标签（安卓6.3之后的版本支持新标签，ios7.0）
        for (EssayQuestionVO vo : essayQuestionVOS) {
            vo.setCorrectRule(null);
            if (StringUtils.isNotEmpty(vo.getCorrectedContent())) {
                if (((terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD)&& cv.compareTo("7.0") < 0)
                        || ((terminal == TerminalType.ANDROID || terminal == TerminalType.ANDROID_IPAD) && cv.compareTo("6.3") < 0)) {
                    vo.setCorrectedContent(vo.getCorrectedContent().replace("{", "").replace("}", ""));
                }
            }
        }
        return essayQuestionVOS;
    }


    /**
     * 校验用户该题的是否可再次批改
     *
     * @param userSession
     * @param terminal
     * @param cv
     * @param type
     * @param baseId
     * @return
     */
    @LogPrint
    @GetMapping(value = "correct/{type}/{baseId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseVO correctCount(@Token UserSession userSession,
                                   @RequestHeader int terminal,
                                   @RequestHeader String cv,
                                   @PathVariable(value = "type", required = true) int type,
                                   @PathVariable(value = "baseId", required = true) long baseId) {

        return userAnswerService.correctCount(userSession.getId(), type, baseId,EssayAnswerCardEnum.ModeTypeEnum.NORMAL);
    }


    /**
     * 批改是否免费
     */
    @LogPrint
    @GetMapping(value = "free", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseVO testFree() {

        return userAnswerService.free();
    }


    /**
     * 腾讯优图   图片识别接口
     */
    @PostMapping(value = "photo/{type}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public YoutuVO photo(MultipartFile file,
                         @Token UserSession userSession,
                         @RequestHeader int terminal,
                         @RequestHeader String cv,
                         @PathVariable(value = "type", required = true) int type) throws IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        YoutuVO photo = new YoutuVO();
        try {
            photo = userAnswerService.photo(file, type, userSession.getId(), terminal);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("拍照识别，用时" + String.valueOf(stopwatch.stop()));
        return photo;
    }


    /**
     * 批量处理未批改完成的答题卡
     */
    @LogPrint
    @PostMapping(value = "unfinished", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public YoutuVO unfinished() throws IOException {
        userAnswerService.unfinishedCardCommit();
        return null;
    }
}
