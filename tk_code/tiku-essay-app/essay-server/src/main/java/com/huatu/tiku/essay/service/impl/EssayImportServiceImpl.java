package com.huatu.tiku.essay.service.impl;

import com.google.common.collect.Lists;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.status.EssayAnswerKeyWordConstant;
import com.huatu.tiku.essay.entity.EssayQuestionDetail;
import com.huatu.tiku.essay.repository.EssayQuestionDetailRepository;
import com.huatu.tiku.essay.repository.EssayStandardAnswerKeyPhraseRepository;
import com.huatu.tiku.essay.repository.EssayStandardAnswerKeyWordRepository;
import com.huatu.tiku.essay.service.EssayImportService;
import com.huatu.tiku.essay.vo.admin.*;
import com.huatu.tiku.essay.vo.export.KeyWithDescVO;
import com.huatu.tiku.essay.web.controller.admin.EssayRuleController;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.huatu.tiku.essay.constant.status.QuestionCorrectTypeConstant.KEY_PHRASE_CORRECT;
import static com.huatu.tiku.essay.constant.status.QuestionCorrectTypeConstant.KEY_WORD_CORRECT;


@Service
@Slf4j
public class EssayImportServiceImpl implements EssayImportService {

    @Autowired
    EssayQuestionDetailRepository essayQuestionDetailRepository;
    @Autowired
    EssayStandardAnswerKeyPhraseRepository essayStandardAnswerKeyPhraseRepository;
    @Autowired
    EssayStandardAnswerKeyWordRepository essayStandardAnswerKeyWordRepository;
    @Autowired
    EssayRuleController essayRuleController;
    //匹配描述 eg：【描述】XXXXX
    private static String descReg = "(】)(.*?)(([0-9]+((\\.){0,1}[0-9]{0,1}))分)";
    private static Pattern descPattern = Pattern.compile(descReg);

    //匹配关键词 eg：“XXXX”、"XXXX"
    private static String wordReg = "(\"|“)(.*?)(\"|”)";
    private static Pattern wordPattern = Pattern.compile(wordReg);

    //匹配分数 eg：（1分）、（2分，、 ”2分
    private static String scoreReg = "(”（|\"|”|）|（|,|，|\\()([0-9]+((\\.){0,1}[0-9]{0,1}))分";
    private static Pattern scorePattern = Pattern.compile(scoreReg);

    //匹配关键句 eg：1.XXXXX
    private static String phraseReg = "(\\.|:|：)(.*?)(（)";
    private static Pattern phrasePattern = Pattern.compile(phraseReg);

    //位置
    private static String positionReg = "(，|,|。)(开头|全篇|开头或中间|中间|中间或结尾|结尾)(，|,|。|\\)|）)";
    private static Pattern positionPattern = Pattern.compile(positionReg);

    @Override
    public AdminQuestionKeyRuleVO readQuestionRule(MultipartFile file, long questionDetailId) {
        //查询题目算法批改类型（关键词or关键句）
        EssayQuestionDetail questionDetail = essayQuestionDetailRepository.findById(questionDetailId);
        if (null == questionDetail) {
            log.info("题目ID错误。questionDetailId:{}", questionDetailId);
            throw new BizException(EssayErrors.ERROR_QUESTION_ID);
        }
        int correctType = questionDetail.getCorrectType();

        if (KEY_WORD_CORRECT != correctType && KEY_PHRASE_CORRECT != correctType) {
            log.info("该题暂未设置批改类型，请先选择匹配方式。questionDetailId：{}", questionDetailId);
            throw new BizException(EssayErrors.EMPTY_CORRECT_TYPE);
        }

        AdminQuestionKeyRuleVO ruleVO = new AdminQuestionKeyRuleVO();
        try {
            //1.文件预处理
            List<String> contentList = filePreHandle(file);

            //2.将数据转换成VO
            ruleVO = convertContent2VO(contentList, correctType);

            ruleVO.setQuestionDetailId(questionDetailId);

            List<Integer> list = Lists.newArrayList(EssayAnswerKeyWordConstant.QUESTION_FROM_TITLE_CHILD_KEYWORD, EssayAnswerKeyWordConstant.QUESTION_FROM_APPELLATION_CHILD_KEYWORD, EssayAnswerKeyWordConstant.QUESTION_FROM_INSCRIBE_CHILD_KEYWORD);
            //3.清除试题旧算法
            essayStandardAnswerKeyPhraseRepository.delByQuestionDetailIdByTypesNotIn(questionDetailId, list);
            essayStandardAnswerKeyWordRepository.delByQuestionDetailIdByTypesNotIn(questionDetailId, list);
            //4.调用保存算法接口
            ruleVO = essayRuleController.addKeyPhraseAndKeyWord(ruleVO);


        } catch (Exception e) {
            if (!(e instanceof BizException)) {
                e.printStackTrace();
                throw new BizException(EssayErrors.ERROR_DOC_STYLE);
            } else {
                throw e;
            }

        }
        return ruleVO;
    }

