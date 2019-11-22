package com.huatu.tiku.essay.web.controller.api.v4;

import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.vo.resp.EssayPaperQuestionVO;
import com.huatu.tiku.essay.vo.resp.EssayPaperVO;
import com.huatu.tiku.essay.service.EssayMaterialService;
import com.huatu.tiku.essay.service.EssayPaperService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.util.admin.EssayAnalyzeUtil;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 套题管理
 */
@RestController
@RequestMapping("api/v4/paper")
@Slf4j
public class ApiEssayPaperControllerV4 {

    @Autowired
    EssayPaperService essayPaperService;
    @Autowired
    EssayMaterialService essayMaterialService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     * 通过地区返回试卷列表（增加人工批改对应状态）
     *
     * @param userSession
     * @param terminal
     * @param cv
     * @param areaId
     * @param page
     * @param pageSize
     * @return
     */
    @LogPrint
    @GetMapping(value = "list/{areaId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object paperList(@Token(check = false) UserSession userSession,
                            @RequestHeader int terminal,
                            @RequestHeader String cv,
                            @PathVariable(name = "areaId") long areaId,
                            @RequestParam(name = "page", defaultValue = "1") int page,
                            @RequestParam(name = "pageSize", defaultValue = "20") int pageSize) {

        int userId = (userSession == null) ? -1 : userSession.getId();

        if (9999 == areaId) {
            return essayPaperService.getGuFenPapers();
        }
        Pageable pageable = new PageRequest(page - 1, pageSize, Sort.Direction.DESC, "paperYear", "paperDate", "areaId", "subAreaId");
        long count = essayPaperService.countPapersByArea(areaId, userId);
        List<EssayPaperVO> papers = null;

        if (count > 0) {
            papers = essayPaperService.findPaperListByAreaV2(areaId, userId, pageable, EssayAnswerCardEnum.ModeTypeEnum.NORMAL);
        }
        PageUtil pageUtil = PageUtil.builder().result(papers).next(((int) count) > page * pageSize ? 1 : 0).build();
        return pageUtil;
    }

    /**
     * 获取试题问题信息（包含人工批注相关）
     *
     * @param userSession
     * @param terminal
     * @param cv
     * @param paperId
     * @return
     */
    @LogPrint
    @GetMapping(value = "questionList/{paperId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object getQuestionsWithAnswer(@Token UserSession userSession,
                                         @RequestHeader int terminal,
                                         @RequestHeader String cv,
                                         @PathVariable(name = "paperId") long paperId,
                                         @RequestParam(value = "bizStatus", defaultValue = "-1") Integer bizStatus,
                                         @RequestParam(value = "answerId", defaultValue = "0") Long answerId,
                                         @RequestParam(defaultValue = "1") Integer correctMode,
                                         @RequestParam(defaultValue = "1") Integer modeType) {
        int userId = userSession.getId();
        EssayPaperQuestionVO essayPaperQuestionVO = essayPaperService.findQuestionDetailByPaperIdV4(paperId, userId, correctMode, bizStatus, answerId, EssayAnswerCardEnum.ModeTypeEnum.create(modeType));
        return essayPaperQuestionVO;
    }


}
