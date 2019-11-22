package com.huatu.tiku.match.web.controller.v1.paper;

import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.match.service.v1.paper.PaperService;
import com.huatu.tiku.match.service.v1.paper.QuestionService;
import com.huatu.tiku.match.service.v1.search.WhiteListService;
import com.huatu.tiku.springboot.users.support.Token;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户提交答题卡信息
 * Created by lijun on 2018/10/18
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/question")
@ApiVersion("v1")
public class QuestionController {

    final PaperService paperService;

    final QuestionService questionService;

    final WhiteListService whiteListService;

    /**
     * 获取答题时试题详情
     */
    @GetMapping(value = "{paperId}/simpleInfo")
    public Object questionSimpleInfo(@PathVariable int paperId) {
        return questionService.findQuestionSimpleBoByPaperId(paperId);
    }


    /**
     * 获取答题时试题详情
     */
    @GetMapping(value = "{paperId}/pre")
    public Object questionPreLookCard(@PathVariable int paperId,
                                      @Token UserSession userSession) throws BizException {
        if(null != userSession){
            Boolean whiteMember = whiteListService.isWhiteMember(userSession.getId());
            if(whiteMember){
                return paperService.findPaperCacheById(paperId);
            }
        }
        throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
    }
}
