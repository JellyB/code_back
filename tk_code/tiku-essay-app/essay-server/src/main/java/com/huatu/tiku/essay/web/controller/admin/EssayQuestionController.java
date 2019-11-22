package com.huatu.tiku.essay.web.controller.admin;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.CommonErrors;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.cache.RedisKeyConstant;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.error.EssayExportErrors;
import com.huatu.tiku.essay.entity.EssayQuestionBase;
import com.huatu.tiku.essay.entity.EssayQuestionDetail;
import com.huatu.tiku.essay.entity.EssayStandardAnswer;
import com.huatu.tiku.essay.service.EssayExportService;
import com.huatu.tiku.essay.service.EssayPaperService;
import com.huatu.tiku.essay.service.EssayQuestionService;
import com.huatu.tiku.essay.service.EssaySimilarQuestionService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.admin.AdminMaterialListVO;
import com.huatu.tiku.essay.vo.admin.AdminQuestionDeductRuleVO;
import com.huatu.tiku.essay.vo.admin.AdminQuestionFormatVO;
import com.huatu.tiku.essay.vo.admin.AdminQuestionFullVO;
import com.huatu.tiku.essay.vo.admin.AdminQuestionKeyRuleVO;
import com.huatu.tiku.essay.vo.admin.AdminQuestionRelationVO;
import com.huatu.tiku.essay.vo.admin.AdminQuestionTypeVO;
import com.huatu.tiku.essay.vo.admin.AdminQuestionVO;
import com.huatu.tiku.essay.vo.export.EssayExportReqVO;
import com.huatu.tiku.essay.vo.resp.EssayMaterialVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionTypeVO;
import com.huatu.tiku.essay.vo.resp.EssayStandardAnswerVO;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by huangqp on 2017\12\5 0005.
 */
@RestController
@Slf4j
@RequestMapping("/end/essayQuestion")
public class EssayQuestionController {
    @Autowired
    EssayQuestionService essayQuestionService;
    @Autowired
    EssayRuleController essayRuleController;
    @Autowired
    private EssaySimilarQuestionService essaySimilarQuestionService;
    @Autowired
    private EssayExportService essayExportService;
    @Autowired
    EssayPaperService essayPaperService;
    @Autowired
    RedisTemplate redisTemplate;

