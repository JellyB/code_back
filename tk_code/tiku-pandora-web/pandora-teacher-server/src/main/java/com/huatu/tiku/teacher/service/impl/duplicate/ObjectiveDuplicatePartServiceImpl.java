package com.huatu.tiku.teacher.service.impl.duplicate;

import com.google.common.collect.Lists;
import com.huatu.tiku.enums.QuestionInfoEnum;
import com.huatu.tiku.entity.duplicate.ObjectiveDuplicatePart;
import com.huatu.tiku.response.question.DuplicatePartResp;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.tiku.teacher.dao.question.ObjectiveDuplicatePartMapper;
import com.huatu.tiku.teacher.service.duplicate.ObjectiveDuplicatePartService;
import com.huatu.tiku.util.html.HtmlConvertUtil;
import com.huatu.tiku.util.question.StringMatch;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2018\5\16 0016.
 */
@Service
public class ObjectiveDuplicatePartServiceImpl extends BaseServiceImpl<ObjectiveDuplicatePart> implements ObjectiveDuplicatePartService {

    public ObjectiveDuplicatePartServiceImpl() {
        super(ObjectiveDuplicatePart.class);
    }

    @Autowired
    ObjectiveDuplicatePartMapper objectiveDuplicatePartMapper;

    @Override
    @Transactional
    public void insertWithFilter(ObjectiveDuplicatePart objectiveDuplicatePart) {
        objectiveDuplicatePart.setId(null);
        assertFilterAttr(objectiveDuplicatePart);
        save(objectiveDuplicatePart);
    }

    /**
     * 给筛选字段赋值
     *
     * @param objectiveDuplicatePart
     */
    private void assertFilterAttr(ObjectiveDuplicatePart objectiveDuplicatePart) {
        objectiveDuplicatePart.setStemFilter(StringMatch.replaceNotChinese(objectiveDuplicatePart.getStem()));
        /**
         * 针对选项，建议去掉选项内容前的A.B.类似的字样
         */
        String choiceFilter = StringMatch.replaceNotChinese(objectiveDuplicatePart.getChoices());
        choiceFilter = choiceFilter.replaceAll(HtmlConvertUtil.CHOICE_HEAD_FLAG, "");
        objectiveDuplicatePart.setChoicesFilter(choiceFilter);
        objectiveDuplicatePart.setAnalysisFilter(StringMatch.replaceNotChinese(objectiveDuplicatePart.getAnalysis()));
        objectiveDuplicatePart.setExtendFilter(StringMatch.replaceNotChinese(objectiveDuplicatePart.getExtend()));
        objectiveDuplicatePart.setJudgeBasisFilter(StringMatch.replaceNotChinese(objectiveDuplicatePart.getJudgeBasis()));
    }

    @Override
    @Transactional
    public void updateWithFilter(ObjectiveDuplicatePart objectiveDuplicatePart) {
        assertFilterAttr(objectiveDuplicatePart);
        save(objectiveDuplicatePart);
    }

