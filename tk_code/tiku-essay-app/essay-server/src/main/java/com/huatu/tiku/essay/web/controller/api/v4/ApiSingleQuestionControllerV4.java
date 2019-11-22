package com.huatu.tiku.essay.web.controller.api.v4;

import java.util.List;

import com.huatu.common.ErrorResult;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.entity.EssaySimilarQuestionGroupInfo;
import com.huatu.tiku.essay.service.EssaySimilarQuestionService;
import com.huatu.tiku.essay.service.v2.question.SingleQuestionSearchV2;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.resp.EssayQuestionVO;
import com.huatu.tiku.springboot.users.support.Token;

import lombok.extern.slf4j.Slf4j;

/**
 * 单题管理v4 人工批改
 */
@RestController
@RequestMapping("api/v4/single")
@Slf4j
public class ApiSingleQuestionControllerV4 {

    @Autowired
    private EssaySimilarQuestionService essaySimilarQuestionService;

    @Autowired
    private SingleQuestionSearchV2 singleQuestionSearch;

    /**
     * 单题列表接口（分页） V3修改：1.支持游客模式 2.将地区列表相关信息在该接口返回
     *
     * @param type
     * @param page
     * @param pageSize
     * @return
     * @RequestHeader String cv,
     * @RequestHeader int terminal,
     */
    @LogPrint
    @GetMapping(value = "questionList/{type}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PageUtil singleQuestionList(@Token(check = false) UserSession userSession, @RequestHeader int terminal,
                                       @RequestHeader String cv, @PathVariable(name = "type") int type,
                                       @RequestParam(name = "page", defaultValue = "1") int page,
                                       @RequestParam(name = "pageSize", defaultValue = "20") int pageSize,
                                       @RequestParam(defaultValue = "1") int modeType) {

        Pageable pageRequest = new PageRequest(page - 1, pageSize, Sort.Direction.DESC, "showMsg", "gmtCreate");
        int userId = (userSession == null) ? -1 : userSession.getId();

        PageUtil<List<EssaySimilarQuestionGroupInfo>> pageUtil = singleQuestionSearch
                .findSimilarQuestionPageInfo(pageRequest, type);
        if (pageUtil.getTotal() == 0) {
            return pageUtil;
        }
        log.debug("单题列表接口... request: userId:{}, type:{}", userId, type);
        List<EssayQuestionVO> singleQuestionList = singleQuestionSearch.findSimilarQuestionList(pageUtil.getResult(),
                userId, EssayAnswerCardEnum.ModeTypeEnum.create(modeType));
        PageUtil<Object> result = PageUtil.builder().total(pageUtil.getTotal()).totalPage(pageUtil.getTotalPage())
                .next(pageUtil.getNext()).result(singleQuestionList).build();
        return result;
    }

    /**
     * 根据试题查询试题详情
     *
     * @param questionBaseId
     * @param modeType       1表示普通答题卡2课后作业答题卡
     * @return
     */
    @LogPrint
    @GetMapping(value = "questionDetail/{questionBaseId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public EssayQuestionVO questionDetail(@Token UserSession userSession,
                                          @RequestHeader int terminal,
                                          @RequestHeader String cv,
                                          @RequestParam(defaultValue = "1") Integer correctMode,
                                          @RequestParam(value = "bizStatus", defaultValue = "-1") Integer bizStatus,
                                          @RequestParam(value = "answerId", defaultValue = "0") Long answerId,
                                          @PathVariable(name = "questionBaseId") long questionBaseId,
                                          @RequestParam(defaultValue = "1") int modeType) throws BizException {
        log.info("questionBaseId: {},correctMode:{}", questionBaseId, correctMode);
        EssayQuestionVO detailV4 = essaySimilarQuestionService.findQuestionDetailV3(questionBaseId, userSession.getId(),
                correctMode, bizStatus, answerId,
                EssayAnswerCardEnum.ModeTypeEnum.create(modeType));

        return detailV4;
    }

}
