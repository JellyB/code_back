package com.huatu.tiku.essay.web.controller.api.V1;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.status.AdminKeyPhraseTypeConstant;
import com.huatu.tiku.essay.constant.status.AdminPaperConstant;
import com.huatu.tiku.essay.entity.EssayMockExam;
import com.huatu.tiku.essay.entity.EssayPaperBase;
import com.huatu.tiku.essay.entity.EssayQuestionBase;
import com.huatu.tiku.essay.repository.EssayMockExamRepository;
import com.huatu.tiku.essay.repository.EssayPaperBaseRepository;
import com.huatu.tiku.essay.service.*;
import com.huatu.tiku.essay.util.common.InnerQuestionUtil;
import com.huatu.tiku.essay.vo.resp.*;
import com.huatu.tiku.essay.vo.admin.*;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.web.controller.admin.EssayQuestionController;
import com.huatu.tiku.essay.web.controller.admin.EssayRuleController;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.EAN;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * essay辅助接口创建（内部维护使用-客户端不会调用）
 * Created by huangqp on 2018\2\26 0026.
 */
@RestController
@Slf4j
@RequestMapping("api/v1/assist")
public class ApiEssayAssistController {
    @Autowired
    private EssayMockExamService essayMockExamService;
    @Autowired
    private EssayPaperService essayPaperService;
    @Autowired
    private EssayMaterialService essayMaterialService;
    @Autowired
    private EssayQuestionService essayQuestionService;
    @Autowired
    private EssayQuestionController essayQuestionController;
    @Autowired
    private EssayRuleController essayRuleController;

    @Autowired
    EssayRuleService essayRuleService;

    @Autowired
    EssayMockExamRepository essayMockExamRepository;

    @Autowired
    EssayPaperBaseRepository essayPaperBaseRepository;

