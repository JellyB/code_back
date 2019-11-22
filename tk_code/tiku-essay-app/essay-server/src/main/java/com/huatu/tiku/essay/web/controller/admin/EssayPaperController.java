package com.huatu.tiku.essay.web.controller.admin;

import com.google.common.collect.Lists;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.entity.BaseEntity;
import com.huatu.tiku.essay.entity.EssayPaperBase;
import com.huatu.tiku.essay.service.paper.SyncPaperService;
import com.huatu.tiku.essay.vo.admin.*;
import com.huatu.tiku.essay.vo.resp.EssayMaterialVO;
import com.huatu.tiku.essay.vo.resp.EssayPaperDetailVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionAreaVO;
import com.huatu.tiku.essay.vo.resp.EssaySimpleQuestionVO;
import com.huatu.tiku.essay.service.EssayPaperService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2017\12\4 0004.
 */
@RestController
@Slf4j
@RequestMapping("/end/essayPaper")
public class EssayPaperController {
    @Autowired
    EssayPaperService essayPaperService;

    @Autowired
    EssayQuestionController essayQuestionController;
    @Autowired
    EssayRuleController essayRuleController;
    @Autowired
    EssayMaterialController essayMaterialController;
    @Autowired
    SyncPaperService syncPaperService;

    /**
     * 查询试卷列表
     *
     * @return 套题id，名称，地区，年份，日期，套题状态，审核状态
     */
    @LogPrint
    @GetMapping(value = "paperList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PageUtil<AdminPaperWithQuestionVO> findByConditions(@RequestParam(defaultValue = "") String name,
                                                               @RequestParam(defaultValue = "-1") long areaId,
                                                               @RequestParam(defaultValue = "") String year,
                                                               @RequestParam(defaultValue = "0") int status,
                                                               @RequestParam(defaultValue = "-1") int bizStatus,
                                                               @RequestParam(defaultValue = "-1") int type,
                                                               @RequestParam(defaultValue = "-1") int mockType,
                                                               @RequestParam(defaultValue = "1") int page,
                                                               @RequestParam(defaultValue = "-1") int tag,
                                                               @RequestParam(defaultValue = "20") int pageSize,
                                                               @RequestParam(defaultValue = "-1") int paperId,
                                                               @RequestParam(defaultValue = "-1") int questionId,
                                                               @RequestHeader(name = "admin", defaultValue = "") String admin) {
        PageRequest pageable = new PageRequest(page - 1, pageSize, Sort.Direction.DESC, "gmtCreate");
        if ("0".equals(year.trim())) {
            year = "";
        }
        return essayPaperService.findByConditions(name, areaId, year, status, type, bizStatus, pageable, mockType, tag, questionId, paperId,admin);

    }

    /**
     * 创建、修改试卷
     *
     * @param essayPaper
     * @return
     */
    @LogPrint
    @PostMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public AdminPaperVO addEssayPaper(@RequestBody AdminPaperVO essayPaper) {
        if (essayPaper.getAreaId() <= 0 || StringUtils.isBlank(essayPaper.getName()) ||
                StringUtils.isBlank(essayPaper.getPaperDate()) ||
                StringUtils.isBlank(essayPaper.getPaperYear()) ||
                essayPaper.getScore() <= 0 || essayPaper.getLimitTime() <= 0) {
            throw new BizException(EssayErrors.ESSAY_PARAM_ILLEGAL);
        }
        AdminPaperVO adminPaperVO = essayPaperService.addEssayPaper(essayPaper);
        syncPaperService.syncPaperInfo(adminPaperVO.getPaperId());
        return adminPaperVO;
    }

//    /**
//     * 修改试题
//     * @param essayPaper
//     * @return
//     */
//    @PutMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//    public EssayPaperVO updateEssayPaper(EssayPaperVO essayPaper){
//        return essayPaperService.saveEssayPaper(essayPaper);
//    }

    /**
     * 修改试卷状态
     *
     * @return
     */
    @LogPrint
    @PutMapping(path = "status/{paperId}/{type}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public int paperStatus(@PathVariable("paperId") long paperId, @PathVariable("type") Integer type) {
        log.info("paperId: {}, status: {} ", paperId, type);
        int i = essayPaperService.modifyPaperStatusById(type, paperId);
        syncPaperService.syncPaperInfo(paperId);
        return i;
    }