    /**
     * 转换成保存算法用的VO
     *
     * @param contentList
     * @return
     */
    private AdminQuestionKeyRuleVO convertContent2VO(List<String> contentList, int correctType) {
        AdminQuestionKeyRuleVO ruleVO = new AdminQuestionKeyRuleVO();

        if (KEY_WORD_CORRECT == correctType) {
            //带描述的关键词
            List<AdminQuestionKeyWordWithDescVO> keyWordWithDescList = getKeyWordWithDescList(contentList);
            ruleVO.setKeyWordWithDescList(keyWordWithDescList);

        } else if (KEY_PHRASE_CORRECT == correctType) {
            //带描述的关键句
            List<AdminQuestionKeyPhraseWithDescVO> keyPhraseWithDescList = getKeyPhraseWithDescList(contentList);
            ruleVO.setKeyPhraseWithDescList(keyPhraseWithDescList);
        }

        return ruleVO;
    }


    private static List<AdminQuestionKeyPhraseWithDescVO> getKeyPhraseWithDescList(List<String> contentList) {
        List<KeyWithDescVO> keyWithDescVOS = convertContentList2KeyWithDescList(contentList);

        //将字符串转换成保存用的VO
        List<AdminQuestionKeyPhraseWithDescVO> keyParaWithDescVOList = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(keyWithDescVOS)) {
            for (KeyWithDescVO vo : keyWithDescVOS) {

                if (StringUtils.isNotEmpty(vo.getDesc())) {

                    //描述句子转vo
                    AdminQuestionDescVO adminQuestionDescVO = convertSentence2Desc(vo.getDesc());
                    //关键句转vo
                    List<AdminQuestionKeyPhraseVO> adminQuestionKeyPhraseVOList = convertSentenceList2KeyPhraseList(vo.getKeyParaList());

                    AdminQuestionKeyPhraseWithDescVO keyPhraseWithDescVO = AdminQuestionKeyPhraseWithDescVO.builder()
                            .item(adminQuestionDescVO.getItem())
                            .score(adminQuestionDescVO.getScore())
                            .keyPhraseList(adminQuestionKeyPhraseVOList)
                            .build();

                    keyParaWithDescVOList.add(keyPhraseWithDescVO);
                }
            }

        }
        return keyParaWithDescVOList;
    }

    private static List<AdminQuestionKeyPhraseVO> convertSentenceList2KeyPhraseList(List<String> keyPhraseParaList) {
        List<AdminQuestionKeyPhraseVO> adminQuestionKeyPhraseVOS = new LinkedList<>();

        if (CollectionUtils.isNotEmpty(keyPhraseParaList)) {
            for (int i = 0; i < keyPhraseParaList.size(); i++) {
                String keyPhrasePara = keyPhraseParaList.get(i);
                if (StringUtils.isNotEmpty(keyPhrasePara)) {

                    boolean similarFlag = keyPhrasePara.startsWith("近义短句");
                    //关键句
                    if (getKeyPhrasePosition(keyPhrasePara) > 0) {

                        AdminQuestionKeyPhraseVO adminQuestionKeyPhraseVO = new AdminQuestionKeyPhraseVO();

                        List<String> keyWordParaList = new LinkedList<>();

                        adminQuestionKeyPhraseVO.setPosition(getKeyPhrasePosition(keyPhrasePara));
                        Matcher phraseMatcher = phrasePattern.matcher(keyPhrasePara);
                        while (phraseMatcher.find()) {
                            adminQuestionKeyPhraseVO.setItem(phraseMatcher.group(2));
                        }

                        Matcher scoreMatcher = scorePattern.matcher(keyPhrasePara);
                        while (scoreMatcher.find()) {
                            adminQuestionKeyPhraseVO.setScore(Double.parseDouble(scoreMatcher.group(2)));
                        }
                        //关键句的关键词
                        while ((i + 1) < keyPhraseParaList.size() && keyPhraseParaList.get(i + 1).startsWith("关键词")) {
                            i++;
                            keyPhrasePara = keyPhraseParaList.get(i);
                            keyWordParaList.add(keyPhrasePara);

                        }
                        List<AdminQuestionKeyWordVO> adminQuestionKeyWordVOS = convertSentenceList2KeyWordList(keyWordParaList);
                        adminQuestionKeyPhraseVO.setKeyWordVOList(adminQuestionKeyWordVOS);

                        if (similarFlag) {
                            List<AdminQuestionKeyPhraseVO> similarPhraseList = ((LinkedList<AdminQuestionKeyPhraseVO>) adminQuestionKeyPhraseVOS).getLast().getSimilarPhraseList();
                            if (CollectionUtils.isEmpty(similarPhraseList)) {
                                similarPhraseList = new LinkedList<>();
                            }
                            similarPhraseList.add(adminQuestionKeyPhraseVO);
                            ((LinkedList<AdminQuestionKeyPhraseVO>) adminQuestionKeyPhraseVOS).getLast().setSimilarPhraseList(similarPhraseList);
                        } else {
                            adminQuestionKeyPhraseVOS.add(adminQuestionKeyPhraseVO);
                        }
                    }
                }
            }
        }
        return adminQuestionKeyPhraseVOS;
    }

    /**
     * 读取带描述的关键词
     */
    private List<AdminQuestionKeyWordWithDescVO> getKeyWordWithDescList(List<String> contentList) {

        List<KeyWithDescVO> keyWithDescVOS = convertContentList2KeyWithDescList(contentList);

        //将字符串转换成保存用的VO
        List<AdminQuestionKeyWordWithDescVO> keyWordWithDescVOList = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(keyWithDescVOS)) {
            for (KeyWithDescVO vo : keyWithDescVOS) {
                if (StringUtils.isNotEmpty(vo.getDesc())) {
                    //描述句子转vo
                    AdminQuestionDescVO adminQuestionDescVO = convertSentence2Desc(vo.getDesc());
                    //关键词句子转vo
                    List<AdminQuestionKeyWordVO> adminQuestionKeyWordVOS = convertSentenceList2KeyWordList(vo.getKeyParaList());
                    AdminQuestionKeyWordWithDescVO adminQuestionKeyWordWithDescVO = AdminQuestionKeyWordWithDescVO.builder()
                            .item(adminQuestionDescVO.getItem())
                            .score(adminQuestionDescVO.getScore())
                            .keyWordList(adminQuestionKeyWordVOS)
                            .build();
                    keyWordWithDescVOList.add(adminQuestionKeyWordWithDescVO);
                }
            }
        }
        return keyWordWithDescVOList;
    }

    /**
     * 段落转换成带描述的VO
     *
     * @param contentList
     * @return
     */
    private static List<KeyWithDescVO> convertContentList2KeyWithDescList(List<String> contentList) {
        List<KeyWithDescVO> keyWithDescVOList = new LinkedList<>();
        KeyWithDescVO keyWithDescVO = new KeyWithDescVO();
        for (int i = 0; i < contentList.size(); i++) {
            String content = contentList.get(i);
            if (StringUtils.isNotEmpty(content)) {
                //2.1拆分成描述和关键词列表
                if (content.startsWith("【描述】")) {
                    //a.描述
                    if (null != keyWithDescVO && StringUtils.isNotEmpty(keyWithDescVO.getDesc())) {
                        keyWithDescVOList.add(keyWithDescVO);
                    }
                    keyWithDescVO = new KeyWithDescVO();
                    keyWithDescVO.setDesc(content);
                } else {
                    //b.关键词句列表
                    List<String> keyParaList = keyWithDescVO.getKeyParaList();
                    if (CollectionUtils.isEmpty(keyParaList)) {
                        keyParaList = new LinkedList<>();
                    }
                    keyParaList.add(content);
                    keyWithDescVO.setKeyParaList(keyParaList);
                }
                if (i == contentList.size() - 1) {
                    keyWithDescVOList.add(keyWithDescVO);
                }
            }
        }
        return keyWithDescVOList;
    }

    //将句子转换成关键词VO
    private static List<AdminQuestionKeyWordVO> convertSentenceList2KeyWordList(List<String> keyWordParaList) {
        LinkedList<AdminQuestionKeyWordVO> adminQuestionKeyWordVOS = new LinkedList<>();

        if (CollectionUtils.isNotEmpty(keyWordParaList)) {
            for (String keyWordPara : keyWordParaList) {

                String[] keyWordSentenceArray = keyWordPara.split("。");
                List<String> keyWordSentenceList = Arrays.asList(keyWordSentenceArray);
                AdminQuestionKeyWordVO adminQuestionKeyWordVO = new AdminQuestionKeyWordVO();
                LinkedList<AdminQuestionKeyWordVO> similarKeyWordVOList = new LinkedList<>();
                if (CollectionUtils.isNotEmpty(keyWordSentenceList)) {

                    for (int i = 0; i < keyWordSentenceList.size(); i++) {
                        String keyWordSentence = keyWordSentenceList.get(i);
                        if (StringUtils.isNotEmpty(keyWordSentence) && keyWordSentence.length() > 3) {
                            Matcher keyWordMatcher = wordPattern.matcher(keyWordSentence);
                            List<String> keyWordList = new LinkedList<>();
                            while (keyWordMatcher.find()) {
                                keyWordList.add(keyWordMatcher.group(2));
                            }

                            //分数
                            List<Double> scoreList = new LinkedList<>();
                            Matcher scoreMatcher = scorePattern.matcher(keyWordSentence);
                            while (scoreMatcher.find()) {
                                scoreList.add(Double.parseDouble(scoreMatcher.group(2)));
                            }

                            //关键词
                            if (i == 0) {
                                if (keyWordList.size() == 0) {
                                    throw new BizException(ErrorResult.create(1000580, "文档格式有误，请检查后再尝试导入。关键字提示:" + keyWordSentenceList.get(0)));
                                }
                                adminQuestionKeyWordVO.setItem(keyWordList.get(0));
                                if (CollectionUtils.isEmpty(scoreList)) {
                                    throw new BizException(ErrorResult.create(1000580, "文档格式有误，请检查后再尝试导入。关键字提示:" + keyWordSentenceList.get(0)));
                                }
                                adminQuestionKeyWordVO.setScore(scoreList.get(0));
                                if (keyWordList.size() > 1) {
                                    adminQuestionKeyWordVO.setSplitWords(keyWordList.subList(1, keyWordList.size()));
                                }
                            } else {
                                //近义词
                                AdminQuestionKeyWordVO similarKeyWordVO = new AdminQuestionKeyWordVO();
                                if (keyWordList.size() == 0) {
                                    throw new BizException(ErrorResult.create(1000580, "文档格式有误，请检查后再尝试导入。关键字提示:" + keyWordSentenceList.get(i - 1)));
                                }
                                similarKeyWordVO.setItem(keyWordList.get(0));
//                                log.info(similarKeyWordVO.getItem()+"==========");
                                if (CollectionUtils.isNotEmpty(scoreList)) {
                                    similarKeyWordVO.setScore(scoreList.get(0));
                                } else {
                                    throw new BizException(ErrorResult.create(1000580, "分数解析有误，请检查后再尝试导入。关键字提示:" + similarKeyWordVO.getItem()));
                                }
                                if (keyWordList.size() > 1) {
                                    similarKeyWordVO.setSplitWords(keyWordList.subList(1, keyWordList.size()));
                                }
                                similarKeyWordVOList.add(similarKeyWordVO);
                            }
                        }
                    }
                    adminQuestionKeyWordVO.setSimilarWordVOList(similarKeyWordVOList);
                }
                adminQuestionKeyWordVOS.add(adminQuestionKeyWordVO);
            }
        }
        return adminQuestionKeyWordVOS;
    }


    //描述的句子转成描述VO(描述+分数)
    private static AdminQuestionDescVO convertSentence2Desc(String descSentence) {
        String desc = "";
        //关键词
        Matcher descMatcher = descPattern.matcher(descSentence);
        while (descMatcher.find()) {
            desc = descMatcher.group(2);
            if (StringUtils.isNotEmpty(desc)) {
                desc = desc.substring(0, desc.length() - 1);
            }
        }

        //分数
        Double score = 0D;
        Matcher scoreMatcher = scorePattern.matcher(descSentence);
        while (scoreMatcher.find()) {
            score = Double.parseDouble(scoreMatcher.group(2));
        }

        return AdminQuestionDescVO.builder()
                .item(desc)
                .score(score)
                .build();
    }

    /**
     * 文件预处理转换成段落list
     *
     * @param file
     * @return
     */
    private List<String> filePreHandle(MultipartFile file) {
        //获取文件名
        String fileName = file.getOriginalFilename();
        System.out.println(file.getContentType());
        ;
        if (!fileName.endsWith("docx")) {
            log.warn("文件类型错误，仅支持docx文件导入。文件名称：{}", fileName);
            throw new BizException(EssayErrors.ERROR_FILE_TYPE);
        }
        List<String> contentList = new ArrayList<>();
        try {
            //读取文件内容
            XWPFDocument xwpfDocument = new XWPFDocument(file.getInputStream());
            Iterator<XWPFParagraph> iterator = xwpfDocument.getParagraphsIterator();
            while (iterator.hasNext()) {
                XWPFParagraph next = iterator.next();
                if (null != next && StringUtils.isNotEmpty(next.getText())) {
                    contentList.add(next.getText().replace(" ", ""));
                }
            }

        } catch (IOException e) {
            throw new BizException(EssayErrors.PDF_OBJ_NULL);
        }

        return contentList;
    }


    //    1为全篇，2为首段，3为开头或中间，4为中间，5为中间或结尾，6为结尾
    private static int getKeyPhrasePosition(String keyPhrasePara) {
        String positionStr = "";
        Matcher descMatcher = positionPattern.matcher(keyPhrasePara);
        while (descMatcher.find()) {
            positionStr = descMatcher.group(2);
        }
        int position = 0;
        if ("全篇".equals(positionStr)) {
            position = 1;
        } else if ("开头".equals(positionStr)) {
            position = 2;
        } else if ("开头或中间".equals(positionStr)) {
            position = 3;
        } else if ("中间".equals(positionStr)) {
            position = 4;
        } else if ("中间或结尾".equals(positionStr)) {
            position = 5;
        } else if ("结尾".equals(positionStr)) {
            position = 6;
        }
        return position;
    }


}
