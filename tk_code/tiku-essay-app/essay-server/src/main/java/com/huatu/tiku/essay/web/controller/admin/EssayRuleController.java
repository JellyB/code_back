package com.huatu.tiku.essay.web.controller.admin;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.huatu.common.exception.BizException;
import com.huatu.common.spring.web.MediaType;
import com.huatu.tiku.essay.constant.cache.RedisKeyConstant;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.status.AdminKeyPhraseTypeConstant;
import com.huatu.tiku.essay.constant.status.EssayAnswerKeyWordConstant;
import com.huatu.tiku.essay.service.EssayPaperService;
import com.huatu.tiku.essay.service.EssayRuleService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.admin.EssayConvertUtil;
import com.huatu.tiku.essay.vo.admin.*;
import com.huatu.tiku.essay.vo.resp.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by huangqp on 2017\12\6 0006.
 */
@RestController
@Slf4j
@RequestMapping("/end/rule")
public class EssayRuleController {
    @Autowired
    EssayRuleService essayRuleService;
    @Autowired
    EssayPaperService essayPaperService;

    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 查询关键词句规则
     *
     * @param questionDetailId
     * @return
     */
    @LogPrint
    @GetMapping(value = "answerKeyPhraseAndKeyWord", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public AdminQuestionKeyRuleVO findKeyPhraseAndKeyWord(@RequestParam long questionDetailId, @RequestHeader(name = "admin", defaultValue = "") String admin, String uname) {

        //查找关键词信息（试题直属的）
        List<AdminQuestionKeyWordVO> keyWordList = findAnswerKeyWordByQuestion(questionDetailId);
        //查询关键句
        List<AdminQuestionKeyPhraseVO> keyPhraseList = findAnswerKeyPhraseByQuestion(questionDetailId,
                AdminKeyPhraseTypeConstant.APPLICATION_KEYPHRASE_TYPE);
        //查询议论文
        List<AdminQuestionKeyPhraseVO> argumentList = findAnswerKeyPhraseByQuestion(questionDetailId,
                AdminKeyPhraseTypeConstant.ARGUMENTATION_CENTRAL_IDEA_TYPE);
        //查询议论文主题
        AdminQuestionTopicVO topic = findAnswerTopicByQuestion(questionDetailId);

        //查找有描述的关键词
        List<AdminQuestionKeyWordWithDescVO> keyWordWithDescList = essayRuleService.findAnswerKeyWordByQuestionWithDesc(questionDetailId);
        //查找有描述的关键句
        List<AdminQuestionKeyPhraseWithDescVO> keyPhraseWithDescList = essayRuleService.findAnswerKeyPhraseByQuestionWithDesc(questionDetailId);
        //组装关键词句、主题和中心论点
        AdminQuestionKeyRuleVO adminQuestionKeyRuleVO = new AdminQuestionKeyRuleVO();
        keyPhraseList.sort((a, b) -> (int) (a.getId() - b.getId()));
        keyWordList.sort((a, b) -> (int) (a.getId() - b.getId()));
        argumentList.sort((a, b) -> (int) (a.getId() - b.getId()));
        adminQuestionKeyRuleVO.setKeyPhraseList(keyPhraseList);
        adminQuestionKeyRuleVO.setKeyWordList(keyWordList);
        adminQuestionKeyRuleVO.setArgumentList(argumentList);
        adminQuestionKeyRuleVO.setTopic(topic);
        adminQuestionKeyRuleVO.setQuestionDetailId(questionDetailId);
        adminQuestionKeyRuleVO.setKeyWordWithDescList(keyWordWithDescList);
        adminQuestionKeyRuleVO.setKeyPhraseWithDescList(keyPhraseWithDescList);
        return adminQuestionKeyRuleVO;
    }

    private AdminQuestionTopicVO findAnswerTopicByQuestion(long questionDetailId) {
        return essayRuleService.findAnswerTopicByQuestion(questionDetailId);
    }

    /**
     * 维护关键词、关键句、中心论点和主题的规则
     *
     * @param essayQuestionKeyRule
     * @return
     */
    @LogPrint
    @PostMapping(value = "answerKeyPhraseAndKeyWord", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public AdminQuestionKeyRuleVO addKeyPhraseAndKeyWord(@RequestBody AdminQuestionKeyRuleVO essayQuestionKeyRule) {
        int uid = -1;
        //查询是否为空
        if (essayQuestionKeyRule == null) {
            throw new BizException(EssayErrors.ESSAY_PARAM_ILLEGAL);
        }
        //去掉句子收尾的空格
        essayQuestionKeyRule.myTrim();
        //判断试题id是否有效，有效则先将所有表数据置为删除状态
        if (essayQuestionKeyRule.getQuestionDetailId() > 0) {

            //修改关键词，关键句不处理格式数据
            AdminQuestionFormatVO answerFormatByQuestion = findAnswerFormatByQuestion(essayQuestionKeyRule.getQuestionDetailId());
            essayPaperService.delQuestionRuleByDetailId(essayQuestionKeyRule.getQuestionDetailId(), false);
            if (null != answerFormatByQuestion && null != answerFormatByQuestion.getType()) {
                addAnswerFormat(answerFormatByQuestion);
            }
        } else {
            throw new BizException(EssayErrors.NO_EXISTED_QUESTION_DETAIL);
        }
        //返回对象初始化
        AdminQuestionKeyRuleVO adminQuestionKeyRuleVO = new AdminQuestionKeyRuleVO();
        long questionId = essayQuestionKeyRule.getQuestionDetailId();
        //修改关键句的内容
        List<AdminQuestionKeyPhraseVO> adminQuestionKeyPhraseVOS = essayQuestionKeyRule.getKeyPhraseList();
        if (CollectionUtils.isNotEmpty(adminQuestionKeyPhraseVOS)) {
            //所有关键句的处理结果
            List<AdminQuestionKeyPhraseVO> adminQuestionKeyPhraseVOList = Lists.newLinkedList();
            for (AdminQuestionKeyPhraseVO adminQuestionKeyPhraseVO : adminQuestionKeyPhraseVOS) {
                adminQuestionKeyPhraseVO.setQuestionDetailId(questionId);
                adminQuestionKeyPhraseVO.setType(AdminKeyPhraseTypeConstant.APPLICATION_KEYPHRASE_TYPE);
                //将关键句添加到数据库或修改现有数据
                adminQuestionKeyPhraseVOList.add(addAnswerKeyPhrase(adminQuestionKeyPhraseVO, uid));
            }
            adminQuestionKeyRuleVO.setKeyPhraseList(adminQuestionKeyPhraseVOList);
        }
        //修改关键句中心论点
        List<AdminQuestionKeyPhraseVO> adminQuestionArgumentVOS = essayQuestionKeyRule.getArgumentList();
        if (CollectionUtils.isNotEmpty(adminQuestionArgumentVOS)) {
            List<AdminQuestionKeyPhraseVO> adminQuestionKeyPhraseVOList = Lists.newLinkedList();
            for (AdminQuestionKeyPhraseVO adminQuestionKeyPhraseVO : adminQuestionArgumentVOS) {
                adminQuestionKeyPhraseVO.setQuestionDetailId(questionId);
                adminQuestionKeyPhraseVO.setType(AdminKeyPhraseTypeConstant.ARGUMENTATION_CENTRAL_IDEA_TYPE);
                //将关键句添加到数据库或修改现有数据
                adminQuestionKeyPhraseVOList.add(addAnswerKeyPhrase(adminQuestionKeyPhraseVO, uid));
            }
            adminQuestionKeyRuleVO.setArgumentList(adminQuestionKeyPhraseVOList);
        }
        //修改议论文主题
        AdminQuestionTopicVO adminQuestionTopicVO = essayQuestionKeyRule.getTopic();
        if (adminQuestionTopicVO != null && adminQuestionTopicVO.getItem() != null && !"".equals(adminQuestionTopicVO.getItem())) {
            //主要逻辑
            adminQuestionTopicVO.setQuestionDetailId(questionId);
            AdminQuestionTopicVO newTopic = addQuestionTopicVO(adminQuestionTopicVO, uid);
            adminQuestionKeyRuleVO.setTopic(newTopic);
        }
        //修改关键词的内容
        List<AdminQuestionKeyWordVO> adminQuestionKeyWordVOS = essayQuestionKeyRule.getKeyWordList();
        if (CollectionUtils.isNotEmpty(adminQuestionKeyWordVOS)) {
            List<AdminQuestionKeyWordVO> adminQuestionKeyWordVOList = Lists.newLinkedList();
            for (AdminQuestionKeyWordVO adminQuestionKeyWordVO : adminQuestionKeyWordVOS) {
                adminQuestionKeyWordVO.setQuestionDetailId(questionId);
                //主要逻辑
                adminQuestionKeyWordVOList.add(addAnswerKeyWord(adminQuestionKeyWordVO, uid));
            }
            adminQuestionKeyRuleVO.setKeyWordList(adminQuestionKeyWordVOList);
        }


        //修改关键词(有描述)
        List<AdminQuestionKeyWordWithDescVO> keyWordWithDescList = essayQuestionKeyRule.getKeyWordWithDescList();
        if (CollectionUtils.isNotEmpty(keyWordWithDescList)) {
            for (AdminQuestionKeyWordWithDescVO adminQuestionKeyWordWithDescVO : keyWordWithDescList) {
                adminQuestionKeyWordWithDescVO.setQuestionDetailId(questionId);
                //主要逻辑
                essayRuleService.addAnswerKeyWordWithDesc(adminQuestionKeyWordWithDescVO, uid);
            }
        }
        //修改关键句(有描述)
        List<AdminQuestionKeyPhraseWithDescVO> keyPhraseWithDescList = essayQuestionKeyRule.getKeyPhraseWithDescList();
        if (CollectionUtils.isNotEmpty(keyPhraseWithDescList)) {
            //所有关键句的处理结果
            for (AdminQuestionKeyPhraseWithDescVO adminQuestionKeyPhraseWithDescVO : keyPhraseWithDescList) {
                adminQuestionKeyPhraseWithDescVO.setQuestionDetailId(questionId);
                //主要逻辑
                essayRuleService.addAnswerKeyPhraseWithDesc(adminQuestionKeyPhraseWithDescVO, uid);
            }
        }
        adminQuestionKeyRuleVO.setQuestionDetailId(questionId);
        //查找有描述的关键词
        keyWordWithDescList = essayRuleService.findAnswerKeyWordByQuestionWithDesc(questionId);
        //查找有描述的关键句
        keyPhraseWithDescList = essayRuleService.findAnswerKeyPhraseByQuestionWithDesc(questionId);
        adminQuestionKeyRuleVO.setKeyPhraseWithDescList(keyPhraseWithDescList);
        adminQuestionKeyRuleVO.setKeyWordWithDescList(keyWordWithDescList);
        essayPaperService.resetPaperStatus(essayQuestionKeyRule.getPaperId());
        return adminQuestionKeyRuleVO;
    }


    private AdminQuestionTopicVO addQuestionTopicVO(AdminQuestionTopicVO adminQuestionTopicVO, int uid) {
        if (StringUtils.isBlank(adminQuestionTopicVO.getItem())) {
            log.warn("主题句内容为空，为无效主题句不做处理，{}", JSON.toJSON(adminQuestionTopicVO));
            return new AdminQuestionTopicVO();
        }
        return essayRuleService.addQuestionTopicVO(adminQuestionTopicVO, uid);
    }

//    private void delAllKeyRuleByQuestion(long questionDetailId, int uid) {
//        essayRuleService.delAllKeyRuleByQuestion(questionDetailId, uid);
//    }

    /**
     * 查询扣分规则
     *
     * @param questionDetailId
     * @return
     */
    @LogPrint
    @GetMapping(value = "answerDeductRule", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public AdminQuestionDeductRuleVO findDeductRule(@RequestParam long questionDetailId, @RequestHeader(name = "admin", defaultValue = "") String admin, String uname) {
        if (StringUtils.isEmpty(admin)) {
            admin = uname;
        }
        if (StringUtils.isNoneBlank(admin)) {
            String userKeyByJY = RedisKeyConstant.getJYUserKey();
            Boolean isJYflag = redisTemplate.opsForSet().isMember(userKeyByJY, admin);
            if (isJYflag) {
                //如果是教育用户则不显示以下内容
                return AdminQuestionDeductRuleVO.builder().build();
            }
        }
        EssayQuestionDeductRuleVO resultVO = EssayQuestionDeductRuleVO.builder()
                .commonRuleList(findCommonAnswerRule(questionDetailId))
                .specialStripList(findAnswerRuleSpecialStripByQuestion(questionDetailId))
                .questionDetailId(questionDetailId).build();
        return EssayConvertUtil.convertDeductVO2Pre(resultVO);
    }

    /**
     * 维护所有扣分项的规则
     *
     * @param essayQuestionDeductRule
     * @return
     */
    @LogPrint
    @PostMapping(value = "answerDeductRule", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public AdminQuestionDeductRuleVO addDeductRule(@RequestBody AdminQuestionDeductRuleVO essayQuestionDeductRule) {
        int uid = -1;
        if (essayQuestionDeductRule == null) {
            throw new BizException(EssayErrors.ESSAY_PARAM_ILLEGAL);
        }
        //去掉句子首尾的空格
        essayQuestionDeductRule.myTrim();
        if (essayQuestionDeductRule.getQuestionDetailId() > 0) {
            delAllDeductRuleByQuestion(essayQuestionDeductRule.getQuestionDetailId(), uid);
        } else {
            throw new BizException(EssayErrors.NO_EXISTED_QUESTION_DETAIL);
        }
        if (CollectionUtils.isEmpty(essayQuestionDeductRule.getDeductRuleList())) {
            throw new BizException(EssayErrors.NO_EXISTED_REAL_DATA);
        }
        //将数据装换为VO模型
        EssayQuestionDeductRuleVO essayQuestionDeductRuleVO = EssayConvertUtil.convertDeductPre2VO(essayQuestionDeductRule);
        EssayQuestionDeductRuleVO resultVO = new EssayQuestionDeductRuleVO();
        //处理特殊分条规则
        List<EssayStandardAnswerRuleSpecialStripVO> specialStripVOS = Lists.newLinkedList();
        for (EssayStandardAnswerRuleSpecialStripVO specialStripVO : essayQuestionDeductRuleVO.getSpecialStripList()) {
            specialStripVOS.add(addAnswerRuleSpecialStrip(specialStripVO, uid));
        }
        resultVO.setSpecialStripList(specialStripVOS);
        List<EssayStandardAnswerRuleVO> commonRules = Lists.newLinkedList();
        for (EssayStandardAnswerRuleVO commonRule : essayQuestionDeductRuleVO.getCommonRuleList()) {
            commonRules.add(addCommonAnswerRule(commonRule, uid));
        }
        resultVO.setCommonRuleList(commonRules);
        resultVO.setQuestionDetailId(essayQuestionDeductRuleVO.getQuestionDetailId());
        essayPaperService.resetPaperStatus(essayQuestionDeductRule.getPaperId());
        return EssayConvertUtil.convertDeductVO2Pre(resultVO);
    }

    private void delAllDeductRuleByQuestion(long questionDetailId, int uid) {
        essayRuleService.delAllDeductRuleByQuestion(questionDetailId, uid);
    }

    /**
     * 查询格式规则
     *
     * @param questionDetailId
     * @return
     */
    @LogPrint
    @GetMapping(value = "answerFormat", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public AdminQuestionFormatVO findAnswerFormatByQuestion(@RequestParam long questionDetailId) {
        List<EssayStandardAnswerFormatVO> resultVOList = essayRuleService.findAnswerFormatsByQuestion(questionDetailId);
        if (CollectionUtils.isEmpty(resultVOList)) {
            return new AdminQuestionFormatVO();
        }
        EssayStandardAnswerFormatVO formatVO = resultVOList.get(0);
        return EssayConvertUtil.convertFormatVO2Pre(formatVO);
    }

    /**
     * 维护格式规则
     *
     * @param essayStandardAnswerFormat
     * @return
     */
    @LogPrint
    @PostMapping(value = "answerFormat", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public AdminQuestionFormatVO addAnswerFormat(@RequestBody AdminQuestionFormatVO essayStandardAnswerFormat) {
        int uid = -1;
        if (essayStandardAnswerFormat == null) {
            log.error("未接受到参数");
            throw new BizException(EssayErrors.ESSAY_PARAM_ILLEGAL);
        }
        //去掉首尾的空格
        essayStandardAnswerFormat.myTrim();
        //转换格式
        EssayStandardAnswerFormatVO essayStandardAnswerFormatVO = EssayConvertUtil.convertFormatPre2VO(essayStandardAnswerFormat);
        if (essayStandardAnswerFormatVO.getQuestionDetailId() > 0) {
            delEssayAnswerFormatByQuestion(essayStandardAnswerFormat.getQuestionDetailId(), uid);
        } else {
            throw new BizException(EssayErrors.NO_EXISTED_QUESTION_DETAIL);
        }
        EssayStandardAnswerFormatVO resultVO = essayRuleService.addAnswerFormat(essayStandardAnswerFormatVO, uid);
        essayPaperService.resetPaperStatus(essayStandardAnswerFormat.getPaperId());
        return EssayConvertUtil.convertFormatVO2Pre(resultVO);
    }

    private void delEssayAnswerFormatByQuestion(long questionDetailId, int uid) {
        essayRuleService.delEssayAnswerFormatByQuestion(questionDetailId, uid);
    }

    /**
     * 修改格式规则
     *
     * @param essayStandardAnswerFormat
     * @return
     */
    @LogPrint
    @PutMapping(value = "answerFormat", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public EssayStandardAnswerFormatVO updateAnswerFormat(@RequestBody EssayStandardAnswerFormatVO essayStandardAnswerFormat) {
        int uid = -1;
        return essayRuleService.updateAnswerFormat(essayStandardAnswerFormat, uid);
    }

    @LogPrint
    @GetMapping(value = "answerKeyPhrase", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<AdminQuestionKeyPhraseVO> findAnswerKeyPhraseByQuestion(@RequestParam long questionDetailId, @RequestParam int type) {
        //查询
        List<EssayStandardAnswerKeyPhraseVO> essayStandardAnswerKeyPhraseVOS = essayRuleService.findAnswerKeyPhraseByQuestion(questionDetailId, type, 0);
        //格式转换
        return EssayConvertUtil.convertBatchKeyPhraseVO2Pre(essayStandardAnswerKeyPhraseVOS);
    }

    /**
     * 处理单个关键句
     *
     * @param essayStandardAnswerKeyPhrase
     * @param uid
     * @return
     */
    @LogPrint
    @PostMapping(value = "answerKeyPhrase", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public AdminQuestionKeyPhraseVO addAnswerKeyPhrase(@RequestBody AdminQuestionKeyPhraseVO essayStandardAnswerKeyPhrase, @RequestParam int uid) {
        if (StringUtils.isBlank(essayStandardAnswerKeyPhrase.getItem())) {
            log.warn("关键句内容为空，为无效关键句不做处理，{}", JSON.toJSON(essayStandardAnswerKeyPhrase));
            return new AdminQuestionKeyPhraseVO();
        }
        //处理vo对象
        EssayStandardAnswerKeyPhraseVO target = EssayConvertUtil.convertKeyPhrasePre2VO(essayStandardAnswerKeyPhrase);

        EssayStandardAnswerKeyPhraseVO resultVO = essayRuleService.addAnswerKeyPhrase(target, uid);
        return EssayConvertUtil.convertKeyPhraseVO2Pre(resultVO);
    }

    @LogPrint
    @GetMapping(value = "answerKeyWord", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<AdminQuestionKeyWordVO> findAnswerKeyWordByQuestion(@RequestParam long questionDetailId) {
        List<EssayStandardAnswerKeyWordVO> essayStandardAnswerKeyWordVOS = essayRuleService.findAnswerKeyWordByQuestion(questionDetailId, EssayAnswerKeyWordConstant.QUESTION_PARENT_KEYWORD_WITHOUT_DESC, 0L);
        return EssayConvertUtil.convertBatchKeyWordVO2Pre(essayStandardAnswerKeyWordVOS);
    }

    @LogPrint
    @PostMapping(value = "answerKeyWord", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public AdminQuestionKeyWordVO addAnswerKeyWord(@RequestBody AdminQuestionKeyWordVO essayStandardAnswerKeyWord, @RequestParam int uid) {
        if (StringUtils.isBlank(essayStandardAnswerKeyWord.getItem())) {
            log.warn("关键词内容为空，为无效关键词不做处理，{}", JSON.toJSON(essayStandardAnswerKeyWord));
            return new AdminQuestionKeyWordVO();
        }
        EssayStandardAnswerKeyWordVO essayStandardAnswerKeyWordVO = EssayConvertUtil.convertKeyWordPre2VO(essayStandardAnswerKeyWord);
        EssayStandardAnswerKeyWordVO resultVO = essayRuleService.addAnswerKeyWord(essayStandardAnswerKeyWordVO, uid, EssayAnswerKeyWordConstant.QUESTION_PARENT_KEYWORD_WITHOUT_DESC);
        return EssayConvertUtil.convertKeyWordVO2Pre(resultVO);
    }

    @LogPrint
    @GetMapping(value = "answerRuleSpecialStrip", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<EssayStandardAnswerRuleSpecialStripVO> findAnswerRuleSpecialStripByQuestion(@RequestParam long questionDetailId) {
        return essayRuleService.findAnswerRuleSpecialStripByQuestion(questionDetailId);
    }

    @LogPrint
    @PostMapping(value = "answerRuleSpecialStrip", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public EssayStandardAnswerRuleSpecialStripVO addAnswerRuleSpecialStrip(@RequestBody EssayStandardAnswerRuleSpecialStripVO essayStandardAnswerRuleSpecialStrip, @RequestParam int uid) {
        return essayRuleService.addAnswerRuleSpecialStrip(essayStandardAnswerRuleSpecialStrip, uid);
    }

    @LogPrint
    @GetMapping(value = "answerRuleStripSegmental", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<EssayStandardAnswerRuleStripSegmentalVO> findAnswerRuleStripSegmentalByQuestion(@RequestParam long questionDetailId) {
        return essayRuleService.findAnswerRuleStripSegmentalByQuestion(questionDetailId);
    }

    @LogPrint
    @PostMapping(value = "answerRuleStripSegmental", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public EssayStandardAnswerRuleStripSegmentalVO addAnswerRuleStripSegmental(@RequestBody EssayStandardAnswerRuleStripSegmentalVO essayStandardAnswerRuleStripSegmental) {
        int uid = -1;
        return essayRuleService.addAnswerRuleStripSegmental(essayStandardAnswerRuleStripSegmental, uid);
    }

    @LogPrint
    @GetMapping(value = "answerRuleWordNum", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<EssayStandardAnswerRuleWordNumVO> findAnswerRuleWordNumByQuestion(@RequestParam long questionDetailId) {
        return essayRuleService.findAnswerRuleWordNumByQuestion(questionDetailId);
    }

    @LogPrint
    @PostMapping(value = "answerRuleWordNum", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public EssayStandardAnswerRuleWordNumVO addAnswerRuleWordNum(@RequestBody EssayStandardAnswerRuleWordNumVO essayStandardAnswerRuleWordNum) {
        int uid = -1;
        return essayRuleService.addAnswerRuleWordNum(essayStandardAnswerRuleWordNum, uid);
    }

    @LogPrint
    @GetMapping(value = "answerRule", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<EssayStandardAnswerRuleVO> findCommonAnswerRule(@RequestParam long questionDetailId) {
        return essayRuleService.findCommonAnswerRule(questionDetailId);
    }

    @LogPrint
    @PostMapping(value = "answerRule", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public EssayStandardAnswerRuleVO addCommonAnswerRule(@RequestBody EssayStandardAnswerRuleVO essayStandardAnswerRuleVO, @RequestParam int uid) {
        return essayRuleService.addCommonAnswerRule(essayStandardAnswerRuleVO, uid);
    }


    /**
     * 应用文   删除分段规则处理(暂时不用)
     *
     * @return
     */
    @LogPrint
    @PostMapping(value = "redis/refresh", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseVO findThemeRule() {

        return essayRuleService.refresh();
    }

}
