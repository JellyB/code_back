package com.huatu.tiku.essay.service.impl;

import com.google.common.collect.Lists;
import com.huatu.tiku.essay.constant.Export.ExportKeyWordConstant;
import com.huatu.tiku.essay.essayEnum.KeyPhrasePositionEnum;
import com.huatu.tiku.essay.service.EssayExportService;
import com.huatu.tiku.essay.service.EssayKeyWordExportService;
import com.huatu.tiku.essay.vo.admin.*;
import com.huatu.tiku.essay.vo.resp.EssayStandardAnswerSplitWordVO;
import com.huatu.tiku.essay.web.controller.admin.EssayRuleController;
import com.huatu.ztk.commons.JsonUtil;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.huatu.tiku.essay.constant.Export.ExportKeyWordConstant.keyWord;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/9/26
 * @描述
 */
@Service
@Slf4j
public class EssayKeyWordExportServiceImpl implements EssayKeyWordExportService {


    @Autowired
    EssayExportService exportService;

    @Autowired
    EssayRuleController essayRuleController;


    /**
     * 带描述的关键句
     *
     * @param keySentenceList
     * @param document
     * @param font
     * @param alignment
     */
    public void writeKeySentenceWithDesc(List<AdminQuestionKeyPhraseWithDescVO> keySentenceList, com.lowagie.text.Document document,
                                         Font font, int alignment) {

        if (CollectionUtils.isEmpty(keySentenceList)) {
            return;
        }
        BaseFont bfChinese = null;
        try {
            bfChinese = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            // 正文字体风格
            Font contentFont = new Font(bfChinese, 10, Font.NORMAL);
            //关键句风格
            Font keySentenceFont = new Font(bfChinese, 10, Font.BOLD);
            for (AdminQuestionKeyPhraseWithDescVO keySentenceVO : keySentenceList) {
                //关键句描述
                String keySentenceDesc = combineDesc(keySentenceVO.getItem(), keySentenceVO.getScore());
                exportService.addContent(keySentenceDesc, document, contentFont, Element.ALIGN_LEFT);
                List<AdminQuestionKeyPhraseVO> keyPhraseList = keySentenceVO.getKeyPhraseList();
                writeKeySentence(keyPhraseList, document, keySentenceFont, contentFont);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关键句写入
     *
     * @param keyPhraseList
     * @param document
     * @param keySentenceFont
     * @param contentFont
     * @throws DocumentException
     */
    public void writeKeySentence(List<AdminQuestionKeyPhraseVO> keyPhraseList, Document document, Font keySentenceFont, Font contentFont) throws DocumentException {
        if (CollectionUtils.isNotEmpty(keyPhraseList)) {
            Integer keyPhraseIndex = 1;
            for (AdminQuestionKeyPhraseVO phraseVO : keyPhraseList) {
                //关键句
                String keySentence = getKeyPhraseVO(phraseVO, ExportKeyWordConstant.keyPhrase, keyPhraseIndex);
                log.info("关键句:{}", keySentence);
                exportService.addContent(keySentence, document, keySentenceFont, Element.ALIGN_LEFT);
                //关键句关键词
                List<AdminQuestionKeyWordVO> keyWordVOList = phraseVO.getKeyWordVOList();
                if (CollectionUtils.isNotEmpty(keyWordVOList)) {
                    keyWordWriteConsumer(keyWordVOList, document, contentFont);
                }
                //近似句 &&  近似句的关键词
                List<AdminQuestionKeyPhraseVO> similarPhraseList = phraseVO.getSimilarPhraseList();
                if (CollectionUtils.isNotEmpty(similarPhraseList)) {
                    writeKeyWordToDocument(similarPhraseList, document, contentFont,
                            ExportKeyWordConstant.similarPhrase, keyWord);
                }
                keyPhraseIndex++;
            }
        }
    }


    /**
     * 关键词写入到word
     *
     * @param keyPhraseList
     */
    public void writeKeyWordToDocument(List<AdminQuestionKeyPhraseVO> keyPhraseList, Document document, Font
            contentFont, String keyPhrase, String keyWord) throws DocumentException {

        if (CollectionUtils.isEmpty(keyPhraseList)) {
            return;
        }
        int keySentenceIndex = 1;
        for (AdminQuestionKeyPhraseVO phraseVO : keyPhraseList) {

            String keyContent = getKeyPhraseVO(phraseVO, ExportKeyWordConstant.similarPhrase, keySentenceIndex);
            //写入关键句
            exportService.addContent(keyContent, document, contentFont, Element.ALIGN_LEFT);
            //关键词
            List<AdminQuestionKeyWordVO> keyWordVOList = phraseVO.getKeyWordVOList();
            if (CollectionUtils.isNotEmpty(keyWordVOList)) {
                keyWordWriteConsumer(keyWordVOList, document, contentFont);
            }
            keySentenceIndex++;
        }
    }

    /**
     * 关键句格式
     *
     * @param keyPhrase
     * @param phraseType
     * @param index
     * @return
     */
    public String getKeyPhraseVO(AdminQuestionKeyPhraseVO keyPhrase, String phraseType, Integer index) {

        StringBuilder keySentence = new StringBuilder();
        keySentence.append(phraseType);
        keySentence.append(index);
        keySentence.append(ExportKeyWordConstant.colonMark);
        keySentence.append(keyPhrase.getItem());
        keySentence.append(ExportKeyWordConstant.leftBracket);
        keySentence.append(translateScore.apply(keyPhrase.getScore()));
        keySentence.append(ExportKeyWordConstant.scoreMark);
        keySentence.append(ExportKeyWordConstant.commaSymbol);
        //位置
        KeyPhrasePositionEnum keyPhrasePosition = KeyPhrasePositionEnum.getKeyPhrasePosition(keyPhrase.getPosition());
        keySentence.append(keyPhrasePosition.getDesc());
        keySentence.append(ExportKeyWordConstant.fullStopMark);
        keySentence.append(ExportKeyWordConstant.rightBracket);
        log.info("组成关键句:{}", keySentence);
        return keySentence.toString();
    }


    /**
     * 关键句关键词写入
     */
    public void keyWordWriteConsumer(List<AdminQuestionKeyWordVO> keyWordVOList, Document document, Font
            contentFont) throws DocumentException {
        if (CollectionUtils.isEmpty(keyWordVOList)) {
            return;
        }
        //log.info("关键词参数是:{}", JsonUtil.toJson(keyWordVOList));
        Integer keyWordIndex = 1;
        List<String> keyWordContentList = getKeyWordContent(keyWordVOList);
        // log.info("参数是:{}", JsonUtil.toJson(keyWordContentList));
        if (CollectionUtils.isNotEmpty(keyWordContentList)) {
            for (String content : keyWordContentList) {
                StringBuilder keyWordContent = new StringBuilder();
                keyWordContent.append(ExportKeyWordConstant.keyWord);
                keyWordContent.append(keyWordIndex);
                keyWordContent.append(ExportKeyWordConstant.colonMark);
                keyWordContent.append(content);
                log.info("关键词:{}", keyWordContent.toString());
                exportService.addContent(keyWordContent.toString(), document, contentFont, Element.ALIGN_LEFT);
                keyWordIndex++;
            }
        }
    }


    /**
     * 带描述的关键词
     *
     * @param questionKeyWordWithDescVOList
     * @return
     */
    public List<KeyWordExportFormateVo> convertQuestionKeyWord
    (List<AdminQuestionKeyWordWithDescVO> questionKeyWordWithDescVOList) {

        List<KeyWordExportFormateVo> keyWordExportFormatVoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(questionKeyWordWithDescVOList)) {
            for (AdminQuestionKeyWordWithDescVO questionKeyWordWithDescVO : questionKeyWordWithDescVOList) {
                KeyWordExportFormateVo keyWordExportFormateVo = new KeyWordExportFormateVo();
                String combineDesc = combineDesc(questionKeyWordWithDescVO.getItem(), questionKeyWordWithDescVO.getScore());
                //描述
                if (StringUtils.isNotEmpty(combineDesc)) {
                    keyWordExportFormateVo.setPhrase(combineDesc);
                }
                //关键词
                List<AdminQuestionKeyWordVO> keyWordList = questionKeyWordWithDescVO.getKeyWordList();
                if (CollectionUtils.isNotEmpty(keyWordList)) {
                    List<String> keyWordContent = getKeyWordContent(keyWordList);
                    keyWordExportFormateVo.setKeyWordList(keyWordContent);
                }
                keyWordExportFormatVoList.add(keyWordExportFormateVo);
            }
            return keyWordExportFormatVoList;
        }
        return Lists.newArrayList();
    }

    /**
     * 获取单个描述内容
     */
    public String combineDesc(String descContent, double score) {

        StringBuilder phraseKeyWordDesc = new StringBuilder();
        if (StringUtils.isNotEmpty(descContent)) {

            phraseKeyWordDesc.append(ExportKeyWordConstant.keyWordDesc);
            phraseKeyWordDesc.append(descContent);
            phraseKeyWordDesc.append(ExportKeyWordConstant.leftBracket);
            phraseKeyWordDesc.append(translateScore.apply(score));
            phraseKeyWordDesc.append(ExportKeyWordConstant.scoreMark);
            phraseKeyWordDesc.append(ExportKeyWordConstant.rightBracket);
            return phraseKeyWordDesc.toString();
        }
        return "";
    }


    /**
     * 获取所有关键词
     */
    public List<String> getKeyWordContent(List<AdminQuestionKeyWordVO> keyWordList) {
        //获取关键词
        List<String> keywordContentList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(keyWordList)) {
            for (AdminQuestionKeyWordVO keyWordVO : keyWordList) {

                StringBuilder keyWordContent = new StringBuilder();
                List<String> splitKeyWords = keyWordVO.getSplitWords();
                List<AdminQuestionKeyWordVO> similarWordVOList = keyWordVO.getSimilarWordVOList();

                String allSimilarItem = "";
                if (CollectionUtils.isNotEmpty(similarWordVOList)) {
                    allSimilarItem = convertSimilarWordFunction.apply(similarWordVOList, true);
                }
                keyWordContent.append(contentAddDoubleMark.apply(keyWordVO.getItem()));
                keyWordContent.append(ExportKeyWordConstant.leftBracket);
                keyWordContent.append(translateScore.apply(keyWordVO.getScore()));
                keyWordContent.append(ExportKeyWordConstant.scoreMark);
                if (CollectionUtils.isNotEmpty(splitKeyWords)) {
                    keyWordContent.append(ExportKeyWordConstant.commaSymbol);
                    keyWordContent.append(convertSpiltKeyWordToString.apply(splitKeyWords));
                }
                keyWordContent.append(ExportKeyWordConstant.fullStopMark);
                keyWordContent.append(allSimilarItem);
                keyWordContent.append(ExportKeyWordConstant.rightBracket);
                keywordContentList.add(keyWordContent.toString());
                log.info("关键词拼接结果:{}", keyWordContent.toString());
            }
        }
        return keywordContentList;
    }


    /**
     * 为词语添加双引号
     */
    Function<String, String> contentAddDoubleMark = (content -> {
        String formatMark = ExportKeyWordConstant.add_double_quotation_marks;
        return String.format(formatMark, content);
    });


    /**
     * 拆分词
     */
    Function<List<String>, String> convertSpiltKeyWordToString = (spiltList -> {
        StringBuilder spiltKeyWordContent = new StringBuilder();
        if (CollectionUtils.isNotEmpty(spiltList)) {
            for (String item : spiltList) {
                spiltKeyWordContent.append(contentAddDoubleMark.apply(item));
            }
            log.info("拆分次:{}", JsonUtil.toJson(spiltKeyWordContent.toString()));
            return spiltKeyWordContent.toString();
        }
        return "";
    });

    /**
     * 分数处理
     */
    Function<Double, String> translateScore = (score -> {
        String newScoreStr = score == null ? "0" : String.valueOf(score);
        if (newScoreStr.indexOf(".0") > 0) {
            newScoreStr = newScoreStr.substring(0, newScoreStr.indexOf(".0"));
        }
        return newScoreStr;
    });


    /**
     * 相似词语
     * 议论文关键词 格式为: "关键词","拆分次","拆分词"。
     * 其他关键词 格式为: "关键词"1分,"拆分次","拆分词"。
     * isAddScoreFlag 是否需要导出分数
     */
    BiFunction<List<AdminQuestionKeyWordVO>, Boolean, String> convertSimilarWordFunction = ((similarList, isAddScoreFlag) -> {

        if (CollectionUtils.isNotEmpty(similarList)) {
            List<String> list = new ArrayList<>();
            for (AdminQuestionKeyWordVO questionKeyWordVO : similarList) {
                StringBuilder content = new StringBuilder();
                List<String> similarSplitWords = questionKeyWordVO.getSplitWords();
                if (StringUtils.isNotEmpty(questionKeyWordVO.getItem())) {
                    //关键词近义词
                    content.append(contentAddDoubleMark.apply(questionKeyWordVO.getItem()));
                }
                if (isAddScoreFlag) {
                    //关键词近义词分数
                    content.append(translateScore.apply(questionKeyWordVO.getScore()));
                    content.append(ExportKeyWordConstant.scoreMark);
                }
                if (CollectionUtils.isNotEmpty(similarSplitWords)) {
                    content.append(ExportKeyWordConstant.commaSymbol);
                    //关键词近义词的拆分词
                    content.append(convertSpiltKeyWordToString.apply(similarSplitWords));
                }
                content.append(ExportKeyWordConstant.fullStopMark);
                list.add(content.toString());
            }
            log.info("相似词是:{}", JsonUtil.toJson(list));

            StringBuilder contentResult = new StringBuilder();
            if (CollectionUtils.isNotEmpty(list)) {
                list.stream().forEach(str -> {
                    if (StringUtils.isNotEmpty(str))
                        contentResult.append(str);
                });
            }
            log.info("相似词:{}", contentResult.toString());
            return contentResult.toString();
        }
        return "";
    });


    /**
     * 议论文主题 && 中心论点
     *
     * @param questionKeyRuleVO
     * @param document
     * @param font
     * @param alignment
     */
    public void writeTopicAndArgument(AdminQuestionKeyRuleVO questionKeyRuleVO, com.lowagie.text.Document document,
                                      Font littleTitleFont, Font font, int alignment) throws DocumentException {

        if (null == questionKeyRuleVO) {
            return;
        }
        AdminQuestionTopicVO topicVO = questionKeyRuleVO.getTopic();
        if (null != topicVO) {
            StringBuilder topicBuilder = new StringBuilder();
            //主题
            if (StringUtils.isNotEmpty(topicVO.getItem())) {
                topicBuilder.append(contentAddDoubleMark.apply(topicVO.getItem()));
            }
            //主题的关键词
            List<EssayStandardAnswerSplitWordVO> keyWordVoList = topicVO.getSplitWordList();
            if (CollectionUtils.isNotEmpty(keyWordVoList)) {
                List<String> keyWordList = keyWordVoList.stream().map(vo -> vo.getItem()).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(keyWordList)) {
                    String keyWordStr = convertSpiltKeyWordToString.apply(keyWordList);
                    if (StringUtils.isNotEmpty(keyWordStr)) {
                        topicBuilder.append(ExportKeyWordConstant.leftBracket);
                        topicBuilder.append(keyWordStr);
                        topicBuilder.append(ExportKeyWordConstant.fullStopMark);
                        topicBuilder.append(ExportKeyWordConstant.rightBracket);
                    }
                }
            }
            //log.info("主题关键词是:{}", topicBuilder.toString());
            //写入主题
            exportService.addContent(ExportKeyWordConstant.topic, document, littleTitleFont, Element.ALIGN_LEFT);
            //写入主题关键词
            exportService.addContent(topicBuilder.toString(), document, font, Element.ALIGN_LEFT);
        }
        List<AdminQuestionKeyPhraseVO> argumentList = questionKeyRuleVO.getArgumentList();
        if (CollectionUtils.isNotEmpty(argumentList)) {
            //论点
            int argumentIndex = 1;
            for (AdminQuestionKeyPhraseVO argument : argumentList) {
                StringBuilder argumentBuilder = new StringBuilder();
                String prefix = getPrefix.apply(ExportKeyWordConstant.argument, argumentIndex);
                argumentBuilder.append(prefix);
                argumentBuilder.append(argument.getItem());
                if (CollectionUtils.isNotEmpty(argument.getKeyWordVOList())) {
                    argumentBuilder.append(ExportKeyWordConstant.leftBracket);
                    argumentBuilder.append(convertSimilarWordFunction.apply(argument.getKeyWordVOList(), false));
                    argumentBuilder.append(ExportKeyWordConstant.rightBracket);
                }
                exportService.addContent(argumentBuilder.toString(), document, font, Element.ALIGN_LEFT);
                argumentIndex++;
            }
        }
    }

