package com.huatu.tiku.essay.util.admin;

import com.google.common.collect.Lists;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.status.EssayAnswerFormatTypeConstant;
import com.huatu.tiku.essay.constant.status.EssayAnswerKeyWordConstant;
import com.huatu.tiku.essay.vo.admin.*;
import com.huatu.tiku.essay.vo.resp.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by huangqp on 2017\12\12 0012.
 */
@Slf4j
public class EssayConvertUtil {
    public static EssayStandardAnswerKeyWordVO convertKeyWordPre2VO(AdminQuestionKeyWordVO source) {
        return convertKeyWordPre2VO(source, EssayAnswerKeyWordConstant.QUESTION_PARENT_KEYWORD_WITHOUT_DESC);
    }

    /**
     * 转换前端的传送的参数为VO参数
     *
     * @param source
     * @return
     */
    private static EssayStandardAnswerKeyWordVO convertKeyWordPre2VO(AdminQuestionKeyWordVO source, int type) {
        long parentId = source.getId();         //补充到近义词中
        EssayStandardAnswerKeyWordVO essayStandardAnswerKeyWordVO = new EssayStandardAnswerKeyWordVO();
        BeanUtils.copyProperties(source, essayStandardAnswerKeyWordVO);
        List<String> words = source.getSplitWords();
        List<String> splitWords = Lists.newArrayList();
        int num = 0;
        if (CollectionUtils.isNotEmpty(source.getSplitWords())) {
            for (String word : words) {
                if (StringUtils.isNotBlank(word)) {
                    num++;
                    splitWords.add(word);
                }
            }
        }
        essayStandardAnswerKeyWordVO.setSplitNum(num);
        essayStandardAnswerKeyWordVO.setType(type);
        for (int i = 0; i < num; i++) {
            switch (i) {
                case 0: {
                    essayStandardAnswerKeyWordVO.setFirstSplitWord(splitWords.get(i));
                    break;
                }
                case 1: {
                    essayStandardAnswerKeyWordVO.setSecondSplitWord(splitWords.get(i));
                    break;
                }
                case 2: {
                    essayStandardAnswerKeyWordVO.setThirdSplitWord(splitWords.get(i));
                    break;
                }
                case 3: {
                    essayStandardAnswerKeyWordVO.setFourthSplitWord(splitWords.get(i));
                    break;
                }
                case 4: {
                    essayStandardAnswerKeyWordVO.setFifthSplitWord(splitWords.get(i));
                    break;
                }
            }
        }
        List<AdminQuestionKeyWordVO> similarList = source.getSimilarWordVOList();
        List<EssayStandardAnswerKeyWordVO> childList = Lists.newLinkedList();
        if (CollectionUtils.isNotEmpty(similarList)) {
            for (AdminQuestionKeyWordVO childSource : similarList) {
                childSource.setCorrespondingId(parentId);
                childSource.setQuestionDetailId(source.getQuestionDetailId());
                childSource.setType(EssayAnswerKeyWordConstant.QUESTION_KEYWORD_CHILD_KEYWORD);
                childList.add(convertKeyWordPre2VO(childSource));
            }
        }
        essayStandardAnswerKeyWordVO.setSimilarWordVOList(childList);
        return essayStandardAnswerKeyWordVO;
    }

    /**
     * 转换VO对象为前端接受对象
     *
     * @param source
     * @return
     */
    public static AdminQuestionKeyWordVO convertKeyWordVO2Pre(EssayStandardAnswerKeyWordVO source) {
        AdminQuestionKeyWordVO adminQuestionKeyWordVO = new AdminQuestionKeyWordVO();
        BeanUtils.copyProperties(source, adminQuestionKeyWordVO);
        List<String> splitkeyWords = Lists.newLinkedList();
        int size = source.getSplitNum();
        for (int i = 0; i < size; i++) {
            switch (i) {
                case 0: {
                    splitkeyWords.add(i, source.getFirstSplitWord());
                    break;
                }
                case 1: {
                    splitkeyWords.add(i, source.getSecondSplitWord());
                    break;
                }
                case 2: {
                    splitkeyWords.add(i, source.getThirdSplitWord());
                    break;
                }
                case 3: {
                    splitkeyWords.add(i, source.getFourthSplitWord());
                    break;
                }
                case 4: {
                    splitkeyWords.add(i, source.getFifthSplitWord());
                    break;
                }
            }
        }
        adminQuestionKeyWordVO.setSplitWords(splitkeyWords);
        List<EssayStandardAnswerKeyWordVO> keyWordVOS = source.getSimilarWordVOList();
        if (CollectionUtils.isNotEmpty(keyWordVOS)) {
            List<AdminQuestionKeyWordVO> adminQuestionKeyWordVOS = Lists.newLinkedList();
            for (EssayStandardAnswerKeyWordVO keyWordVO : keyWordVOS) {
                adminQuestionKeyWordVOS.add(convertKeyWordVO2Pre(keyWordVO));
            }
            adminQuestionKeyWordVO.setSimilarWordVOList(adminQuestionKeyWordVOS);
		} else {
			adminQuestionKeyWordVO.setSimilarWordVOList(Lists.newLinkedList());
		}
        return adminQuestionKeyWordVO;
    }