    @LogPrint
    @GetMapping(value = "mock/all", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public EssayPaperFullVO mockDetail(@RequestParam long paperId) throws BizException {
        EssayPaperFullVO essayPaperFullVO = new EssayPaperFullVO();
        //查询模考试卷信息
        EssayMockExamVO essayMockExamVO = essayMockExamService.queryMockPaper(paperId);
        essayPaperFullVO.setEssayMockExamVO(essayMockExamVO);
        //查询所有试卷下的材料
        List<EssayMaterialVO> materials = essayMaterialService.findMaterialsByPaperId(paperId);
        essayPaperFullVO.setMaterials(materials);
        //查询所有的试题信息
        List<EssayQuestionFullVO> questions = Lists.newArrayList();
        List<EssayQuestionBase> questionBases = essayQuestionService.findQuestionsByPaperId(paperId);
        questionBases.sort((a, b) -> (int) (a.getId() - b.getId()));
        for (EssayQuestionBase essayQuestionBase : questionBases) {
            EssayQuestionFullVO essayQuestionFullVO = new EssayQuestionFullVO();
            essayQuestionFullVO.setEssayQuestionBase(essayQuestionBase);
            AdminQuestionVO adminQuestionVO = essayQuestionController.findQuestionsDetailById(essayQuestionBase.getId());
            essayQuestionFullVO.setAdminQuestionVO(adminQuestionVO);
            List<EssayMaterialVO> materialList = essayQuestionController.materialList(essayQuestionBase.getId(), paperId);
            essayQuestionFullVO.setMaterialList(materialList);
            AdminQuestionKeyRuleVO adminQuestionKeyRuleVO = essayRuleController.findKeyPhraseAndKeyWord(essayQuestionBase.getDetailId(), null, null);
            essayQuestionFullVO.setAdminQuestionKeyRuleVO(adminQuestionKeyRuleVO);
            AdminQuestionDeductRuleVO adminQuestionDeductRuleVO = essayRuleController.findDeductRule(essayQuestionBase.getDetailId(), null, null);
            essayQuestionFullVO.setAdminQuestionDeductRuleVO(adminQuestionDeductRuleVO);
            AdminQuestionFormatVO adminQuestionFormatVO = essayRuleController.findAnswerFormatByQuestion(essayQuestionBase.getDetailId());
            essayQuestionFullVO.setAdminQuestionFormatVO(adminQuestionFormatVO);
            questions.add(essayQuestionFullVO);
        }
        essayPaperFullVO.setQuestionFullVOS(questions);
        return essayPaperFullVO;
    }

    @LogPrint
    @PostMapping(value = "mock/all", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object mock(@RequestBody EssayPaperFullVO essayPaperFullVO) throws BizException {
        EssayMockExam exam = null;
        String userId = "assist";
        //查询模考试卷信息
        EssayMockExamVO essayMockExamVO = essayPaperFullVO.getEssayMockExamVO();
        essayMockExamVO.setId(-1);
        Object essayMockExam = essayMockExamService.saveMockPaper(essayMockExamVO);
        if (essayMockExam != null && essayMockExam instanceof EssayMockExam) {
            exam = (EssayMockExam) essayMockExam;
        } else {
            log.warn("essayMockExam is null or illgeal");
            return null;
        }
        final Long mockId = exam.getId();
        //查询所有试卷下的材料
        List<EssayMaterialVO> materials = essayPaperFullVO.getMaterials();
        //添加试卷下的材料(没有返回值)
        AdminMaterialListVO adminMaterialVO = new AdminMaterialListVO();
        adminMaterialVO.setPaperId(mockId);
        Map<Long, Long> materialMap = Maps.newHashMap();     //sort->id
        materials.forEach(i -> {
            materialMap.put(i.getSort().longValue(), i.getId());
            i.setPaperId(mockId);
            i.setId(0L);
        });//sort和旧的材料ID的对应关系
        adminMaterialVO.setMaterialList(materials);
        essayMaterialService.saveMaterial(adminMaterialVO, userId);
        List<EssayMaterialVO> materialVOList = essayMaterialService.findMaterialsByPaperId(mockId); //查询用户材料列表返回材料ID
        materialVOList.forEach(i -> materialMap.put(materialMap.get(i.getSort().longValue()), i.getId()));     //sort和新的材料ID的对应关系
        //查询所有的试题信息
        List<EssayQuestionFullVO> questions = essayPaperFullVO.getQuestionFullVOS();

        for (EssayQuestionFullVO essayQuestionFullVO : questions) {
            AdminQuestionVO adminQuestionVO = essayQuestionFullVO.getAdminQuestionVO();
            adminQuestionVO.setPaperId(mockId);
            adminQuestionVO.setQuestionBaseId(-1);
            adminQuestionVO.setQuestionDetailId(-1);
            AdminQuestionVO adminQuestion = essayQuestionController.saveQuestionsDetailById(adminQuestionVO);
            List<EssayStandardAnswerVO> answerList = adminQuestionVO.getAnswerList();
            for (EssayStandardAnswerVO essayStandardAnswerVO : answerList) {
                long questionDetailId = adminQuestion.getQuestionDetailId();
                essayStandardAnswerVO.setQuestionId(questionDetailId);
                essayStandardAnswerVO.setId(-1L);
                essayQuestionService.saveStandardAnswer(essayStandardAnswerVO);
            }
            List<EssayMaterialVO> materialList = essayQuestionFullVO.getMaterialList();
            materialList.forEach(i -> {
                i.setPaperId(mockId);
                i.setId(materialMap.get(i.getId()));
            });
            AdminMaterialListVO adminMaterialListVO = new AdminMaterialListVO();
            adminMaterialListVO.setPaperId(mockId);
            adminMaterialListVO.setQuestionBaseId(adminQuestion.getQuestionBaseId());
            adminMaterialListVO.setUserId(-1);
            adminMaterialListVO.setMaterialList(materialList);
//            essayMaterialService.saveMaterial(adminMaterialListVO,userId);
            essayQuestionService.saveMaterial(adminMaterialListVO);
            //得分规则处理
            AdminQuestionKeyRuleVO adminQuestionKeyRuleVO = essayQuestionFullVO.getAdminQuestionKeyRuleVO();
            if (adminQuestionKeyRuleVO != null && adminQuestionKeyRuleVO.getQuestionDetailId() > 0) {
                adminQuestionKeyRuleVO.setPaperId(mockId);
                adminQuestionKeyRuleVO.setQuestionDetailId(adminQuestion.getQuestionDetailId());
                //处理主题句
                AdminQuestionTopicVO adminQuestionTopicVO = adminQuestionKeyRuleVO.getTopic();
                Function<AdminQuestionTopicVO, AdminQuestionTopicVO> saveTopic = (i -> essayRuleService.addQuestionTopicVO(i, -1));
                InnerQuestionUtil.setSaveTopicVO(saveTopic);
                InnerQuestionUtil.writeMainTopic(adminQuestionTopicVO, adminQuestion);
                //中心论点处理逻辑
                List<AdminQuestionKeyPhraseVO> adminQuestionKeyRuleVOArgumentList = adminQuestionKeyRuleVO.getArgumentList();
                Function<AdminQuestionKeyPhraseVO, AdminQuestionKeyPhraseVO> saveArgument = (i -> {
                    i.setType(AdminKeyPhraseTypeConstant.ARGUMENTATION_CENTRAL_IDEA_TYPE);
                    //将关键句添加到数据库或修改现有数据
                    return essayRuleController.addAnswerKeyPhrase(i, -1);
                });
                if (CollectionUtils.isNotEmpty(adminQuestionKeyRuleVOArgumentList) && !"".equals(adminQuestionKeyRuleVOArgumentList.get(0).getItem())) {
                    for (AdminQuestionKeyPhraseVO adminQuestionKeyPhraseVO : adminQuestionKeyRuleVOArgumentList) {
                        InnerQuestionUtil.writeArgumentOrPhrase(adminQuestionKeyPhraseVO,adminQuestion,saveArgument);
                    }
                }
                //关键句处理逻辑
                List<AdminQuestionKeyPhraseVO> adminQuestionKeyPhraseVOS = adminQuestionKeyRuleVO.getKeyPhraseList();
                Function<AdminQuestionKeyPhraseVO, AdminQuestionKeyPhraseVO> saveKeyPhrase = (i -> {
                    i.setType(AdminKeyPhraseTypeConstant.APPLICATION_KEYPHRASE_TYPE);
                    //将关键句添加到数据库或修改现有数据
                    return essayRuleController.addAnswerKeyPhrase(i, -1);
                });
                if (CollectionUtils.isNotEmpty(adminQuestionKeyPhraseVOS) && !"".equals(adminQuestionKeyPhraseVOS.get(0).getItem())) {
                    for (AdminQuestionKeyPhraseVO adminQuestionKeyPhraseVO : adminQuestionKeyPhraseVOS) {
                        InnerQuestionUtil.writeArgumentOrPhrase(adminQuestionKeyPhraseVO,adminQuestion,saveKeyPhrase);
                    }
                }
                //关键词逻辑处理
                List<AdminQuestionKeyWordVO> adminQuestionKeyWordVOS = adminQuestionKeyRuleVO.getKeyWordList();
                Function<AdminQuestionKeyWordVO, AdminQuestionKeyWordVO> saveKeyWord = (i ->
                    essayRuleController.addAnswerKeyWord(i, -1)
                );
                if (CollectionUtils.isNotEmpty(adminQuestionKeyWordVOS) && !"".equals(adminQuestionKeyWordVOS.get(0).getItem())) {
                    for (AdminQuestionKeyWordVO adminQuestionKeyWordVO : adminQuestionKeyWordVOS) {
                        InnerQuestionUtil.writeKeyWord(adminQuestionKeyWordVO,adminQuestion,saveKeyWord);
                    }
                }
                List<AdminQuestionKeyPhraseWithDescVO> keyPhraseWithDescList = adminQuestionKeyRuleVO.getKeyPhraseWithDescList();
                Function<AdminQuestionKeyPhraseWithDescVO,AdminQuestionKeyPhraseWithDescVO> saveKeyPhraseWithDesc = (i-> essayRuleService.addAnswerKeyPhraseWithDesc(i, -1));
                if(CollectionUtils.isNotEmpty(keyPhraseWithDescList) && StringUtils.isNotBlank(keyPhraseWithDescList.get(0).getItem())){
                    for (AdminQuestionKeyPhraseWithDescVO adminQuestionKeyPhraseWithDescVO : keyPhraseWithDescList) {
                        InnerQuestionUtil.writeKeyPhraseWithDescList(adminQuestionKeyPhraseWithDescVO,adminQuestion,saveKeyPhraseWithDesc);
                    }
                }
                List<AdminQuestionKeyWordWithDescVO> keyWordWithDescList = adminQuestionKeyRuleVO.getKeyWordWithDescList();
                Function<AdminQuestionKeyWordWithDescVO,AdminQuestionKeyWordWithDescVO> saveKeyWordWithDesc = (i-> essayRuleService.addAnswerKeyWordWithDesc(i, -1));
                if(CollectionUtils.isNotEmpty(keyWordWithDescList) && StringUtils.isNotBlank(keyWordWithDescList.get(0).getItem())){
                    for (AdminQuestionKeyWordWithDescVO adminQuestionKeyWordWithDescVO : keyWordWithDescList) {
                        InnerQuestionUtil.writeKeyWordWithDescList(adminQuestionKeyWordWithDescVO,adminQuestion,saveKeyWordWithDesc);
                    }
                }
            }
            //扣分规则处理
            AdminQuestionDeductRuleVO adminQuestionDeductRuleVO = essayQuestionFullVO.getAdminQuestionDeductRuleVO();
            if (adminQuestionDeductRuleVO != null && adminQuestionDeductRuleVO.getQuestionDetailId() > 0) {
                adminQuestionDeductRuleVO.setPaperId(mockId);
                adminQuestionDeductRuleVO.setQuestionDetailId(adminQuestion.getQuestionDetailId());
                List<AdminCommonDeductVO> adminCommonDeductVOS = adminQuestionDeductRuleVO.getDeductRuleList();
                adminCommonDeductVOS.forEach(i -> {
                    i.setId(-1);
                    i.setQuestionDetailId(-1);
                });
                essayRuleController.addDeductRule(adminQuestionDeductRuleVO);
            }

            //格式规则处理
            AdminQuestionFormatVO adminQuestionFormatVO = essayQuestionFullVO.getAdminQuestionFormatVO();
            if (adminQuestionFormatVO != null && adminQuestionFormatVO.getQuestionDetailId() > 0) {
                adminQuestionFormatVO.setId(-1);
                adminQuestionFormatVO.setPaperId(mockId);
                adminQuestionFormatVO.setQuestionDetailId(adminQuestion.getQuestionDetailId());
                if (adminQuestionFormatVO.getTitleInfo() != null && CollectionUtils.isNotEmpty(adminQuestionFormatVO.getTitleInfo().getChildKeyWords())) {
                    adminQuestionFormatVO.getTitleInfo().getChildKeyWords().forEach(i -> {
                        i.setId(-1);
                        i.setQuestionDetailId(adminQuestion.getQuestionDetailId());
                        i.setCorrespondingId(-1);
                        if (CollectionUtils.isNotEmpty(i.getSimilarWordVOList())) {
                            i.getSimilarWordVOList().forEach(j -> {
                                j.setCorrespondingId(-1);
                                j.setQuestionDetailId(adminQuestion.getQuestionDetailId());
                                j.setId(-1);
                            });
                        }
                    });
                }
                if (adminQuestionFormatVO.getInscribeInfo() != null && CollectionUtils.isNotEmpty(adminQuestionFormatVO.getInscribeInfo().getChildKeyWords())) {
                    adminQuestionFormatVO.getInscribeInfo().getChildKeyWords().forEach(i -> {
                        i.setId(-1);
                        i.setQuestionDetailId(adminQuestion.getQuestionDetailId());
                        i.setCorrespondingId(-1);
                        if (CollectionUtils.isNotEmpty(i.getSimilarWordVOList())) {
                            i.getSimilarWordVOList().forEach(j -> {
                                j.setCorrespondingId(-1);
                                j.setQuestionDetailId(adminQuestion.getQuestionDetailId());
                                j.setId(-1);
                            });
                        }
                    });
                }
                if (adminQuestionFormatVO.getAppellationInfo() != null && CollectionUtils.isNotEmpty(adminQuestionFormatVO.getAppellationInfo().getChildKeyWords())) {
                    adminQuestionFormatVO.getAppellationInfo().getChildKeyWords().forEach(i -> {
                        i.setId(-1);
                        i.setQuestionDetailId(adminQuestion.getQuestionDetailId());
                        i.setCorrespondingId(-1);
                        if (CollectionUtils.isNotEmpty(i.getSimilarWordVOList())) {
                            i.getSimilarWordVOList().forEach(j -> {
                                j.setCorrespondingId(-1);
                                j.setQuestionDetailId(adminQuestion.getQuestionDetailId());
                                j.setId(-1);
                            });
                        }
                    });
                }
                essayRuleController.addAnswerFormat(adminQuestionFormatVO);
            }

        }
        return essayMockExam;
    }

    @PostMapping(value = "convert/{mockId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object convertMock2Paper(@PathVariable long mockId){
        EssayMockExam one = essayMockExamRepository.findOne(mockId);
        one.setStatus(-1);
        essayMockExamRepository.save(one);
        EssayPaperBase paperBase = essayPaperBaseRepository.findOne(mockId);
        paperBase.setType(AdminPaperConstant.TRUE_PAPER);
        essayPaperBaseRepository.save(paperBase);
        return "成功";
    }

    @LogPrint
    @GetMapping(value = "question/rule", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public AdminQuestionKeyRuleVO getRule(@RequestParam long questionId) throws BizException {
        AdminQuestionKeyRuleVO adminQuestionKeyRuleVO = essayRuleController.findKeyPhraseAndKeyWord(questionId, null, null);
        return adminQuestionKeyRuleVO;
    }


    @LogPrint
    @PostMapping(value = "question/rule", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public AdminQuestionKeyRuleVO saveRule(@RequestBody AdminQuestionKeyRuleVO adminQuestionKeyRuleVO) throws BizException {
        //关键词逻辑处理
        List<AdminQuestionKeyWordVO> adminQuestionKeyWordVOS = adminQuestionKeyRuleVO.getKeyWordList();
        if (CollectionUtils.isNotEmpty(adminQuestionKeyWordVOS) && !"".equals(adminQuestionKeyWordVOS.get(0).getItem())) {
            for (AdminQuestionKeyWordVO adminQuestionKeyWordVO : adminQuestionKeyWordVOS) {
                adminQuestionKeyWordVO.setQuestionDetailId(adminQuestionKeyRuleVO.getQuestionDetailId());
                adminQuestionKeyWordVO.setId(-1);
                adminQuestionKeyWordVO.setCorrespondingId(-1);
                if (CollectionUtils.isNotEmpty(adminQuestionKeyWordVO.getSimilarWordVOList())) {
                    adminQuestionKeyWordVO.getSimilarWordVOList().forEach(i -> {
                        i.setCorrespondingId(-1);
                        i.setQuestionDetailId(-1);
                        i.setId(-1);
                    });
                }
            }
        }
        essayRuleController.addKeyPhraseAndKeyWord(adminQuestionKeyRuleVO);
        return null;
    }

}
