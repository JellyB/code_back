package com.huatu.tiku.teacher.util;

import com.google.common.collect.Maps;
import com.huatu.tiku.enums.QuestionWeightEnum;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.QuestionMeta;
import org.apache.commons.collections.MapUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class QuestionWeightUtil {
    public static final Function<QuestionMeta, Integer> getPercent = (meta -> meta.getPercents()[meta.getRindex()]);

    public static Boolean isMetaValid(QuestionMeta meta) {
        if (null == meta) {
            return false;
        }
        if (!isValid(QuestionWeightEnum.WeightEnum.DO_NUM, meta.getCount())) {
            return false;
        }
        if (!isValid(QuestionWeightEnum.WeightEnum.WRONG_PERCENT, 100 - getPercent.apply(meta))) {
            return false;
        }
        return true;
    }


    public static Boolean isAttrValid(Question question) {
        if (null == question) {
            return false;
        }
        if (!(question instanceof GenericQuestion)) {
            return false;
        }
        if (!isValid(QuestionWeightEnum.WeightEnum.MODE, question.getMode())) {
            return false;
        }
        if (!isValid(QuestionWeightEnum.WeightEnum.YEAR, question.getYear())) {
            return false;
        }
        return true;
    }

    private static boolean isValid(QuestionWeightEnum.WeightEnum weightEnum, int count) {
        return weightEnum.getFlag().apply(count);
    }

    public static Double getWeight(Question question) {
        HashMap<QuestionWeightEnum.WeightEnum, Double> map = Maps.newHashMap();
        Boolean attrValid = isAttrValid(question);
        boolean metaValid = question instanceof GenericQuestion && isMetaValid(((GenericQuestion) question).getMeta());
        for (QuestionWeightEnum.WeightEnum value : QuestionWeightEnum.WeightEnum.values()) {
            switch (value) {
                case MODE:
                    if (attrValid) {
                        map.put(value, value.getPercentEnum().apply(question.getMode()));
                    }
                case YEAR:
                    if (attrValid) {
                        map.put(value, value.getPercentEnum().apply(question.getYear()));
                    }
                case DO_NUM:
                    if (attrValid && metaValid) {
                        Integer percent = getPercent.apply(((GenericQuestion) question).getMeta());
                        map.put(value, value.getPercentEnum().apply(percent));
                    }
                case WRONG_PERCENT:
                    if (attrValid && metaValid) {
                        map.put(value, value.getPercentEnum().apply(((GenericQuestion) question).getMeta().getCount()));
                    }
            }
        }
        return sumWeight(map);
    }

    private static Double sumWeight(HashMap<QuestionWeightEnum.WeightEnum, Double> map) {
        if (null == map || map.isEmpty()) {
            return 0d;
        }
        return map.values().stream().mapToDouble(Double::doubleValue).sum();
    }
}