    /**
     * 组成前缀
     * 格式: xx+序号+：,（比如：论点2：,关键词1：,关键句1）
     */
    BiFunction<String, Integer, String> getPrefix = ((prefix, index) -> {
        if (StringUtils.isNotEmpty(prefix)) {
            StringBuilder prefixStr = new StringBuilder();
            prefixStr.append(prefix);
            prefixStr.append(index);
            prefixStr.append(ExportKeyWordConstant.colonMark);
            return prefixStr.toString();
        }
        return "";
    });


    /**
     * 内容格式分数
     *
     * @param answerFormatByQuestion
     * @param document
     * @param contentFont
     */
    public void writeAnswerFormat(AdminQuestionFormatVO answerFormatByQuestion, Document document, Font contentFont) throws DocumentException {
        if (null == answerFormatByQuestion) {
            return;
        }
        //标题
        writeRuleFormat(answerFormatByQuestion.getTitleInfo(), document, contentFont, ExportKeyWordConstant.title);
        //称呼
        writeRuleFormat(answerFormatByQuestion.getAppellationInfo(), document, contentFont, ExportKeyWordConstant.appellation);
        //称呼
        writeRuleFormat(answerFormatByQuestion.getInscribeInfo(), document, contentFont, ExportKeyWordConstant.inscribe);
    }

