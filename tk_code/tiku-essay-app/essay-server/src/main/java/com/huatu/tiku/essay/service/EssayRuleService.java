package com.huatu.tiku.essay.service;

import com.huatu.tiku.essay.vo.admin.AdminQuestionKeyPhraseWithDescVO;
import com.huatu.tiku.essay.vo.admin.AdminQuestionKeyWordVO;
import com.huatu.tiku.essay.vo.admin.AdminQuestionKeyWordWithDescVO;
import com.huatu.tiku.essay.vo.admin.AdminQuestionTopicVO;
import com.huatu.tiku.essay.vo.resp.*;

import java.util.List;

/**
 * Created by huangqp on 2017\12\6 0006.
 */
public interface EssayRuleService {

    List<EssayStandardAnswerFormatVO> findAnswerFormatsByQuestion(long questionId);

    List<EssayStandardAnswerKeyPhraseVO> findAnswerKeyPhraseByQuestion(long questionId, int type,long pid);

    List<EssayStandardAnswerKeyWordVO> findAnswerKeyWordByQuestion(long questionId, int type,long pid);

    List<EssayStandardAnswerRuleSpecialStripVO> findAnswerRuleSpecialStripByQuestion(long questionId);

    List<EssayStandardAnswerRuleStripSegmentalVO> findAnswerRuleStripSegmentalByQuestion(long questionId);

    List<EssayStandardAnswerRuleWordNumVO> findAnswerRuleWordNumByQuestion(long questionId);

    EssayStandardAnswerFormatVO addAnswerFormat(EssayStandardAnswerFormatVO essayStandardAnswerFormat, int uid);

    EssayStandardAnswerKeyPhraseVO addAnswerKeyPhrase(EssayStandardAnswerKeyPhraseVO essayStandardAnswerKeyPhrase, int uid);

    EssayStandardAnswerKeyWordVO addAnswerKeyWord(EssayStandardAnswerKeyWordVO essayStandardAnswerKeyWord, int uid,int type);

    EssayStandardAnswerRuleSpecialStripVO addAnswerRuleSpecialStrip(EssayStandardAnswerRuleSpecialStripVO essayStandardAnswerRuleSpecialStrip, int uid);

    EssayStandardAnswerRuleStripSegmentalVO addAnswerRuleStripSegmental(EssayStandardAnswerRuleStripSegmentalVO essayStandardAnswerRuleStripSegmental, int uid);

    EssayStandardAnswerRuleWordNumVO addAnswerRuleWordNum(EssayStandardAnswerRuleWordNumVO essayStandardAnswerRuleWordNum, int uid);

    EssayStandardAnswerRuleWordNumVO updateAnswerRuleWordNum(EssayStandardAnswerRuleWordNumVO essayStandardAnswerRuleWordNum, int uid);

    EssayStandardAnswerRuleStripSegmentalVO updateAnswerRuleStripSegmental(EssayStandardAnswerRuleStripSegmentalVO essayStandardAnswerRuleStripSegmental, int uid);

    EssayStandardAnswerRuleSpecialStripVO updateAnswerRuleSpecialStrip(EssayStandardAnswerRuleSpecialStripVO essayStandardAnswerRuleSpecialStrip, int uid);

    EssayStandardAnswerKeyWordVO updateAnswerKeyWord(EssayStandardAnswerKeyWordVO essayStandardAnswerKeyWord, int uid);

    EssayStandardAnswerKeyPhraseVO updateAnswerKeyPhrase(EssayStandardAnswerKeyPhraseVO essayStandardAnswerKeyPhrase, int uid);

    EssayStandardAnswerFormatVO updateAnswerFormat(EssayStandardAnswerFormatVO essayStandardAnswerFormat, int uid);

    List<EssayStandardAnswerRuleVO> findCommonAnswerRule(long questionId);

    EssayStandardAnswerRuleVO addCommonAnswerRule(EssayStandardAnswerRuleVO essayStandardAnswerRuleVO,int uid);

    void delAllDeductRuleByQuestion(long questionDetailId, int uid);

//    void delAllKeyRuleByQuestion(long questionDetailId, int uid);

    void delEssayAnswerFormatByQuestion(long questionDetailId, int uid);

    AdminQuestionTopicVO addQuestionTopicVO(AdminQuestionTopicVO adminQuestionTopicVO, int uid);

    AdminQuestionTopicVO findAnswerTopicByQuestion(long questionDetailId);

    ResponseVO refresh();

    AdminQuestionKeyWordWithDescVO addAnswerKeyWordWithDesc(AdminQuestionKeyWordWithDescVO adminQuestionKeyWordWithDescVO, int uid);

    AdminQuestionKeyPhraseWithDescVO addAnswerKeyPhraseWithDesc(AdminQuestionKeyPhraseWithDescVO adminQuestionKeyPhraseWithDescVO, int uid);

    List<AdminQuestionKeyWordWithDescVO> findAnswerKeyWordByQuestionWithDesc(long questionDetailId);

    List<AdminQuestionKeyPhraseWithDescVO> findAnswerKeyPhraseByQuestionWithDesc(long questionDetailId);

//    List<EssayStandardAnswerKeyWordVO> findAnswerKeyWord(long pId, int type);
}
