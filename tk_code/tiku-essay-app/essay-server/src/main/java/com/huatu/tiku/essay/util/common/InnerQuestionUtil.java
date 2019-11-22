package com.huatu.tiku.essay.util.common;

import com.google.common.collect.Lists;
import com.huatu.tiku.essay.vo.admin.*;
import com.huatu.tiku.essay.vo.resp.EssayStandardAnswerSplitWordVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.function.Function;

public class InnerQuestionUtil {

    public static Function<AdminQuestionTopicVO, AdminQuestionTopicVO> saveTopicVO;

    public static void setSaveTopicVO(Function<AdminQuestionTopicVO, AdminQuestionTopicVO> saveTopicVO) {
        InnerQuestionUtil.saveTopicVO = saveTopicVO;
    }

    /**
     * 关键句一级数据复制
     */
    public static Function<AdminQuestionKeyPhraseVO, AdminQuestionKeyPhraseVO> copyKeyPhrase = (i -> AdminQuestionKeyPhraseVO.builder()
            .id(i.getId())
            .item(i.getItem())
            .level(i.getLevel())
            .pid(i.getPid())
            .position(i.getPosition())
            .questionDetailId(i.getQuestionDetailId())
            .score(i.getScore())
            .type(i.getType())
            .build());

    /**
     * 关键词一级数据复制
     */
    public static Function<AdminQuestionKeyWordVO, AdminQuestionKeyWordVO> copyKeyWord = (i -> AdminQuestionKeyWordVO.builder()
            .id(i.getId())
            .item(i.getItem())
            .questionDetailId(i.getQuestionDetailId())
            .score(i.getScore())
            .type(i.getType())
            .correspondingId(i.getCorrespondingId())
            .build());

    public static Function<AdminQuestionKeyPhraseWithDescVO, AdminQuestionKeyPhraseWithDescVO> copyKeyPhrase1 = (i ->
            AdminQuestionKeyPhraseWithDescVO.builder()
                    .id(i.getId())
                    .item(i.getItem())
                    .position(i.getPosition())
                    .questionDetailId(i.getQuestionDetailId())
                    .score(i.getScore())
                    .type(i.getType())
                    .build());

    public static Function<AdminQuestionKeyWordWithDescVO, AdminQuestionKeyWordWithDescVO> copyKeyWord1 = (i ->
            AdminQuestionKeyWordWithDescVO.builder()
                    .id(i.getId())
                    .item(i.getItem())
                    .questionDetailId(i.getQuestionDetailId())
                    .score(i.getScore())
                    .type(i.getType())
                    .build());

    /**
     * 写入主题句
     */
    public static void writeMainTopic(AdminQuestionTopicVO resource, AdminQuestionVO questionVO) {
        if (null == resource || (resource.getId() <= 0 && StringUtils.isBlank(resource.getItem()))) {
            return;
        }
        AdminQuestionTopicVO tempTopicVO = AdminQuestionTopicVO.builder()
                .id(0)
                .item(resource.getItem())
                .score(resource.getScore())
                .questionDetailId(questionVO.getQuestionDetailId())
                .splitWordList(Lists.newArrayList())
                .build();
        AdminQuestionTopicVO result = saveTopicVO.apply(tempTopicVO);
        List<EssayStandardAnswerSplitWordVO> splitWordList = resource.getSplitWordList();
        if (CollectionUtils.isEmpty(splitWordList) || (splitWordList.get(0).getId() <= 0 && StringUtils.isBlank(splitWordList.get(0).getItem()))) {
            return;
        }
        for (EssayStandardAnswerSplitWordVO essayStandardAnswerSplitWordVO : splitWordList) {
            essayStandardAnswerSplitWordVO.setId(0);
            essayStandardAnswerSplitWordVO.setRelationId(result.getId());
        }
        result.setSplitWordList(splitWordList);
        saveTopicVO.apply(result);
    }

