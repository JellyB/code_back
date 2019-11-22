package com.huatu.tiku.enums;

import com.google.common.collect.Lists;
import com.huatu.tiku.constants.teacher.ExportType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 导出word -- 试题元素枚举
 * Created by huangqingpeng on 2018/8/14.
 */
public enum QuestionElementEnum {
    ;

    /**
     * 属性元素枚举
     */
    @Getter
    @AllArgsConstructor
    public enum ElementEnum {
        SORT("sort", QuestionFieldEnum.COMMON, "", LocationEnum.UN_KNOW, LocationEnum.UN_KNOW),
        TYPE("type", QuestionFieldEnum.COMMON, "【题型】", LocationEnum.OLD_LINE, LocationEnum.UN_KNOW),
        MATERIAL("material", QuestionFieldEnum.COMMON, "【资料】", LocationEnum.OTHER, LocationEnum.OTHER),
        QUESTION_ID("questionId", QuestionFieldEnum.COMMON, "【试题ID】", LocationEnum.OLD_LINE, LocationEnum.UN_KNOW),
        STEM("stem", QuestionFieldEnum.STEM, ". ", LocationEnum.OTHER, LocationEnum.OTHER),
        CHOICE("choices", QuestionFieldEnum.STEM, ".", LocationEnum.OTHER, LocationEnum.OTHER),
        ANSWER("answer", QuestionFieldEnum.ANSWER, "【答案】", LocationEnum.OLD_LINE, LocationEnum.OLD_LINE),
        ANSWER_COMMENT("answerComment", QuestionFieldEnum.ANSWER, "【参考答案】", LocationEnum.NEW_LINE, LocationEnum.NEW_LINE),
        KNOWLEDGE("pointName", QuestionFieldEnum.ANSWER, "【三级知识点】", LocationEnum.OLD_LINE, LocationEnum.OLD_LINE),
        SOURCE("source", QuestionFieldEnum.ANSWER, "【来源】", LocationEnum.OLD_LINE, LocationEnum.OLD_LINE),
        ANALYSIS("analysis", QuestionFieldEnum.ANSWER, "【解析】", LocationEnum.NEW_LINE, LocationEnum.NEW_LINE),
        EXTEND("extend", QuestionFieldEnum.ANSWER, "【拓展】", LocationEnum.NEW_LINE, LocationEnum.NEW_LINE),
        ACCURACY("accuracy", QuestionFieldEnum.ANSWER, "【正确率】", LocationEnum.OLD_LINE, LocationEnum.UN_KNOW),
        TRAIN_TIME("trainTime", QuestionFieldEnum.ANSWER, "【答题次数】", LocationEnum.OLD_LINE, LocationEnum.UN_KNOW),;
        /**
         * Map获取数据的key值
         */
        private String key;
        /**
         * 1题目内容2解析内容
         */
        private QuestionFieldEnum field;
        /**
         * 数据模块的处理标签
         */
        private String name;
        /**
         * 标签与数据内容的位置关系
         */
        private LocationEnum location;
        /**
         * 标签与数据内容的位置关系
         */
        private LocationEnum pdfLocation;

        public boolean equals(ElementEnum elementEnum) {
            return this.getKey().equals(elementEnum.getKey());
        }
    }

    /**
     * 试题内容处理方式
     */
    @Getter
    @AllArgsConstructor
    public enum QuestionFieldEnum {
        //全部内容
        COMMON(ExportType.PAPER_WORD_TYPE_ALL, "共有内容"),
        //题目内容
        STEM(ExportType.PAPER_WORD_TYPE_SIDE_STEM, "题目内容"),
        //解析内容
        ANSWER(ExportType.PAPER_WORD_TYPE_SIDE_ANSWER, "解析内容"),;
        private int key;
        private String value;

        public boolean equals(QuestionFieldEnum questionFieldEnum) {
            return this.getKey() == questionFieldEnum.getKey();
        }