    @LogPrint
    @GetMapping(value = "detail", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public AdminQuestionVO findQuestionsDetailById(@RequestParam(defaultValue = "0") long questionBaseId) {
        EssayQuestionBase base = essayQuestionService.findQuestionBaseById(questionBaseId);
        if (base == null) {
            return new AdminQuestionVO();
        }
        EssayQuestionDetail detail = essayQuestionService.findQuestionDetailById(base.getDetailId());
        if (detail == null) {
            throw new BizException(EssayErrors.NO_EXISTED_QUESTION_DETAIL);
        }

        String paperName = essayQuestionService.findPaperName(base.getPaperId());
        AdminQuestionVO resultVO = new AdminQuestionVO();
        BeanUtils.copyProperties(base, resultVO);
        BeanUtils.copyProperties(detail, resultVO);
        int type = detail.getType();
        AdminQuestionTypeVO questionTypeVO = essayQuestionService.getQuestionType(type);
        resultVO.setQuestionTypeName(questionTypeVO.getQuestionTypeName());
        resultVO.setQuestionType(questionTypeVO.getQuestionType());
        resultVO.setPaperName(paperName);
        resultVO.setIsLack(base.getIsLack());
        resultVO.setQuestionDetailId(detail.getId());
        resultVO.setQuestionBaseId(base.getId());
        //填充答案
        List<EssayStandardAnswer> standardAnswerList = essayQuestionService.findStandardAnswer(base.getDetailId());
        if (CollectionUtils.isNotEmpty(standardAnswerList)) {
            List<EssayStandardAnswerVO> essayStandardAnswerVOS = new LinkedList<>();
            standardAnswerList.forEach(answer -> {
                EssayStandardAnswerVO vo = new EssayStandardAnswerVO();
                BeanUtils.copyProperties(answer, vo);
                String inscribedName = vo.getInscribedName();
                if (StringUtils.isNotEmpty(inscribedName)) {
                    String[] split = inscribedName.split("<br/>");
                    List<String> inscribedNameList = Arrays.asList(split);
                    vo.setInscribedNameList(inscribedNameList);
                }
                essayStandardAnswerVOS.add(vo);
            });
            resultVO.setAnswerList(essayStandardAnswerVOS);
        }
        return resultVO;
    }

    @LogPrint
    @PostMapping(value = "detail", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public AdminQuestionVO saveQuestionsDetailById(@RequestBody AdminQuestionVO question) {
        int uid = -1;
        if (question.getQuestionBaseId() > 0) {
            //修改试题
            EssayQuestionBase base = essayQuestionService.findQuestionBaseById(question.getQuestionBaseId());
            return essayQuestionService.saveQuestionDetail(question, base.getPaperId(), uid);
        } else if (question.getPaperId() > 0) {
            //添加试题
            return essayQuestionService.saveQuestionDetail(question, question.getPaperId(), uid);
        } else {
            log.info("无效参数请求");
            throw new BizException(EssayErrors.ESSAY_PARAM_ILLEGAL);
        }
    }

    /**
     * 查询试题下材料列表
     */
    @LogPrint
    @GetMapping(value = "materialList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<EssayMaterialVO> materialList(@RequestParam(name = "questionBaseId", defaultValue = "0") long questionBaseId,
                                              @RequestParam(name = "paperId", defaultValue = "0") long paperId) {
        return essayQuestionService.materialList(questionBaseId, paperId);
    }

    /**
     * 编辑试题下材料
     */
    @LogPrint
    @PostMapping(value = "material", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public AdminMaterialListVO saveMaterial(@RequestBody AdminMaterialListVO vo) {
        return essayQuestionService.saveMaterial(vo);
    }

    /**
     * 查询试卷下试题信息
     */
    @LogPrint
    @GetMapping(value = "paper", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object findQuestionsByPaperId(@RequestParam long paperId) {
        List<EssayQuestionBase> questions = essayQuestionService.findQuestionsByPaperId(paperId);
        List<Map<String, Object>> resultList = Lists.newLinkedList();
        for (EssayQuestionBase question : questions) {
            Map<String, Object> questionMap = Maps.newHashMap();
            questionMap.put("questionBaseId", question.getId());
            questionMap.put("questionDetailId", question.getDetailId());
            // 查询试题名称
            EssayQuestionDetail questionDetail = essayQuestionService.findQuestionDetailById(question.getDetailId());
            if (null != questionDetail) {
                questionMap.put("questionName", questionDetail.getStem());
            }
            questionMap.put("sort", question.getSort());
            resultList.add(questionMap);
        }
        return resultList;
    }

    public List<AdminQuestionVO> findQuestionByPaperId(long paperId) {
        List<AdminQuestionVO> questionList = Lists.newLinkedList();
        List<EssayQuestionBase> questions = essayQuestionService.findQuestionsByPaperId(paperId);
        if (CollectionUtils.isEmpty(questions)) {
            return Lists.newLinkedList();
        }
        for (EssayQuestionBase essayQuestionBase : questions) {
            questionList.add(AdminQuestionVO.builder()
                    .sort(essayQuestionBase.getSort())
                    .questionBaseId(essayQuestionBase.getId())
                    .questionDetailId(essayQuestionBase.getDetailId())
                    .build());
        }
        questionList.sort((a, b) -> (a.getSort() - b.getSort()));
        return questionList;
    }

    /**
     * @param questionBaseId
     * @return
     */
    @LogPrint
    @GetMapping(value = "info", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public AdminQuestionFullVO findQuestionFullInfo(@RequestParam long questionBaseId, @RequestHeader(name = "admin", defaultValue = "") String admin, String uname) {
        if (StringUtils.isEmpty(admin)) {
            admin = uname;
        }
        AdminQuestionVO adminQuestionVO = findQuestionsDetailById(questionBaseId);
        if (StringUtils.isNoneBlank(admin)) {
            String userKeyByJY = RedisKeyConstant.getJYUserKey();
            Boolean isJYflag = redisTemplate.opsForSet().isMember(userKeyByJY, admin);
            if (isJYflag) {
                //如果是教育用户则不显示以下内容
                adminQuestionVO.setAnalyzeQuestion(null);//试题分析
                adminQuestionVO.setDifficultGrade(0D);//难度系数
                adminQuestionVO.setAuthorityReviews(null);//材料与标准答案点评

            }

        }
        long detailId = adminQuestionVO.getQuestionDetailId();
        AdminQuestionKeyRuleVO adminQuestionKeyRuleVO = essayRuleController.findKeyPhraseAndKeyWord(detailId, null, admin);
        AdminQuestionFormatVO adminQuestionFormatVO = essayRuleController.findAnswerFormatByQuestion(detailId);
        AdminQuestionDeductRuleVO adminQuestionDeductRuleVO = essayRuleController.findDeductRule(detailId, null, admin);
        return AdminQuestionFullVO.builder().singleQuestion(adminQuestionVO)
                .format(adminQuestionFormatVO)
                .keyRule(adminQuestionKeyRuleVO)
                .deductRules(adminQuestionDeductRuleVO)
                .build();
    }

    /**
     * 查询试题关联的信息，包括试卷信息和材料信息
     */
    @LogPrint
    @GetMapping(value = "relationInfo", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public AdminQuestionRelationVO findQuestionRelationInfo(@RequestParam long questionBaseId) {
        AdminQuestionRelationVO relationInfo = essayQuestionService.findQuestionRelationInfo(questionBaseId);
        return relationInfo;
    }

    /**
     * @param stem
     * @param year
     * @param areaId
     * @param type
     * @param page
     * @param pageSize
     * @return
     */
    @LogPrint
    @GetMapping(value = "questions", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object findQuestionListByCondition(@RequestParam(defaultValue = "") String stem,
                                              @RequestParam(defaultValue = "") String year,
                                              @RequestParam(defaultValue = "-1") long areaId,
                                              @RequestParam(defaultValue = "-1") int type,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "20") int pageSize) {
        PageRequest pageable = new PageRequest(page - 1, pageSize, Sort.Direction.DESC, "gmtCreate");
        if ("0".equals(year.trim())) {
            year = "";
        }
        PageUtil<AdminQuestionVO> relationInfo = essayQuestionService.findQuestionListByCondition(stem, year, areaId, type, pageable);
        return relationInfo;
    }


    @LogPrint
    @PostMapping(value = "answer", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public EssayStandardAnswer saveAnswer(@RequestBody EssayStandardAnswerVO standardAnswer) {
        return essayQuestionService.saveStandardAnswer(standardAnswer);
    }


    @LogPrint
    @DeleteMapping(value = "answer/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public int delAnswer(@PathVariable long id) {
        return essayQuestionService.delStandardAnswer(id);
    }


    /**
     * 查询试题类型
     * 缓存中获取  永久缓存
     *
     * @return
     * @modify zw
     */
    @GetMapping(value = "questionTypeList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<EssayQuestionTypeVO> questionType() {
        return essaySimilarQuestionService.findQuestionTypeV2();
    }


    /**
     * 删除题目
     *
     * @return
     * @modify zw
     */
    @DeleteMapping(value = "{questionBaseId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public int delQuestion(@PathVariable long questionBaseId) {
        return essayPaperService.resetQuestion(questionBaseId);
//        int delQuestion = essaySimilarQuestionService.delQuestion(questionBaseId);
//
//        HashMap<String, Boolean> map = new HashMap<>();
//        boolean flag = (delQuestion == 1);
//        map.put("flag", flag);
//        return map;
    }


    /**
     * 试题绑定视频
     *
     * @param questionId
     * @param videoId
     * @return
     */
    @LogPrint
    @PostMapping(value = "video", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object boundVideo(@RequestParam Long questionId, @RequestParam Integer videoId) {
        if (questionId == null || questionId <= 0) {
            log.info("试题ID有误，请检查试题ID后重试,questionId:{}", questionId);
            throw new BizException(EssayErrors.ERROR_QUESTION_ID);
        }

        if (questionId == null || questionId <= 0) {
            log.info("视频ID有误，请检查视频ID后重试,videoId:{}", videoId);
            throw new BizException(EssayErrors.ERROR_VIDEO_ID);
        }
        return essayQuestionService.boundVideo(questionId, videoId);
    }


    /**
     * 试题和视频取消绑定
     *
     * @param questionId
     * @return
     */
    @LogPrint
    @DeleteMapping(value = "video", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object cancelBoundVideo(@RequestParam Long questionId) {
        if (questionId == null || questionId <= 0) {
            log.info("试题ID有误，请检查试题ID后重试,questionId:{}", questionId);
            throw new BizException(EssayErrors.ERROR_QUESTION_ID);
        }

        return essayQuestionService.cancelBoundVideo(questionId);
    }


    /**
     * 设置题目批改算法规则
     * 1关键词匹配  2关键句匹配
     *
     * @param questionDetailId
     * @param correctType
     * @return
     */
    @LogPrint
    @PutMapping(value = "type", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object setCorrectType(@RequestParam Long questionDetailId, @RequestParam int correctType) {
        return essayQuestionService.setCorrectType(questionDetailId, correctType);
    }


    /**
     * 根据试题id查询学员答案
     *
     * @return
     */
    @LogPrint
    @GetMapping(value = "answer", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object answerList(@RequestParam(defaultValue = "0") long questionBaseId,
                             @RequestParam(defaultValue = "0") double examScoreMax,
                             @RequestParam(defaultValue = "0") double examScoreMin,
                             @RequestParam(defaultValue = "0") int inputWordMax,
                             @RequestParam(defaultValue = "0") int inputWordMin,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "20") int pageSize) {
        if (questionBaseId < 0) {
            log.info("试题ID错误，请选择正确的试题。questionBaseId：{}", questionBaseId);
            throw new BizException(EssayErrors.ERROR_QUESTION_ID);
        }
        return essayQuestionService.getAnswerByConditions(questionBaseId, examScoreMax, examScoreMin, inputWordMax, inputWordMin, page, pageSize);
    }


    /**
     * 导出学员答案
     *
     * @return
     */
    @LogPrint
    @PostMapping(value = "answer/export", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String answerExport(@RequestBody EssayExportReqVO vo) {

        if (vo == null) {
            log.warn("参数错误，请求参数不能为空");
            throw new BizException(CommonErrors.INVALID_ARGUMENTS);
        }

        if (CollectionUtils.isEmpty(vo.getAnswerIdList())) {
            log.warn("参数错误，所选答题卡列表为空。");
            throw new BizException(EssayExportErrors.EMPTY_LIST);
        }

        //导出word
        if (vo.getType() != EssayExportReqVO.ANSWER_WITH_CORRECTED && vo.getType() != EssayExportReqVO.ANSWER_WITHOUT_CORRECTED) {
            log.warn("参数错误，导出范围错误。fileType:{}", vo.getFileType());
            throw new BizException(EssayExportErrors.ERROR_TYPE);
        }

        //导出word
        if (vo.getFileType() != EssayExportReqVO.FILE_TYPE_WORD && vo.getFileType() != EssayExportReqVO.FILE_TYPE_PDF) {
            log.warn("参数错误，文件类型错误。fileType:{}", vo.getFileType());
            throw new BizException(EssayExportErrors.ERROR_FILE_TYPE);
        }

        return essayExportService.exportAnswer(vo);
    }





}