    /**
     * 查询试卷详情
     *
     * @param paperId
     * @return
     */
    @LogPrint
    @GetMapping(value = "paperDetail", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public EssayPaperDetailVO findPaperDetails(@RequestParam long paperId, @RequestHeader(name = "admin", defaultValue = "") String admin) {
        AdminEssayPaperVO adminEssayPaperVO = getPaperInfo(paperId);
        AdminPaperWithQuestionVO paperInfo = new AdminPaperWithQuestionVO();
        BeanUtils.copyProperties(adminEssayPaperVO, paperInfo);
        List<AdminQuestionVO> questions = essayQuestionController.findQuestionByPaperId(paperId);
        paperInfo.setQuestions(questions);
        List<EssayMaterialVO> materials = essayMaterialController.findMaterialsByPaperId(paperId);
        AdminQuestionFullVO adminQuestionFullVO = new AdminQuestionFullVO();
        if (CollectionUtils.isNotEmpty(questions)) {
            AdminQuestionVO adminQuestion = questions.get(0);
            long questionBaseId = adminQuestion.getQuestionBaseId();
            adminQuestionFullVO = essayQuestionController.findQuestionFullInfo(questionBaseId,null,admin);
        }
        return EssayPaperDetailVO.builder().essayPaper(paperInfo)
                .essayMaterials(materials).question(adminQuestionFullVO).build();
    }


    /**
     * 查询试卷下题目列表
     *
     * @param paperId
     * @return
     */
    @LogPrint
    @GetMapping(value = "questionList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<AdminSingleQuestionVO> findQuestionListByPaper(@RequestParam long paperId) {
        return essayPaperService.findQuestionListByPaper(paperId, false);
    }

    @LogPrint
    @GetMapping(value = "batchQuestionList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<EssaySimpleQuestionVO> findQuestionListByPapers(@RequestParam String paperIds) {
        List ids = Lists.newLinkedList();
        for (String id : paperIds.split(",")) {
            try {
                ids.add(Long.parseLong(id));
            } catch (Exception e) {
                log.error("questionId style not is Long : {}", id);
                ErrorResult errorResult = EssayErrors.COMMON_DATA_STYLE_ERROR;
                throw new BizException(errorResult);
            }
        }
        return essayPaperService.findQuestionListByPapers(ids);
    }

    /**
     * 删除试卷下题目
     * （改成重置，清空算法，只保留题干和标答）
     *
     * @param questionBaseId
     * @return
     */
    @LogPrint
    @PutMapping(value = "question", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public int deleteQuestion(@RequestParam long questionBaseId) {
        //删除试题
//        return essayPaperService.deleteQuestion(questionBaseId, paperId);
        //重置试题
        return essayPaperService.resetQuestion(questionBaseId);

    }


    @LogPrint
    @GetMapping(value = "info", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public AdminEssayPaperVO getPaperInfo(@RequestParam long paperId) {
        EssayPaperBase essayPaperBase = essayPaperService.findPaperInfoById(paperId);
        if (essayPaperBase == null) {
            throw new BizException(EssayErrors.NO_EXISTED_PAPER);
        }
        AdminEssayPaperVO resultVO = new AdminEssayPaperVO();
        BeanUtils.copyProperties(essayPaperBase, resultVO);
        return resultVO;
    }

    /**
     * 返回试卷所在的所有地区列表
     *
     * @return
     */
    @LogPrint
    @GetMapping(value = "areaList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<EssayQuestionAreaVO> multiQuestionList( @RequestHeader(name = "admin", defaultValue = "") String admin) {

        return essayPaperService.findAreaListNoBiz(admin);
    }

    @GetMapping(value = "sync",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<Long> syncPapers(){
        List<EssayPaperBase> all = essayPaperService.findAll();
        all.stream().mapToLong(BaseEntity::getId).forEach(syncPaperService::syncPaperInfo);
        return all.stream().map(BaseEntity::getId).collect(Collectors.toList());
    }

}