    /**
     * 写入中心论点
     */
    public static void writeArgumentOrPhrase(AdminQuestionKeyPhraseVO resource, AdminQuestionVO questionVO, Function<AdminQuestionKeyPhraseVO, AdminQuestionKeyPhraseVO> save) {
        if (null == resource || (resource.getId() <= 0 && StringUtils.isBlank(resource.getItem()))) {
            return;
        }
        AdminQuestionKeyPhraseVO tempKeyPhraseVO = copyKeyPhrase.apply(resource);
        tempKeyPhraseVO.setId(-1);
        tempKeyPhraseVO.setQuestionDetailId(questionVO.getQuestionDetailId());
        tempKeyPhraseVO.setSimilarPhraseList(Lists.newArrayList());
        tempKeyPhraseVO.setKeyWordVOList(Lists.newArrayList());
        AdminQuestionKeyPhraseVO temp = save.apply(tempKeyPhraseVO);
        List<AdminQuestionKeyWordVO> adminQuestionKeyWordVOS = resource.getKeyWordVOList();
        List<AdminQuestionKeyPhraseVO> similarPhraseList = resource.getSimilarPhraseList();
        if (CollectionUtils.isNotEmpty(adminQuestionKeyWordVOS) && StringUtils.isNotBlank(adminQuestionKeyWordVOS.get(0).getItem())) {
            adminQuestionKeyWordVOS.forEach(i -> i.clearId());
            temp.setKeyWordVOList(adminQuestionKeyWordVOS);
        }
        if (CollectionUtils.isNotEmpty(similarPhraseList) && StringUtils.isNotBlank(similarPhraseList.get(0).getItem())) {
            similarPhraseList.forEach(i -> i.clearId());
            temp.setSimilarPhraseList(similarPhraseList);
        }
        save.apply(temp);
    }

    public static void writeKeyWord(AdminQuestionKeyWordVO resource, AdminQuestionVO adminQuestion, Function<AdminQuestionKeyWordVO, AdminQuestionKeyWordVO> saveKeyWord) {
        if (null == resource || (resource.getId() <= 0 && StringUtils.isBlank(resource.getItem()))) {
            return;
        }
        AdminQuestionKeyWordVO tempKeyWord = copyKeyWord.apply(resource);
        tempKeyWord.setQuestionDetailId(adminQuestion.getQuestionDetailId());
        tempKeyWord.setId(-1);
        tempKeyWord.setCorrespondingId(-1);
        AdminQuestionKeyWordVO temp = saveKeyWord.apply(tempKeyWord);
        List<AdminQuestionKeyWordVO> similarWordVOList = resource.getSimilarWordVOList();
        if (CollectionUtils.isNotEmpty(similarWordVOList) && StringUtils.isNotBlank(similarWordVOList.get(0).getItem())) {
            similarWordVOList.forEach(i -> i.clearId());
            temp.setSimilarWordVOList(similarWordVOList);
        }
        List<String> splitWords = resource.getSplitWords();
        if (CollectionUtils.isNotEmpty(splitWords)) {
            temp.setSplitWords(splitWords);
        }
        saveKeyWord.apply(temp);
    }

    public static void writeKeyPhraseWithDescList(AdminQuestionKeyPhraseWithDescVO resource, AdminQuestionVO adminQuestion, Function<AdminQuestionKeyPhraseWithDescVO, AdminQuestionKeyPhraseWithDescVO> saveKeyPhraseWithDesc) {
        if (null == resource || (resource.getId() <= 0 && StringUtils.isBlank(resource.getItem()))) {
            return;
        }
        AdminQuestionKeyPhraseWithDescVO tempKeyPhrase = copyKeyPhrase1.apply(resource);
        tempKeyPhrase.setQuestionDetailId(adminQuestion.getQuestionDetailId());
        tempKeyPhrase.setId(-1);
        tempKeyPhrase.setKeyPhraseList(Lists.newArrayList());
        List<AdminQuestionKeyPhraseVO> keyPhraseList = resource.getKeyPhraseList();
        if (CollectionUtils.isNotEmpty(keyPhraseList) && StringUtils.isNotBlank(keyPhraseList.get(0).getItem())) {
            keyPhraseList.forEach(i -> i.clearId());
            tempKeyPhrase.setKeyPhraseList(keyPhraseList);
        }
        saveKeyPhraseWithDesc.apply(tempKeyPhrase);
    }

    public static void writeKeyWordWithDescList(AdminQuestionKeyWordWithDescVO resource, AdminQuestionVO adminQuestion, Function<AdminQuestionKeyWordWithDescVO, AdminQuestionKeyWordWithDescVO> saveKeyWordWithDesc) {
        if (null == resource || (resource.getId() <= 0 && StringUtils.isBlank(resource.getItem()))) {
            return;
        }
        AdminQuestionKeyWordWithDescVO tempKeyWord = copyKeyWord1.apply(resource);
        tempKeyWord.setId(-1);
        tempKeyWord.setKeyWordList(Lists.newArrayList());
        tempKeyWord.setQuestionDetailId(adminQuestion.getQuestionDetailId());
        List<AdminQuestionKeyWordVO> keyWordList = resource.getKeyWordList();
        if (CollectionUtils.isNotEmpty(keyWordList) && StringUtils.isNotBlank(keyWordList.get(0).getItem())) {
            keyWordList.forEach(i -> i.clearId());
            tempKeyWord.setKeyWordList(keyWordList);
        }
        saveKeyWordWithDesc.apply(tempKeyWord);
    }
}
