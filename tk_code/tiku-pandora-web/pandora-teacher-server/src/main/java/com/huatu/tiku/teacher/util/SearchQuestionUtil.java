package com.huatu.tiku.teacher.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.enums.QuestionInfoEnum;
import com.huatu.tiku.teacher.enums.ActivityTypeAndStatus;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.GenericSubjectiveQuestion;
import com.huatu.ztk.question.bean.Question;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by huangqingpeng on 2018/8/27.
 */
public class SearchQuestionUtil {

    //答题选项
    public static final List<String> optionList = Lists.newArrayList();

    /**
     * 查询题源涉及到的活动类型集合（哪些活动类型试卷计入试题来源中）
     */
    public static final List<ActivityTypeAndStatus.ActivityTypeEnum> SEARCH_SOURCE_ACTIVITY_TYPE = Lists.newArrayList(
            ActivityTypeAndStatus.ActivityTypeEnum.TRUE_PAPER,
            ActivityTypeAndStatus.ActivityTypeEnum.REGULAR_PAPER,
            ActivityTypeAndStatus.ActivityTypeEnum.MATCH
    );

    static {
        for (char c = 'A'; c <= 'Z'; c++) {
            optionList.add(c + "");
        }
    }

    public static Map transToMap(Question question) {
        if (question instanceof GenericQuestion) {
            return checkGenericQuestion((GenericQuestion) question);
        } else if (question instanceof GenericSubjectiveQuestion) {
            return checkGenericSubjectiveQuestion((GenericSubjectiveQuestion) question);
        }
        return Maps.newHashMap();
    }




    /**
     * @param target
     * @return
     */
    private static Map checkGenericSubjectiveQuestion(GenericSubjectiveQuestion target) {
        Map mapData = Maps.newHashMap();
        int type = target.getType();
        QuestionInfoEnum.QuestionDuplicateTypeEnum typeEnum = QuestionInfoEnum.getDuplicateTypeByQuestionType(type);
        QuestionInfoEnum.QuestionSaveTypeEnum saveTypeEnum = QuestionInfoEnum.getSaveTypeByQuestionType(type);
        if (typeEnum.equals(QuestionInfoEnum.QuestionDuplicateTypeEnum.JUDGE_OBJECT) ||
                saveTypeEnum.equals(QuestionInfoEnum.QuestionSaveTypeEnum.COMPOSITE)) {
            return mapData;
        }
        mapData.put("id", target.getId());//id
        mapData.put("type", target.getType());//试题类型
        mapData.put("from", StringUtils.trimToEmpty(target.getFrom()));//来源
        mapData.put("material", wrapperMaterials(target.getMaterials()));//材料
        mapData.put("year", target.getYear());
        mapData.put("area", Lists.newArrayList(target.getArea()));
        mapData.put("subject", target.getSubject());
        mapData.put("catgory", target.getSubject());
        mapData.put("stem", getContentText(target.getStem()));
        mapData.put("parent", target.getParent());
        mapData.put("createTime", target.getCreateTime());
        mapData.put("difficult", target.getDifficult());
        mapData.put("mode", target.getMode());
        mapData.put("referAnalysis", target.getReferAnalysis());//参考解析
        return mapData;
    }

    private static Map checkGenericQuestion(GenericQuestion target) {
        Map mapData = Maps.newHashMap();
        int type = target.getType();
        QuestionInfoEnum.QuestionDuplicateTypeEnum duplicateTypeEnum = QuestionInfoEnum.getDuplicateTypeByQuestionType(type);
        if (!duplicateTypeEnum.equals(QuestionInfoEnum.QuestionDuplicateTypeEnum.JUDGE_OBJECT)) {
            return mapData;
        }
        mapData.put("id", target.getId());//id
        mapData.put("type", target.getType());//试题类型
        mapData.put("from", StringUtils.trimToEmpty(target.getFrom()));//来源
        mapData.put("material", getContentText(target.getMaterial()));//材料
        mapData.put("year", target.getYear());
        mapData.put("area", Lists.newArrayList(target.getArea()));
        mapData.put("subject", target.getSubject());
        mapData.put("catgory", target.getSubject());
        mapData.put("stem", getContentText(target.getStem()));
        mapData.put("choices", wrapperChoices(target.getChoices()));
        mapData.put("analysis", getContentText(target.getAnalysis()));
        mapData.put("parent", target.getParent());
        mapData.put("createTime", target.getCreateTime());
        mapData.put("difficult", target.getDifficult());
        mapData.put("mode", target.getMode());
        mapData.put("points", target.getPoints());
        return mapData;
    }

    /**
     * 将内容里面的标签去掉
     */
    private static String getContentText(String source) {
        if (StringUtils.isBlank(source)) {//为空则直接返回空字符串
            return "";
        }
        //获取html里面的文本内容
//        return StringMatch.replaceNotChinese(source);
        /**
         * 不做处理
         */
        return source;
    }

    /**
     * 将选项转换为带有A，B，C，D
     */
    private static String wrapperChoices(List<String> choices) {
        StringBuilder results = new StringBuilder();
        for (int i = 0; i < choices.size(); i++) {//遍历选项，把他们拼接到一块
            results.append(optionList.get(i));
            results.append(":");//：作为选项喝内容的分隔符
            results.append(getContentText(choices.get(i)));
            if (i < choices.size() - 1) {//最后一个不用添加空格
                results.append("&nbsp;");//添加空格
            }
        }
        return results.toString();
    }

    /**
     * 转换主观题材料
     */
    private static String wrapperMaterials(List<String> materials) {
        return StringUtils.join(materials, "\n");
    }
}
