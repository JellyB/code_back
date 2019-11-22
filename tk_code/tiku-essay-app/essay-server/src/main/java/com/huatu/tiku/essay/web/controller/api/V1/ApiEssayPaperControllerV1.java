package com.huatu.tiku.essay.web.controller.api.V1;

import com.google.common.collect.Lists;
import com.huatu.common.consts.TerminalType;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.constant.cache.AreaRedisKeyConstant;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.vo.resp.EssayPaperQuestionVO;
import com.huatu.tiku.essay.vo.resp.EssayPaperVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionAreaVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionVO;
import com.huatu.tiku.essay.service.EssayMaterialService;
import com.huatu.tiku.essay.service.EssayPaperService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 套题
 * Created by huangqp on 2017\11\23 0023.
 */
@RestController
@RequestMapping("api/v1/paper")
@Slf4j
public class ApiEssayPaperControllerV1 {

    @Autowired
    EssayPaperService essayPaperService;
    @Autowired
    EssayMaterialService essayMaterialService;
    @Autowired
    RedisTemplate redisTemplate;


    /**
     * 通过地区返回试卷列表
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
    public Object paperList(@Token UserSession userSession,
                            @RequestHeader int terminal,
                            @RequestHeader String cv,
                            @PathVariable(name = "areaId") long areaId,
                            @RequestParam(name = "page", defaultValue = "1") int page,
                            @RequestParam(name = "pageSize", defaultValue = "20") int pageSize) {

        log.info("areaId: {}", areaId);
        if(9999 == areaId){
            return essayPaperService.getGuFenPapers();
        }
        Pageable pageable = new PageRequest(page - 1, pageSize, Sort.Direction.DESC, "paperYear", "paperDate", "areaId", "subAreaId");
        long count = essayPaperService.countPapersByArea(areaId, userSession.getId());
        List<EssayPaperVO> papers = null;

        if (count > 0) {
            papers = essayPaperService.findPaperListByArea(areaId, userSession.getId(),EssayAnswerCardEnum.ModeTypeEnum.NORMAL, pageable);
        }
        PageUtil pageUtil = PageUtil.builder().result(papers).next(((int) count) > page * pageSize ? 1 : 0).build();
        return pageUtil;
    }

    /**
     * 返回试卷所在的所有地区列表
     *
     * @return
     */
    @LogPrint
    @GetMapping(value = "areaList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<EssayQuestionAreaVO> multiQuestionList(@RequestHeader int terminal,
                                                       @RequestHeader String cv) {

        List<EssayQuestionAreaVO> areaList = Lists.newCopyOnWriteArrayList(essayPaperService.findAreaList());
        //根据版本号，判断是否展示估分地区（安卓7.1.9之后的版本 ios7.1.8之后的版本)去掉版本限制
//        if (("7.1.9".compareTo(cv) <= 0 && (TerminalType.ANDROID_IPAD ==terminal || TerminalType.ANDROID ==terminal))
//                || ("7.1.8".compareTo(cv) <= 0 && (TerminalType.IPHONE_IPAD ==terminal || TerminalType.IPHONE ==terminal))) {
            String showGufenAreaKey = AreaRedisKeyConstant.getShowGufenAreaKey();
            Integer showGufenArea = (Integer) redisTemplate.opsForValue().get(showGufenAreaKey);
            if (showGufenArea != null && showGufenArea != 0) {
                EssayQuestionAreaVO areaVO = EssayQuestionAreaVO.builder()
                        .id(9999L)
                        .name("估分")
                        .build();
                areaList.add(0, areaVO);
            }
//        }
        return areaList;
    }

    /**
     * 获得试卷材料
     *
     * @param userSession
     * @param terminal
     * @param cv
     * @param paperId
     * @return
     */
    @LogPrint
    @GetMapping(value = "materialList/{paperId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object getMaterialsByPaper(@Token UserSession userSession,
                                      @RequestHeader int terminal,
                                      @RequestHeader String cv,
                                      @PathVariable(name = "paperId") long paperId) {
        return essayMaterialService.findMaterialsByPaperId(paperId);
    }

    /**
     * 获取试卷题目
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
                                         @RequestParam(defaultValue = "1") int modeType) {
        int userId = userSession.getId();
        //单个答案
//        return essayPaperService.findQuestionDetailByPaperId(paperId,userId);
        //多个答案兼容单个答案
        EssayPaperQuestionVO detailByPaperIdV1 = essayPaperService.findQuestionDetailByPaperIdV1(paperId, userId, EssayAnswerCardEnum.ModeTypeEnum.create(modeType));

        if ((terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD)
                && (cv.compareTo("6.1.3") > 0)) {
            List<EssayQuestionVO> essayQuestions = detailByPaperIdV1.getEssayQuestions();
            if (CollectionUtils.isNotEmpty(essayQuestions)) {
                for (EssayQuestionVO essayQuestion : essayQuestions) {
                    essayQuestion.setCommitWordNumMax(essayQuestion.getInputWordNumMax());//最多答题字数（提交的字数）
                    essayQuestion.setInputWordNumMax(essayQuestion.getInputWordNumMax());//最多录入字数（录入的字数）
                }
            }
        }
        return detailByPaperIdV1;
    }
}