    @Override
    public List<DuplicatePartResp> selectByMyExample(String choiceContent, String stem, String analysis, String extend, Integer questionType) {
        if (StringMatch.isAllBlank(choiceContent, stem, analysis, extend)) {
            return Lists.newArrayList();
        }
        Example example = new Example(ObjectiveDuplicatePart.class);
        example.and().andEqualTo("status", 1);
        String stemFilter = StringMatch.replaceNotChinese(stem);
        if (StringUtils.isNotBlank(stemFilter)) {
            example.and().andLike("stemFilter", "%" + stemFilter.trim() + "%");
        }
        String analysisFilter = StringMatch.replaceNotChinese(analysis);
        if (StringUtils.isNotBlank(analysisFilter)) {
            example.and().andLike("analysisFilter", "%" + analysisFilter.trim() + "%");
        }
        String extendFilter = StringMatch.replaceNotChinese(extend);
        if (StringUtils.isNotBlank(extendFilter)) {
            example.and().andLike("extendFilter", "%" + extendFilter.trim() + "%");
        }
        /**
         * 选项在额外去掉多余的选项标识
         */
        String choiceContentFilter = StringMatch.replaceNotChinese(choiceContent);
        if (StringUtils.isNotBlank(choiceContentFilter)) {
            String choiceFinalFilter = choiceContentFilter.replaceAll(HtmlConvertUtil.CHOICE_HEAD_FLAG, "");
            example.and().andLike("choicesFilter", "%" + choiceFinalFilter.trim() + "%");
        }
        if (questionType != null || questionType.intValue() != -1) {
            example.and().andEqualTo("questionType", questionType);
        }
        List<ObjectiveDuplicatePart> objectiveDuplicateParts = selectByExample(example);

        if (CollectionUtils.isEmpty(objectiveDuplicateParts)) {
            return Lists.newArrayList();
        }
        //做出每个查询结果和查询条件的相似度值
        Map<Long, Double> percentMap = objectiveDuplicateParts.stream().collect(Collectors.toMap(i -> i.getId(), i -> getSimilarPercent(i, stemFilter, choiceContentFilter, analysisFilter, extendFilter)));
        //按照相似度值倒序排列
        objectiveDuplicateParts.sort(Comparator.comparingDouble(i -> -percentMap.get(i.getId())));
        //取前1000的数值
        if (objectiveDuplicateParts.size() > 1000) {
            objectiveDuplicateParts = objectiveDuplicateParts.subList(0, 1000);
        }
        return objectiveDuplicateParts.stream().map(i -> {
            DuplicatePartResp duplicatePartResp = new DuplicatePartResp();
            BeanUtils.copyProperties(i, duplicatePartResp);
            duplicatePartResp.setDuplicateId(i.getId());
            duplicatePartResp.setChoices(HtmlConvertUtil.parseChoices(i.getChoices()));
            if (StringUtils.isNotBlank(duplicatePartResp.getStem())) {
                duplicatePartResp.setStemShow(HtmlConvertUtil.span2Img(duplicatePartResp.getStem(), false));
            }
            if (StringUtils.isNotBlank(duplicatePartResp.getAnalysis())) {
                duplicatePartResp.setAnalysisShow(HtmlConvertUtil.span2Img(duplicatePartResp.getAnalysis(), false));
            }
            if (StringUtils.isNotBlank(duplicatePartResp.getExtend())) {
                duplicatePartResp.setExtendShow(HtmlConvertUtil.span2Img(duplicatePartResp.getExtend(), false));
            }
            if (CollectionUtils.isNotEmpty(duplicatePartResp.getChoices())) {
                duplicatePartResp.setChoicesShow(HtmlConvertUtil.parseChoices(i.getChoices()).stream().map(j -> HtmlConvertUtil.span2Img(j, false)).collect(Collectors.toList()));
            }
            //如果是判断题，改变答案为0/1
            if (questionType.intValue() == QuestionInfoEnum.QuestionTypeEnum.JUDGE.getCode()) {
                duplicatePartResp.setAnswer("A".equals(i.getAnswer()) ? "1" : "0");
            }
            /**
             * update by lizhenjuan
             * 前端根据题型，需要copy不同的属性
             */
            duplicatePartResp.setQuestionType(questionType);
            return duplicatePartResp;
        }).collect(Collectors.toList());
    }

    @Override
    public HashMap<String, Object> findByQuestionId(Long questionId) {
        return objectiveDuplicatePartMapper.findByQuestionId(questionId);
    }

    /**
     * 相似度倒序排序
     *
     * @param objectiveDuplicatePart
     * @param targets
     * @param
     * @return
     */
    private double getSimilarPercent(ObjectiveDuplicatePart objectiveDuplicatePart, String... targets) {
        if (ArrayUtils.isEmpty(targets)) {
            return 0;
        }
        double percent = 0d;
        for (String target : targets) {
            if (StringUtils.isNotBlank(target)) {
                double p = StringMatch.getSimilar(target, objectiveDuplicatePart.getStemFilter(), objectiveDuplicatePart.getAnalysisFilter(), objectiveDuplicatePart.getExtendFilter(), objectiveDuplicatePart.getChoicesFilter());
                if (p > 0) {
                    percent += p;
                }
            }
        }
        return percent;
    }
}