    /**
     * 内容格式分数
     *
     * @param answerSubFormatVO
     * @param document
     * @param contentFont
     * @param titleType
     * @throws DocumentException
     */
    public void writeRuleFormat(AnswerSubFormatVO answerSubFormatVO, Document document, Font contentFont, String titleType) throws DocumentException {
        if (null == answerSubFormatVO) {
            return;
        }
        List<AdminQuestionKeyWordVO> childKeyWords = answerSubFormatVO.getChildKeyWords();
        if (CollectionUtils.isEmpty(childKeyWords)) {
            return;
        }
        List<String> keyWordContentList = getKeyWordContent(childKeyWords);
        if (CollectionUtils.isNotEmpty(keyWordContentList)) {
            ExportAnswerFormatVO exportAnswerFormatVO = new ExportAnswerFormatVO();
            StringBuilder title = new StringBuilder();
            title.append(titleType);
            title.append(ExportKeyWordConstant.colonMark);
            title.append(answerSubFormatVO.getScore());
            title.append(ExportKeyWordConstant.fullStopMark);
            title.append("\n");
            exportAnswerFormatVO.setTitleScore(title.toString());
            exportService.addContent(title.toString(), document, contentFont, Element.ALIGN_LEFT);

            for (String keyWordContent : keyWordContentList) {
                StringBuilder keyword = new StringBuilder();
                keyword.append(ExportKeyWordConstant.keyWord);
                keyword.append(ExportKeyWordConstant.colonMark);
                keyword.append(keyWordContent);
                exportService.addContent(keyword.toString(), document, contentFont, Element.ALIGN_LEFT);
            }
        }
    }
}
