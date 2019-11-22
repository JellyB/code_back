package com.huatu.tiku.essay.service;

import com.huatu.tiku.essay.vo.admin.*;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;

import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/9/26
 * @描述
 */

public interface EssayKeyWordExportService {

    List<KeyWordExportFormateVo> convertQuestionKeyWord(List<AdminQuestionKeyWordWithDescVO> questionKeyWordWithDescVOList);


    /**
     * 获取所有关键词
     */
    List<String> getKeyWordContent(List<AdminQuestionKeyWordVO> keyWordList);

    /**
     * 关键句
     */
    void writeKeySentenceWithDesc(List<AdminQuestionKeyPhraseWithDescVO> keySentenceList, com.lowagie.text.Document document,
                                  Font font, int alignment);


    void writeTopicAndArgument(AdminQuestionKeyRuleVO questionKeyRuleVO, com.lowagie.text.Document document,
                               Font littleTitleFont,Font font, int alignment) throws DocumentException;

    void writeKeySentence(List<AdminQuestionKeyPhraseVO> keyPhraseList, Document document, Font keySentenceFont, Font contentFont) throws DocumentException;

    /**
     * 应用文格式
     *
     * @param answerFormatByQuestion
     * @param document
     * @param contentFont
     * @throws DocumentException
     */
    void writeAnswerFormat(AdminQuestionFormatVO answerFormatByQuestion, Document document, Font contentFont) throws DocumentException;
}