package com.huatu.tiku.match.service.impl.v1.paper;

import com.google.common.collect.Lists;
import com.huatu.tiku.enums.QuestionInfoEnum;
import com.huatu.tiku.match.bo.paper.*;
import com.huatu.ztk.knowledge.bean.Module;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.GenericSubjectiveQuestion;
import com.huatu.ztk.question.bean.Question;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created by lijun on 2019/1/2
 */
final class QuestionUtil {

    /**
     * 试题信息 转换成 答题需要的试题信息
     *
     * @param questionInfo 试题基础信息
     * @return 答题需要的试题信息
     */
    public static QuestionSimpleBo transQuestionInfoToSimpleBo(Question questionInfo) {
        if (null == questionInfo) {
            return null;
        }
        if (questionInfo instanceof GenericQuestion) {
            return transGenericQuestionInfoToSimpleBo((GenericQuestion) questionInfo);
        } else if (questionInfo instanceof GenericSubjectiveQuestion) {
            return transGenericSubjectiveQuestionInfoToSimpleBo((GenericSubjectiveQuestion) questionInfo);
        } else {
            return null;
        }
    }

    /**
     * 试题信息 转成 解析数据
     *
     * @param questionInfo  试题信息
     * @param buildMetaInfo 构建试题统计数据
     * @return 解析所需数据
     */
    public static QuestionSimpleBo transQuestionInfoToAnalysisBo(Question questionInfo, Consumer<QuestionSimpleBo> buildMetaInfo) {
        if (null == questionInfo) {
            return null;
        }
        if (questionInfo instanceof GenericQuestion) {
            return transGenericQuestionInfoToAnalysisBo((GenericQuestion) questionInfo, buildMetaInfo);
        } else if (questionInfo instanceof GenericSubjectiveQuestion) {
            return transGenericSubjectiveQuestionAnalysisBoToAnalysisBo((GenericSubjectiveQuestion) questionInfo, buildMetaInfo);
        } else {
            return null;
        }
    }


    /**
     * 客观题-试题信息 转换成 答题需要的试题信息
     *
     * @param questionInfo 试题基础信息
     * @return 答题需要的试题信息
     */
    private static GenericQuestionSimpleBo transGenericQuestionInfoToSimpleBo(GenericQuestion questionInfo) {
        if (null == questionInfo) {
            return new GenericQuestionSimpleBo();
        }
        GenericQuestionSimpleBo questionSimpleBo = GenericQuestionSimpleBo.builder()
                .id(questionInfo.getId())
                .parentId(questionInfo.getParent())
                .type(questionInfo.getType())
                .materialList(questionInfo.getMaterials())
                .stem(questionInfo.getStem())
                .choiceList(questionInfo.getChoices())
                .build();
        if(StringUtils.isNotBlank(questionInfo.getTeachType())){
            questionSimpleBo.setTeachType(questionInfo.getTeachType());
        }else{
            questionSimpleBo.setTeachType(QuestionInfoEnum.QuestionTypeEnum.create(questionInfo.getType()).getName());
        }
        if (CollectionUtils.isEmpty(questionSimpleBo.getMaterialList()) && StringUtils.isNotBlank(questionInfo.getMaterial())) {
            questionSimpleBo.setMaterialList(Lists.newArrayList(questionInfo.getMaterial()));
        }
        return questionSimpleBo;
    }


    /**
     * 主观题-试题信息 转换成 答题需要的试题信息
     *
     * @param questionInfo 试题基础信息
     * @return 答题需要的试题信息
     */
    private static GenericSubjectiveQuestionSimpleBo transGenericSubjectiveQuestionInfoToSimpleBo(GenericSubjectiveQuestion questionInfo) {
        if (null == questionInfo) {
            return new GenericSubjectiveQuestionSimpleBo();
        }
        GenericSubjectiveQuestionSimpleBo questionSimpleBo = GenericSubjectiveQuestionSimpleBo.builder()
                .id(questionInfo.getId())
                .parentId(questionInfo.getParent())
                .type(QuestionInfoEnum.QuestionTypeEnum.SUBJECTIVE.getCode())       //客户端SUBJECTIVE类型作为不可答的标识，没有其他用途，故将所有新加的主观题题型type都改为SUBJECTIVE
                .materialList(questionInfo.getMaterials())
                .stem(questionInfo.getStem())
                .require(questionInfo.getRequire())
                .answerRequire(questionInfo.getAnswerRequire())
                .minWordCount(questionInfo.getMinWordCount())
                .maxWordCount(questionInfo.getMaxWordCount())
                .build();
        if(StringUtils.isNotBlank(questionInfo.getTeachType())){
            questionSimpleBo.setTeachType(questionInfo.getTeachType());
        }else{
            questionSimpleBo.setTeachType(QuestionInfoEnum.QuestionTypeEnum.create(questionInfo.getType()).getName());
        }
        if (CollectionUtils.isEmpty(questionSimpleBo.getMaterialList()) && StringUtils.isNotBlank(questionInfo.getMaterial())) {
            questionSimpleBo.setMaterialList(Lists.newArrayList(questionInfo.getMaterial()));
        }
        return questionSimpleBo;
    }

