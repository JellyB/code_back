package com.huatu.tiku.essay.web.controller.api.V1;

import com.huatu.common.consts.TerminalType;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.entity.EssayMaterial;
import com.huatu.tiku.essay.entity.EssayMockExam;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.service.paper.SyncPaperService;
import com.huatu.tiku.essay.util.system.VersionUtil;
import com.huatu.tiku.essay.util.system.VersionUtil;
import com.huatu.tiku.essay.vo.resp.EssayPaperQuestionVO;
import com.huatu.tiku.essay.vo.resp.EssayPaperVO;
import com.huatu.tiku.essay.vo.resp.PaperCommitVO;
import com.huatu.tiku.essay.vo.resp.ResponseVO;
import com.huatu.tiku.essay.entity.vo.report.EssayAnswerCardVO;
import com.huatu.tiku.essay.service.EssayMockExamService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Created by x6 on 2017/12/15.
 * 模考相关
 */
@RestController
@Slf4j
@RequestMapping(value = "api/v1/mock", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ApiMockExamController {

    @Autowired
    EssayMockExamService essayMockExamService;
    @Autowired
    SyncPaperService syncPaperService;

    /**
     * 查询模考试卷材料
     * （MQ创建答题卡）
     */
    @LogPrint
    @GetMapping(value = "materialList/{paperId}")
    public List<EssayMaterial> materialList(@Token UserSession userSession,
                                            @RequestHeader int terminal,
                                            @RequestHeader String cv,
                                            @PathVariable(name = "paperId") long paperId) throws BizException {
        return essayMockExamService.materialList(userSession.getId(), paperId, terminal);
    }

    /**
     * 查询模考试卷   试题信息
     */
    @LogPrint
    @GetMapping(value = "questionList/{paperId}")
    public EssayPaperQuestionVO questionList(@Token UserSession userSession,
                                             @RequestHeader int terminal,
                                             @RequestHeader String cv,
                                             @PathVariable(name = "paperId") long paperId) throws BizException {
        return essayMockExamService.questionList(userSession.getId(), paperId, terminal);
    }

    /**
     * 申论模考 -- 交卷保存（暂时不扣批改次数）
     */
    @LogPrint
    @PostMapping(value = "mockAnswerCard")
    public int paperCommit(@Token UserSession userSession,
                           @RequestHeader int terminal,
                           @RequestHeader String cv,
                           @RequestBody PaperCommitVO paperCommitVO) throws BizException {

        return essayMockExamService.paperCommit(userSession, paperCommitVO, terminal);
    }


    /**
     * 查询模考历史,不做分页
     * 不用每次都实时计算，可以直接将结果缓存()
     * tag 默认是是3 2019年模考标签
     *
     * @return
     * @throws BizException
     */
    @LogPrint
    @GetMapping(value = "history")
    public Map getHistory(@Token UserSession userSession,
                          @RequestHeader int terminal,
                          @RequestHeader String cv,
                          @RequestParam(defaultValue = "-1") int tag,
                          @RequestParam(defaultValue = "14") int subjectId) throws BizException {

        if (terminal == TerminalType.ANDROID || terminal == TerminalType.ANDROID_IPAD) {
            if (VersionUtil.compareVersion(cv, "7.1.162") <= 0) {
                tag = -1;
            }
        }
        log.info("模考历史,terminal:{},cv:{},tag:{}", terminal, cv, tag);
        return essayMockExamService.getHistory(userSession.getId(), tag);
    }


    /**
     * 查询模考成绩报告
     * 不用每次都实时计算，可以直接将结果缓存
     *
     * @return
     * @throws BizException
     */
    @LogPrint
    @GetMapping(value = "report")
    public EssayAnswerCardVO getReport(@Token UserSession userSession,
                                       @RequestHeader int terminal,
                                       @RequestHeader String cv,
                                       @RequestParam long paperId) throws BizException {
        EssayAnswerCardVO essayAnswerCardVO = new EssayAnswerCardVO(essayMockExamService.getReport(userSession.getId(), paperId, terminal, EssayAnswerCardEnum.ModeTypeEnum.NORMAL));
        //fix 安卓7.1.8以及7.1.7 模考大赛闪退bug修复

        EssayAnswerCardVO essayAnswerCardResult = essayMockExamService.dealAnswerCardScore(essayAnswerCardVO);
        if (null != essayAnswerCardResult) {
            if (("7.1.8".equals(cv) || "7.1.7".equals(cv)) && (TerminalType.ANDROID == terminal || TerminalType.ANDROID_IPAD == terminal)) {
                essayAnswerCardResult.setQuestionList(null);
            }
        }
        return essayAnswerCardResult;
    }


    /**
     * 查询模考剩余时间
     */
    @LogPrint
    @GetMapping(value = "leftTime")
    public ResponseVO getLeftTime(@Token UserSession userSession,
                                  @RequestParam long paperId) throws BizException {
        return essayMockExamService.getLeftTime(userSession.getId(), paperId);
    }

    /**
     * 修改申论模考状态(行测端调用)
     */
    @LogPrint
    @PostMapping(value = "status", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object updateStatus(
            @RequestParam int type,
            @RequestParam long id,
            @RequestParam long practiceId) throws BizException {
        Object o = essayMockExamService.updateMockStatus(type, practiceId, id);
        syncPaperService.syncPaperInfo(id);
        return o;
    }


    /**
     * 查询模考信息(行测端使用)
     */
    @LogPrint
    @GetMapping(value = "")
    public EssayMockExam getMock(@RequestParam long id) throws BizException {
        return essayMockExamService.getMock(id);
    }


    /**
     * 查询往期模考试卷
     */
    @LogPrint
    @GetMapping(value = "papers")
    public PageUtil<EssayPaperVO> getHistoryPaperList(@Token UserSession userSession,
                                                      @RequestParam(name = "page", defaultValue = "1") int page,
                                                      @RequestParam(name = "pageSize", defaultValue = "20") int pageSize,
                                                      @RequestParam(defaultValue = "-1") int tag,
                                                      @RequestHeader(defaultValue = "1") int terminal,
                                                      @RequestHeader(defaultValue = "7.1.162") String cv) throws BizException {
        int userId = userSession.getId();
        if (terminal == TerminalType.ANDROID || terminal == TerminalType.ANDROID_IPAD) {
            if (VersionUtil.compareVersion(cv, "7.1.162") <= 0) {
                tag = -1;
            }
        }
        log.info("往期模考,terminal:{},cv:{},tag:{}", terminal, cv, tag);
        return essayMockExamService.getHistoryPaperList(userId, page, pageSize, tag,EssayAnswerCardEnum.ModeTypeEnum.NORMAL);
    }

    /**
     * 备用接口（如果模考大赛答题卡提交失败，手动处理的借口）
     */
    @LogPrint
    @PostMapping(value = "card")
    public Object handCommit(@RequestParam int userId, @RequestParam long paperId) throws BizException {
        return essayMockExamService.handCommit(userId, paperId);
    }


}