    /**
     * 将关键句从前端对象转为VO对象
     *
     * @param source
     * @return
     */
    public static EssayStandardAnswerKeyPhraseVO convertKeyPhrasePre2VO(AdminQuestionKeyPhraseVO source) {
        EssayStandardAnswerKeyPhraseVO essayStandardAnswerKeyPhraseVO = new EssayStandardAnswerKeyPhraseVO();
        long parentId = source.getId();         //补充到近义词中
        BeanUtils.copyProperties(source, essayStandardAnswerKeyPhraseVO);
        List<AdminQuestionKeyWordVO> keyWordVOList = source.getKeyWordVOList();
        List<EssayStandardAnswerKeyWordVO> keyWordVOS = Lists.newLinkedList();
        if (CollectionUtils.isNotEmpty(keyWordVOList)) {
            for (AdminQuestionKeyWordVO adminQuestionKeyWordVO : keyWordVOList) {
                adminQuestionKeyWordVO.setCorrespondingId(parentId);
                adminQuestionKeyWordVO.setQuestionDetailId(source.getQuestionDetailId());
                keyWordVOS.add(convertKeyWordPre2VO(adminQuestionKeyWordVO, EssayAnswerKeyWordConstant.QUESTION_KEYPHRASE_CHILD_KEYWORD));
            }
        }
        //处理近似句
        List<AdminQuestionKeyPhraseVO> sourceSimilarPhraseList = source.getSimilarPhraseList();
        if (CollectionUtils.isNotEmpty(sourceSimilarPhraseList)) {
            LinkedList<EssayStandardAnswerKeyPhraseVO> targetSimilarPhraseList = new LinkedList<>();
            for (AdminQuestionKeyPhraseVO sourceSimilarPhrase : sourceSimilarPhraseList) {
                EssayStandardAnswerKeyPhraseVO targetSimilarPhrase = convertKeyPhrasePre2VO(sourceSimilarPhrase);
                targetSimilarPhrase.setQuestionDetailId(source.getQuestionDetailId());
                targetSimilarPhraseList.add(targetSimilarPhrase);
            }
            essayStandardAnswerKeyPhraseVO.setSimilarPhraseList(targetSimilarPhraseList);
        }

        essayStandardAnswerKeyPhraseVO.setKeyWordVOList(keyWordVOS);
        return essayStandardAnswerKeyPhraseVO;
    }

    public static AdminQuestionKeyPhraseVO convertKeyPhraseVO2Pre(EssayStandardAnswerKeyPhraseVO source) {
        AdminQuestionKeyPhraseVO adminQuestionKeyPhraseVO = new AdminQuestionKeyPhraseVO();
        BeanUtils.copyProperties(source, adminQuestionKeyPhraseVO);
        List<EssayStandardAnswerKeyWordVO> keyWordVOS = source.getKeyWordVOList();
        if (CollectionUtils.isNotEmpty(keyWordVOS)) {
            List<AdminQuestionKeyWordVO> keyWordVOList = Lists.newLinkedList();
            for (EssayStandardAnswerKeyWordVO essayStandardAnswerKeyWordVO : keyWordVOS) {
                keyWordVOList.add(convertKeyWordVO2Pre(essayStandardAnswerKeyWordVO));
            }
            adminQuestionKeyPhraseVO.setKeyWordVOList(keyWordVOList);
        }
        List<EssayStandardAnswerKeyPhraseVO> similarPhraseList = source.getSimilarPhraseList();
        if (CollectionUtils.isNotEmpty(similarPhraseList)) {
            LinkedList<AdminQuestionKeyPhraseVO> similarPhraseVOList = new LinkedList<>();
            for (EssayStandardAnswerKeyPhraseVO similarPhrase : similarPhraseList) {
                AdminQuestionKeyPhraseVO similarPhraseVO = convertKeyPhraseVO2Pre(similarPhrase);
                similarPhraseVOList.add(similarPhraseVO);
            }
            adminQuestionKeyPhraseVO.setSimilarPhraseList(similarPhraseVOList);
        }

        return adminQuestionKeyPhraseVO;
    }

