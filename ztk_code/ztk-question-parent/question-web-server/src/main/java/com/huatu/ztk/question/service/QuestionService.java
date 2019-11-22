package com.huatu.ztk.question.service;


import com.huatu.common.consts.TerminalType;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.CompositeQuestion;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.dao.QuestionDao;
import com.huatu.ztk.question.util.PageUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 试题service层
 * Created by shaojieyue
 * Created time 2016-04-24 20:41
 */

@Service
public class QuestionService {
    public static final Logger logger = LoggerFactory.getLogger(QuestionService.class);

    @Autowired
    private QuestionDubboService questionDubboService;

    @Autowired
    private QuestionDao questionDao;

    /**
     * 获取试题
     *
     * @param id
     * @return
     */
    public Question findById(int id, String cv) {
        final Question question = questionDubboService.findById(id);
        if (question == null) {//试题不存在
            return null;
        }
        if (!StringUtils.isEmpty(cv) && "2.2".compareTo(cv) >= 0) {
            formatQuestion(question);//更改试题标签
        }
        return question;
    }

    /**
     * 获取批量试题
     *
     * @param ids
     * @param cv
     * @return
     */
    public List<Question> findBath(List<Integer> ids, String cv) {
        final List<Question> questions = questionDubboService.findBath(ids);
        if (CollectionUtils.isNotEmpty(questions)) {//试题存在
            if (!StringUtils.isEmpty(cv) && "2.2".compareTo(cv) >= 0) {  //b版本小于等于2.2时，将标签《u》替换为<underline>
                for (Question question : questions) {
                    formatQuestion(question);
                }
            }
        }
        return questions;
    }

    /**
     * 格式化试题
     *
     * @param question
     */
    private void formatQuestion(Question question) {
        if (question instanceof GenericQuestion) {
            GenericQuestion genericQuestion = (GenericQuestion) question;
            //格式化数据
            genericQuestion.setStem(convertLableU2Underline(genericQuestion.getStem()));
            genericQuestion.setAnalysis(convertLableU2Underline(genericQuestion.getAnalysis()));
            genericQuestion.setMaterial(convertLableU2Underline(genericQuestion.getMaterial()));
            for (int i = 0; i < genericQuestion.getChoices().size(); i++) {
                String choice = convertLableU2Underline(genericQuestion.getChoices().get(i));
                genericQuestion.getChoices().set(i, choice);
            }
        } else if (question instanceof CompositeQuestion) {
            CompositeQuestion compositeQuestion = (CompositeQuestion) question;
            compositeQuestion.setMaterial(convertLableU2Underline(compositeQuestion.getMaterial()));
        }
    }

    /**
     * 转换标签
     *
     * @param content
     * @return
     */
    private String convertLableU2Underline(String content) {
        if (StringUtils.isEmpty(content)) {
            return content;
        }
        if (content.contains("<u>")) {
            content = content.replaceAll("<u>", "<underline>")
                    .replaceAll("</u>", "</underline>");
        }
        return content;
    }

    /**
     * 通过type 查询 所有的question 数据
     *
     * @param type
     * @param mode
     * @return
     */
    public List<Question> findByType(Integer type, Integer mode) {
        List<Question> questionList = questionDao.findByType(type, mode);
        logger.info("questionList.size = " + questionList.size());
        List<Integer> ids = questionList.stream().map(Question::getId).collect(Collectors.toList());
        List<Question> list = questionDubboService.findBath(ids);
        return list;
    }

    /**
     * 批量查询
     *
     * @param idList
     * @return
     */
    public List<Question> findBatchV3(List<Integer> idList) {
        List<Question> questions = questionDubboService.findBatchV3(idList);

        //试题存在
        if (CollectionUtils.isNotEmpty(questions)) {
            for (Question question : questions) {
                //处理试题格式
                formatQuestion(question);
            }
        }

        return questions;
    }

    /**
     * 多条件分页查询
     *
     * @param type
     * @param difficult
     * @param mode
     * @param points
     * @return
     */
    public PageUtil<Question> findByConditionV3(Integer type, Integer difficult, Integer mode,
                                                String points, String content, String ids, Integer page, Integer pageSize,String subject) {
        logger.info("=======开始调用dubbo服务=====");
        return questionDubboService.findByConditionV3(type, difficult, mode, points, content, ids, page, pageSize,subject);


    }

    /**
     * ios单独处理 "&#" 字符串的显示
     *
     * @param questions
     * @return
     */
    public void convertSpecialTag(List<Question> questions) {

        final List<Question> afterTranslateResult = questions.parallelStream().map(question -> {
            if (question instanceof GenericQuestion) {
                GenericQuestion genericQuestion = (GenericQuestion) question;
                //格式化数据
                genericQuestion.setStem(convertSpecialTag(genericQuestion.getStem()));
                genericQuestion.setAnalysis(convertSpecialTag(genericQuestion.getAnalysis()));
                genericQuestion.setMaterial(convertSpecialTag(genericQuestion.getMaterial()));
                for (int i = 0; i < genericQuestion.getChoices().size(); i++) {
                    String choice = convertSpecialTag(genericQuestion.getChoices().get(i));
                    genericQuestion.getChoices().set(i, choice);
                }
            } else if (question instanceof CompositeQuestion) {
                CompositeQuestion compositeQuestion = (CompositeQuestion) question;
                compositeQuestion.setMaterial(convertSpecialTag(compositeQuestion.getMaterial()));
            }
            return question;
        }).collect(Collectors.toList());
        //logger.info("处理符号之后显示内容：{}", afterTranslateResult);
    }


    /**
     * ios转换特殊字符
     *
     * @param content
     * @return
     */
    private String convertSpecialTag(String content) {
        if (StringUtils.isEmpty(content)) {
            return content;
        }
        if (content.contains("&#")) {
            content = content.replaceAll("&#", "&<span></span>#");
        }
        return content;
    }

    public List<Question> findBatchV3WithTerminal(List<Integer> idList, int terminal) {
        List<Question> batchV3 = findBatchV3(idList);
        if(terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD){
            batchV3.forEach(this::handlerIosPointName);
        }else if(terminal == TerminalType.PC){
            batchV3.forEach(this::handlerPCPointName);
        }
        return batchV3;
    }

    private void handlerPCPointName(Question question) {
        if(question instanceof GenericQuestion){
            List<String> pointNames = ((GenericQuestion) question).getPointsName().stream().map(i -> i.replaceAll(",", " ")).collect(Collectors.toList());
            ((GenericQuestion) question).setPointsName(pointNames);
        }
    }

    private void handlerIosPointName(Question question) {
        if(question instanceof GenericQuestion){
            List<String> pointNames = ((GenericQuestion) question).getPointsName().stream().map(i -> i.replaceAll(",", "、")).collect(Collectors.toList());
            ((GenericQuestion) question).setPointsName(pointNames);
        }
    }

    public List<Question> findBathWithTerminal(List idList, String cv, Integer terminal) {
        List<Question> bath = findBath(idList, cv);
        if(terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD){
            bath.forEach(this::handlerIosPointName);
        }else if(terminal == TerminalType.PC){
            bath.forEach(this::handlerPCPointName);
        }
        return bath;
    }

    public Question findByIdWithTerminal(int id, String cv, Integer terminal) {
        Question question = findById(id, cv);
        if(terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD){
            handlerIosPointName(question);
        }else if(terminal == TerminalType.PC){
            handlerPCPointName(question);
        }
        return question;
    }
}