    /**
     * 主观题 - 试题信息 转成 解析数据
     *
     * @param questionInfo  试题信息
     * @param buildMetaInfo 构建试题统计数据
     * @return 解析所需数据
     */
    private static GenericQuestionAnalysisBo transGenericQuestionInfoToAnalysisBo(GenericQuestion questionInfo, Consumer<QuestionSimpleBo> buildMetaInfo) {
        if (null == questionInfo) {
            return new GenericQuestionAnalysisBo();
        }
        QuestionSimpleBo questionSimpleBo = transGenericQuestionInfoToSimpleBo(questionInfo);
        GenericQuestionAnalysisBo questionAnalysisBo = new GenericQuestionAnalysisBo();
        BeanUtils.copyProperties(questionSimpleBo, questionAnalysisBo);
        //答案、解析、拓展、知识点信息、来源
        questionAnalysisBo.setAnswer(questionInfo.getAnswer());
        questionAnalysisBo.setAnalysis(questionInfo.getAnalysis());
        questionAnalysisBo.setExtend(questionInfo.getExtend());
        questionAnalysisBo.setPointsName(questionInfo.getPointsName());
        questionAnalysisBo.setSource(questionInfo.getFrom());

        //试题统计信息
        if (null != buildMetaInfo) {
            buildMetaInfo.accept(questionAnalysisBo);
        }
        return questionAnalysisBo;
    }

    /**
     * 客观题 - 试题信息 转成 解析数据
     *
     * @param questionInfo  试题信息
     * @param buildMetaInfo 构建试题统计数据
     * @return 解析所需数据
     */
    private static GenericSubjectiveQuestionAnalysisBo transGenericSubjectiveQuestionAnalysisBoToAnalysisBo(GenericSubjectiveQuestion questionInfo, Consumer<QuestionSimpleBo> buildMetaInfo) {
        if (null == questionInfo) {
            return new GenericSubjectiveQuestionAnalysisBo();
        }
        GenericSubjectiveQuestionSimpleBo questionSimpleBo = transGenericSubjectiveQuestionInfoToSimpleBo(questionInfo);
        final GenericSubjectiveQuestionAnalysisBo questionAnalysisBo = new GenericSubjectiveQuestionAnalysisBo();
        BeanUtils.copyProperties(questionSimpleBo, questionAnalysisBo);
        //赋分说明、参考解析、审题要求、解题思路
        questionAnalysisBo.setScoreExplain(questionInfo.getScoreExplain());
        questionAnalysisBo.setReferAnalysis(questionInfo.getReferAnalysis());
        questionAnalysisBo.setExamPoint(questionInfo.getExamPoint());
        questionAnalysisBo.setSolvingIdea(questionInfo.getSolvingIdea());
        questionAnalysisBo.setExtend(questionInfo.getExtend());
        //试题统计信息
        if (null != buildMetaInfo) {
            buildMetaInfo.accept(questionAnalysisBo);
        }
        return questionAnalysisBo;
    }

    /**
     * 构建试题 在试卷中的模块信息
     *
     * @param questionSimpleBo 试题BO
     * @param paper            试卷
     */
    public static void buildModuleInfo(QuestionSimpleBo questionSimpleBo, Paper paper) {
        do {
            if (null == questionSimpleBo || null == paper) {
                break;
            }
            final List<Integer> questions = paper.getQuestions();
            final List<Module> moduleList = paper.getModules();
            if (CollectionUtils.isEmpty(questions) || CollectionUtils.isEmpty(moduleList)) {
                break;
            }
            int questionIndex = questions.indexOf(questionSimpleBo.getId());
            if (questionIndex < 0) {
                break;
            }
            int moduleCountNum = 0;
            for (int moduleIndex = 0; moduleIndex < moduleList.size(); moduleIndex++) {
                final Module module = moduleList.get(moduleIndex);
                moduleCountNum += module.getQcount();
                if (questionIndex < moduleCountNum) {
                    questionSimpleBo.setModuleName(module.getName());
                    break;
                }
            }

        } while (false);
    }
}