    public static EssayStandardAnswerFormatVO convertFormatPre2VO(AdminQuestionFormatVO source) {
        EssayStandardAnswerFormatVO resultVO = new EssayStandardAnswerFormatVO();
        BeanUtils.copyProperties(source, resultVO);
        long questionId = source.getQuestionDetailId();
        resultVO.setQuestionDetailId(questionId);
        if (source.getScore() != null) {
            resultVO.setAppellationScore(source.getScore());
            resultVO.setInscribeScore(source.getScore());
            resultVO.setTitleScore(source.getScore());
            return resultVO;
        }
        List<EssayStandardAnswerKeyWordVO> keyWordList = Lists.newLinkedList();
        AnswerSubFormatVO appellationInfo = source.getAppellationInfo();
        if (appellationInfo != null) {
            resultVO.setAppellationScore(appellationInfo.getScore());
            for (AdminQuestionKeyWordVO adminQuestionKeyWordVO : appellationInfo.getChildKeyWords()) {
                adminQuestionKeyWordVO.setQuestionDetailId(questionId);
                keyWordList.add(convertKeyWordPre2VO(adminQuestionKeyWordVO, EssayAnswerKeyWordConstant.QUESTION_FROM_APPELLATION_CHILD_KEYWORD));
            }
        }
        AnswerSubFormatVO titleInfo = source.getTitleInfo();
        if (titleInfo != null) {
            resultVO.setTitleScore(titleInfo.getScore());
            for (AdminQuestionKeyWordVO adminQuestionKeyWordVO : titleInfo.getChildKeyWords()) {
                adminQuestionKeyWordVO.setQuestionDetailId(questionId);
                keyWordList.add(convertKeyWordPre2VO(adminQuestionKeyWordVO, EssayAnswerKeyWordConstant.QUESTION_FROM_TITLE_CHILD_KEYWORD));
            }
        }
        AnswerSubFormatVO inscribeInfo = source.getInscribeInfo();
        if (inscribeInfo != null) {
            resultVO.setInscribeScore(inscribeInfo.getScore());
            for (AdminQuestionKeyWordVO adminQuestionKeyWordVO : inscribeInfo.getChildKeyWords()) {
                adminQuestionKeyWordVO.setQuestionDetailId(questionId);
                keyWordList.add(convertKeyWordPre2VO(adminQuestionKeyWordVO, EssayAnswerKeyWordConstant.QUESTION_FROM_INSCRIBE_CHILD_KEYWORD));
            }
        }
        resultVO.setChildKeyWords(keyWordList);
        return resultVO;
    }

    public static AdminQuestionFormatVO convertFormatVO2Pre(EssayStandardAnswerFormatVO source) {
        AdminQuestionFormatVO resultVO = new AdminQuestionFormatVO();
        BeanUtils.copyProperties(source, resultVO);
        if (source.getChildKeyWords() == null) {
            source.setChildKeyWords(Lists.newLinkedList());
        }
        int type = resultVO.getType();
        if (EssayAnswerFormatTypeConstant.EssayAnswerFormatTypeEnum.NULL.getTypeId() == type) {
            resultVO.setScore(source.getTitleScore());
            return resultVO;
        }
        //标题是必有的，所有不用加特定的判断语句
        resultVO.setTitleInfo(AnswerSubFormatVO.builder()
                .childKeyWords(getKeyWordsByType(source.getChildKeyWords(), EssayAnswerKeyWordConstant.QUESTION_FROM_TITLE_CHILD_KEYWORD)).score(source.getTitleScore()).build());
        if (EssayAnswerFormatTypeConstant.EssayAnswerFormatTypeEnum.TITLEANDAPPELLATION.getTypeId() == type
                || EssayAnswerFormatTypeConstant.EssayAnswerFormatTypeEnum.TITLEANDAPPELLATIONANDINSCRIBE.getTypeId() == type) {
            resultVO.setAppellationInfo(AnswerSubFormatVO.builder()
                    .childKeyWords(getKeyWordsByType(source.getChildKeyWords(), EssayAnswerKeyWordConstant.QUESTION_FROM_APPELLATION_CHILD_KEYWORD)).score(source.getAppellationScore()).build());
        }
        if (EssayAnswerFormatTypeConstant.EssayAnswerFormatTypeEnum.TITLEANDINSCRIBE.getTypeId() == type
                || EssayAnswerFormatTypeConstant.EssayAnswerFormatTypeEnum.TITLEANDAPPELLATIONANDINSCRIBE.getTypeId() == type) {
            resultVO.setInscribeInfo(AnswerSubFormatVO.builder()
                    .childKeyWords(getKeyWordsByType(source.getChildKeyWords(), EssayAnswerKeyWordConstant.QUESTION_FROM_INSCRIBE_CHILD_KEYWORD)).score(source.getInscribeScore()).build());
        }
        return resultVO;
    }