        public static QuestionFieldEnum create(int exportType) {
            for (QuestionFieldEnum questionFieldEnum : QuestionFieldEnum.values()) {
                if (exportType == questionFieldEnum.getKey()) {
                    return questionFieldEnum;
                }
            }
            return QuestionFieldEnum.COMMON;
        }
    }

    /**
     * 试题内容处理方式
     */
    @Getter
    @AllArgsConstructor
    public enum LocationEnum {
        NEW_LINE(1, "换行"),
        OLD_LINE(2, "不换行"),
        OTHER(3, "自定义"),
        UN_KNOW(4, "附属参数"),;
        private int key;
        private String value;

        public boolean equals(LocationEnum locationEnum) {
            return this.getKey() == locationEnum.getKey();
        }
    }

    /**
     * 不同类型试题处理顺枚举
     */
    @Getter
    @AllArgsConstructor
    public enum QuestionOperateEnum {
        OBJECTIVE(QuestionInfoEnum.QuestionSaveTypeEnum.OBJECTIVE, Lists.newArrayList(
                ElementEnum.TYPE,
                ElementEnum.QUESTION_ID,
                ElementEnum.SORT,
                ElementEnum.STEM,
                ElementEnum.CHOICE,
                ElementEnum.ANSWER,
                ElementEnum.ACCURACY,
                ElementEnum.TRAIN_TIME,
                ElementEnum.KNOWLEDGE,
                ElementEnum.ANALYSIS,
                ElementEnum.EXTEND,
                ElementEnum.SOURCE)),
        JUDGE(QuestionInfoEnum.QuestionSaveTypeEnum.JUDGE, Lists.newArrayList(
                ElementEnum.TYPE,
                ElementEnum.QUESTION_ID,
                ElementEnum.SORT,
                ElementEnum.STEM,
                ElementEnum.CHOICE,
                ElementEnum.ANSWER,
                ElementEnum.ACCURACY,
                ElementEnum.TRAIN_TIME,
                ElementEnum.KNOWLEDGE,
                ElementEnum.ANALYSIS,
                ElementEnum.EXTEND,
                ElementEnum.SOURCE)),
        SUBJECTIVE(QuestionInfoEnum.QuestionSaveTypeEnum.SUBJECTIVE, Lists.newArrayList(
                ElementEnum.TYPE,
                ElementEnum.QUESTION_ID,
                ElementEnum.SORT,
                ElementEnum.STEM,
                ElementEnum.ANSWER_COMMENT,
                ElementEnum.SOURCE)),
        COMPOSITE(QuestionInfoEnum.QuestionSaveTypeEnum.COMPOSITE, Lists.newArrayList(
                ElementEnum.TYPE,
                ElementEnum.QUESTION_ID,
                ElementEnum.SORT,
                ElementEnum.MATERIAL,
                ElementEnum.SOURCE)),
        UNKNOWN_TYPE(QuestionInfoEnum.QuestionSaveTypeEnum.UNKNOWN_TYPE, Lists.newArrayList()),;

        private QuestionInfoEnum.QuestionSaveTypeEnum type;
        private List<ElementEnum> value;

        public static QuestionOperateEnum create(QuestionInfoEnum.QuestionSaveTypeEnum type) {
            for (QuestionOperateEnum questionOperateEnum : QuestionOperateEnum.values()) {
                if (questionOperateEnum.getType().equals(type)) {
                    return questionOperateEnum;
                }
            }
            return UNKNOWN_TYPE;
        }
    }


    @Getter
    @AllArgsConstructor
    public enum TextStyleEnum{
        STRONG(1,"加粗", Pattern.compile("<strong>(((?!</strong>).)*)</strong>")),
        UNDERLINE(2,"下划线",Pattern.compile("<u>(((?!</u>).)*)</u>"));
        private int key;
        private String value;
        private Pattern pattern;
    }
}