    private static List<AdminQuestionKeyWordVO> getKeyWordsByType(List<EssayStandardAnswerKeyWordVO> childKeyWords, int type) {
        List<AdminQuestionKeyWordVO> resultVO = Lists.newLinkedList();
        for (EssayStandardAnswerKeyWordVO keyWordVO : childKeyWords) {
            if (type == keyWordVO.getType()) {
                resultVO.add(convertKeyWordVO2Pre(keyWordVO));
            }
        }
        return resultVO;
    }

    /**
     * 将扣分规则前端数据转换为VO模型
     *
     * @param source
     * @return
     */
    public static EssayQuestionDeductRuleVO convertDeductPre2VO(AdminQuestionDeductRuleVO source) {
        long questionId = source.getQuestionDetailId();
        boolean isNumLimit = false;
        List<EssayStandardAnswerRuleSpecialStripVO> specialStripVOS = Lists.newLinkedList();
        List<EssayStandardAnswerRuleVO> commonRuleVOs = Lists.newLinkedList();
        for (AdminCommonDeductVO rule : source.getDeductRuleList()) {
            rule.setQuestionDetailId(questionId);
            int ruleType = -1;
            int type = -1;
            try {
                String[] ruleTypes = rule.getType().split("-");
                //ruleType：1特殊规则2普通规则  type： 各自规则的细分类别
                ruleType = Integer.parseInt(ruleTypes[0]);
                type = Integer.parseInt(ruleTypes[1]);
            } catch (Exception e) {
                log.error("规则解析出错");
                e.printStackTrace();
                throw new BizException(EssayErrors.ESSAY_PARAM_ILLEGAL);
            }
            if (2 == ruleType) {
                //组装特殊规则VO对象
                List<String> words = rule.getSpecialWords();
                EssayStandardAnswerRuleSpecialStripVO stripVO = EssayStandardAnswerRuleSpecialStripVO.builder()
                        .type(type)
                        .deductType(rule.getDeductType()).deductScore(rule.getDeductTypeScorePercent())
                        .questionDetailId(questionId)
                        .build();
                if (CollectionUtils.isEmpty(words)) {
                    specialStripVOS.add(stripVO);
                    continue;
                }
                stripVO.setWordNum(words.size());
                for (int i = 0; i < words.size(); i++) {
                    switch (i) {
                        case 0: {
                            stripVO.setFirstWord(words.get(i));
                            break;
                        }
                        case 1: {
                            stripVO.setSecondWord(words.get(i));
                            break;
                        }
                        case 2: {
                            stripVO.setThirdWord(words.get(i));
                            break;
                        }
                        case 3: {
                            stripVO.setFourthWord(words.get(i));
                            break;
                        }
                        case 4: {
                            stripVO.setFifthWord(words.get(i));
                            break;
                        }
                    }
                }
                specialStripVOS.add(stripVO);
            } else {
                //组装普通规则VO对象(判断是否存在字数限制规则，如果有将规则标识isNumLimit变为true)
                EssayStandardAnswerRuleVO ruleVO = new EssayStandardAnswerRuleVO();
                BeanUtils.copyProperties(rule, ruleVO);
                if (1 == ruleType) {
                    if (checkNumLimitRule(rule, type)) {
                        ruleVO.setDeductType(1);
                        isNumLimit = true;
                    }
                }
                ruleVO.setType(type);
                commonRuleVOs.add(ruleVO);
            }
        }
        if (!isNumLimit) {
            log.error("试题字数限制规则未录入");
            throw new BizException(EssayErrors.NO_EXISTED_QUESTION_NUM_LIMIT);
        }
        return EssayQuestionDeductRuleVO.builder().questionDetailId(questionId).specialStripList(specialStripVOS).commonRuleList(commonRuleVOs).build();
    }

    public static boolean checkNumLimitRule(AdminCommonDeductVO rule, int type) {
        if (1 != type) {
            return false;
        }
        if (0 >= rule.getMaxNum() || 0 >= rule.getMinNum()) {
            log.error("试题字数限制规则存在参数未录入");
            throw new BizException(EssayErrors.PARAM_ILLEGAL_IN_QUESTION_NUM_LIMIT);
        }
        return true;
    }

    /**
     * 将扣分规则VO模型转换为前端数据
     *
     * @param source
     * @return
     */
    public static AdminQuestionDeductRuleVO convertDeductVO2Pre(EssayQuestionDeductRuleVO source) {
        AdminQuestionDeductRuleVO result = new AdminQuestionDeductRuleVO();
        result.setQuestionDetailId(source.getQuestionDetailId()
        );
        List<AdminCommonDeductVO> deductRuleList = Lists.newLinkedList();
        if (CollectionUtils.isNotEmpty(source.getCommonRuleList())) {
            for (EssayStandardAnswerRuleVO ruleVO : source.getCommonRuleList()) {
                AdminCommonDeductVO deductVO = new AdminCommonDeductVO();
                BeanUtils.copyProperties(ruleVO, deductVO);
                deductVO.setType("1-" + ruleVO.getType());
                deductRuleList.add(deductVO);
            }
        }
        if (CollectionUtils.isNotEmpty(source.getSpecialStripList())) {
            for (EssayStandardAnswerRuleSpecialStripVO specialStripVO : source.getSpecialStripList()) {
                int num = specialStripVO.getWordNum();
                List<String> specialWords = Lists.newLinkedList();
                for (int i = 0; i < num; i++) {
                    switch (i) {
                        case 0: {
                            specialWords.add(i, specialStripVO.getFirstWord());
                            break;
                        }
                        case 1: {
                            specialWords.add(i, specialStripVO.getSecondWord());
                            break;
                        }
                        case 2: {
                            specialWords.add(i, specialStripVO.getThirdWord());
                            break;
                        }
                        case 3: {
                            specialWords.add(i, specialStripVO.getFourthWord());
                            break;
                        }
                        case 4: {
                            specialWords.add(i, specialStripVO.getFifthWord());
                            break;
                        }
                    }
                }
                AdminCommonDeductVO deductVO = AdminCommonDeductVO.builder()
                        .id(specialStripVO.getId())
                        .type("2-" + specialStripVO.getType())
                        .specialWords(specialWords)
                        .deductType(specialStripVO.getDeductType())
                        .deductTypeScorePercent(specialStripVO.getDeductScore())
                        .build();
                deductRuleList.add(deductVO);
            }
        }
        result.setDeductRuleList(deductRuleList);
        return result;
    }

    /**
     * 批量转化为前端对象
     *
     * @param essayStandardAnswerKeyWordVOS
     * @return
     */
    public static List<AdminQuestionKeyWordVO> convertBatchKeyWordVO2Pre(List<EssayStandardAnswerKeyWordVO> essayStandardAnswerKeyWordVOS) {
        List<AdminQuestionKeyWordVO> resultList = Lists.newLinkedList();
        for (EssayStandardAnswerKeyWordVO essayStandardAnswerKeyWordVO : essayStandardAnswerKeyWordVOS) {

            resultList.add(EssayConvertUtil.convertKeyWordVO2Pre(essayStandardAnswerKeyWordVO));
        }
        if (CollectionUtils.isEmpty(essayStandardAnswerKeyWordVOS)) {
            resultList.add(new AdminQuestionKeyWordVO());
        }
        return resultList;
    }

    /**
     * 批量转化为前端对象
     *
     * @param essayStandardAnswerKeyPhraseVOS
     * @return
     */
    public static List<AdminQuestionKeyPhraseVO> convertBatchKeyPhraseVO2Pre(List<EssayStandardAnswerKeyPhraseVO> essayStandardAnswerKeyPhraseVOS) {
        if (CollectionUtils.isEmpty(essayStandardAnswerKeyPhraseVOS)) {
            List<AdminQuestionKeyPhraseVO> nullList = Lists.newLinkedList();
            nullList.add(new AdminQuestionKeyPhraseVO());
            return nullList;
        }
        List<AdminQuestionKeyPhraseVO> resultVO = Lists.newLinkedList();
        for (EssayStandardAnswerKeyPhraseVO essayStandardAnswerKeyPhraseVO : essayStandardAnswerKeyPhraseVOS) {
            resultVO.add(EssayConvertUtil.convertKeyPhraseVO2Pre(essayStandardAnswerKeyPhraseVO));
        }
        return resultVO;
    }
}
