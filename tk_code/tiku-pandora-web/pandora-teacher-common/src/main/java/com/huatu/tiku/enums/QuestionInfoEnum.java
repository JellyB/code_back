
package com.huatu.tiku.enums;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.entity.question.BaseQuestion;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 题目相关枚举
 */
public enum QuestionInfoEnum {
    ;

    /**
     * 题型 - 业务类型
     */
    @AllArgsConstructor
    @Getter
    public enum QuestionSaveTypeEnum implements EnumCommon {
        /**
         * 试题存储类型（1客观选择类,2判断类,3主观类,4连线类,5填空类,6复合类）--决定试题详细信息的存储表
         */
        OBJECTIVE(1, "客观选择类"),
        JUDGE(2, "判断类"),
        SUBJECTIVE(3, "主观类"),
        LINK(4, "连线类"),
        BLANK(5, "填空类"),
        COMPOSITE(6, "复合类"),
        UNKNOWN_TYPE(-1, "未知"),
        ;

        private int code;
        private String name;


        @Override
        public int getKey() {
            return this.code;
        }

        @Override
        public String getValue() {
            return this.name;
        }

        public boolean equals(QuestionSaveTypeEnum saveTypeEnum) {
            return this.getKey() == saveTypeEnum.getKey();
        }

        public static  QuestionSaveTypeEnum create(int type){
            for (QuestionSaveTypeEnum questionSaveTypeEnum : QuestionSaveTypeEnum.values()) {
                if(questionSaveTypeEnum.getCode()==type){
                    return questionSaveTypeEnum;
                }
            }
            return UNKNOWN_TYPE;
        }
    }

    /**
     * 题型 - 存表类型
     */
    @AllArgsConstructor
    @Getter
    public enum QuestionDuplicateTypeEnum implements EnumCommon {
        /**
         * 试题去重类型
         * 判断客观类：
         */
        JUDGE_OBJECT(1, "判断客观类"),
        COMPOSITE_SUBJECTIVE(2, "主观复合类"),
        UNKNOWN_TYPE(-1, "未知");

        private int code;
        private String name;

        @Override
        public int getKey() {
            return this.code;
        }

        @Override
        public String getValue() {
            return this.name;
        }

        public boolean equals(QuestionDuplicateTypeEnum duplicateTypeEnum) {
            return this.getKey() == duplicateTypeEnum.getKey();
        }
    }

    /**
     * 题型 - 基础类型
     */
    @AllArgsConstructor
    @Getter
    public enum QuestionTypeEnum implements EnumCommon {
        SINGLE(99, "单选题", QuestionSaveTypeEnum.OBJECTIVE, QuestionDuplicateTypeEnum.JUDGE_OBJECT),
        MULTI(100, "多选题", QuestionSaveTypeEnum.OBJECTIVE, QuestionDuplicateTypeEnum.JUDGE_OBJECT),
        INFINITIVE(101, "不定项选择", QuestionSaveTypeEnum.OBJECTIVE, QuestionDuplicateTypeEnum.JUDGE_OBJECT),
        JUDGE(109, "判断题", QuestionSaveTypeEnum.JUDGE, QuestionDuplicateTypeEnum.JUDGE_OBJECT),
        COMPOSITE(105, "复合题", QuestionSaveTypeEnum.COMPOSITE, QuestionDuplicateTypeEnum.COMPOSITE_SUBJECTIVE),
        SUBJECTIVE(106, "单一主观题", QuestionSaveTypeEnum.SUBJECTIVE, QuestionDuplicateTypeEnum.COMPOSITE_SUBJECTIVE),
        SHORT_ANSWER(111, "简答题", QuestionSaveTypeEnum.SUBJECTIVE, QuestionDuplicateTypeEnum.COMPOSITE_SUBJECTIVE),
        DISCRIMINATION(113, "辨析题", QuestionSaveTypeEnum.SUBJECTIVE, QuestionDuplicateTypeEnum.COMPOSITE_SUBJECTIVE),
        MATERIAL_ANALYSIS(114, "材料分析题", QuestionSaveTypeEnum.SUBJECTIVE, QuestionDuplicateTypeEnum.COMPOSITE_SUBJECTIVE),
        SHORT_ESSAY(115, "作文", QuestionSaveTypeEnum.SUBJECTIVE, QuestionDuplicateTypeEnum.COMPOSITE_SUBJECTIVE),
        TEACHING_DESIGN(116, "教学设计题", QuestionSaveTypeEnum.SUBJECTIVE, QuestionDuplicateTypeEnum.COMPOSITE_SUBJECTIVE),
        EXPLAIN_NOUNS(110, "名词解析", QuestionSaveTypeEnum.SUBJECTIVE, QuestionDuplicateTypeEnum.COMPOSITE_SUBJECTIVE),
        COMPOSITE_SUBJECTIVE(107, "复合主观题", QuestionSaveTypeEnum.COMPOSITE, QuestionDuplicateTypeEnum.COMPOSITE_SUBJECTIVE),
        BLANKS(112, "填空题", QuestionSaveTypeEnum.SUBJECTIVE, QuestionDuplicateTypeEnum.COMPOSITE_SUBJECTIVE),
        CASE_ANALYSIS(117, "案例分析题", QuestionSaveTypeEnum.SUBJECTIVE, QuestionDuplicateTypeEnum.COMPOSITE_SUBJECTIVE),
        DISCUSS(118, "论述题", QuestionSaveTypeEnum.SUBJECTIVE, QuestionDuplicateTypeEnum.COMPOSITE_SUBJECTIVE),
        ACTIVITY_DESIGN(119, "活动设计题", QuestionSaveTypeEnum.SUBJECTIVE, QuestionDuplicateTypeEnum.COMPOSITE_SUBJECTIVE),
        ANALYSIS_OF_TEACHING_SITUATIONS(120, "教学情景分析", QuestionSaveTypeEnum.SUBJECTIVE, QuestionDuplicateTypeEnum.COMPOSITE_SUBJECTIVE),
        JUDGEMENT_REASONING(121, "判断说理题", QuestionSaveTypeEnum.SUBJECTIVE, QuestionDuplicateTypeEnum.COMPOSITE_SUBJECTIVE),
        ;


        private int code;
        private String name;
        private QuestionSaveTypeEnum questionSaveTypeEnum;
        private QuestionDuplicateTypeEnum duplicateTypeEnum;

        @Override
        public int getKey() {
            return this.code;
        }

        @Override
        public String getValue() {
            return this.name;
        }

        public boolean equals(QuestionTypeEnum typeEnum) {
            return this.getKey() == typeEnum.getKey();
        }

        public static QuestionTypeEnum create(int questionType){
            QuestionTypeEnum[] values = QuestionTypeEnum.values();
            for (QuestionTypeEnum value : values) {
                if (value.getCode() == questionType){
                    return value;
                }
            }
            return SINGLE;
        }
    }

    /**
     * 试题 - 作废标识
     */
    @AllArgsConstructor
    @Getter
    public enum AvailableEnum implements EnumCommon {
        /**
         * 是否作废-正常状态
         */
        AVAILABLE(1, "正常", "取消作废"),
        /**
         * 是否作废-作废状态
         */
        UNAVAILABLE(2, "作废", "作废"),
        UNKNOWN_FLAG(-1, "非法", "非法参数"),;
        private int code;
        private String name;
        private String desc;

        @Override
        public int getKey() {
            return this.getCode();
        }

        @Override
        public String getValue() {
            return this.getName();
        }

        public boolean equals(AvailableEnum availableEnum) {
            return this.getCode() == availableEnum.getCode();
        }

        /**
         * 根据 code 获得枚举类型
         *
         * @param code
         * @return
         */
        public static AvailableEnum create(int code) {
            AvailableEnum[] enums = AvailableEnum.values();
            for (AvailableEnum availableEnum : enums) {
                if (availableEnum.getCode() == code) {
                    return availableEnum;
                }
            }
            return AvailableEnum.UNKNOWN_FLAG;
        }
    }

    @AllArgsConstructor
    @Getter
    public enum QuestionPartTypeEnum implements  EnumCommon{
        SINGLE(1,"单题"),
        COMPOSITE(2,"复合题"),
        CHILD(3,"子题"),
        ;
        private int code;
        private String name;
        @Override
        public int getKey() {
            return this.getCode();
        }

        @Override
        public String getValue() {
            return this.getName();
        }

        public boolean equals(QuestionPartTypeEnum questionPartTypeEnum) {
            return this.getCode() == questionPartTypeEnum.getCode();
        }

        public static QuestionPartTypeEnum create(BaseQuestion baseQuestion){
            if(null == baseQuestion){
                throw new BizException(ErrorResult.create(1001011,"试题不存在"));
            }
            if(baseQuestion.getMultiId().equals(0L)&&baseQuestion.getMultiFlag().equals(0)){
                return SINGLE;
            }else if(baseQuestion.getMultiId().equals(0L)&&baseQuestion.getMultiFlag().equals(1)){
                return COMPOSITE;
            }else if(baseQuestion.getMultiId()>0&&baseQuestion.getMultiFlag().equals(0)){
                return CHILD;
            }
            throw new BizException(ErrorResult.create(1001011,"无效的试题ID:"+baseQuestion.getId()));
        }
    }
    /**
     * 试题 - 残缺标识
     */
    @AllArgsConstructor
    @Getter
    public enum CompleteEnum implements EnumCommon {
        /**
         * 残缺
         */
        INCOMPLETE(1, "残缺", "标记残缺标识"),
        /**
         * 正常
         */
        COMPLETE(2, "正常", "取消残缺标识"),
        UNKNOWN_FLAG(-1, "非法", "非法参数"),;
        private int code;
        private String name;
        private String desc;

        @Override
        public int getKey() {
            return this.getCode();
        }

        @Override
        public String getValue() {
            return this.getName();
        }

        public boolean equals(CompleteEnum completeEnum) {
            return this.getCode() == completeEnum.getCode();
        }

        /**
         * 根据 code 获得枚举类型
         *
         * @param code
         * @return
         */
        public static CompleteEnum create(int code) {
            CompleteEnum[] enums = CompleteEnum.values();
            for (CompleteEnum completeEnum : enums) {
                if (completeEnum.getCode() == code) {
                    return completeEnum;
                }
            }
            return CompleteEnum.UNKNOWN_FLAG;
        }
    }

    /**
     * questionType - questionSaveType 映射关系
     */
    public static final Map<Integer, QuestionSaveTypeEnum> questionTypeToSaveMap = Maps.newHashMap();
    /**
     * questionType - questionDuplicateType 映射关系
     */
    public static final Map<Integer, QuestionDuplicateTypeEnum> questionTypeToDuplicateMap = Maps.newHashMap();

    static {
        questionTypeToSaveMap.putAll(Arrays.stream(QuestionTypeEnum.values()).collect(Collectors.toMap(
                i -> i.getKey(),
                i -> i.getQuestionSaveTypeEnum()
        )));
        questionTypeToDuplicateMap.putAll(Arrays.stream(QuestionTypeEnum.values()).collect(Collectors.toMap(
                i -> i.getKey(),
                i -> i.getDuplicateTypeEnum()
        )));
    }

    /**
     * 通过题型查询业务类型
     *
     * @param questionType
     * @return
     */
    public static QuestionSaveTypeEnum getSaveTypeByQuestionType(int questionType) {
        return questionTypeToSaveMap.getOrDefault(questionType, QuestionSaveTypeEnum.UNKNOWN_TYPE);
    }

    /**
     * 通过题型查询数据结构（存表）类型
     *
     * @param questionType
     * @return
     */
    public static QuestionDuplicateTypeEnum getDuplicateTypeByQuestionType(int questionType) {
        return questionTypeToDuplicateMap.getOrDefault(questionType, QuestionDuplicateTypeEnum.UNKNOWN_TYPE);
    }

    @Getter
    @AllArgsConstructor
    public enum SubjectQuestionTypeEnum{
        GWY_XINGCE(1L, Lists.newArrayList(QuestionTypeEnum.SINGLE,
                QuestionTypeEnum.MULTI,
                QuestionTypeEnum.INFINITIVE,
                QuestionTypeEnum.COMPOSITE,
                QuestionTypeEnum.JUDGE)),
        SYDW_ZHICE(3L, Lists.newArrayList(QuestionTypeEnum.SINGLE,
                QuestionTypeEnum.MULTI,
                QuestionTypeEnum.INFINITIVE,
                QuestionTypeEnum.COMPOSITE,
                QuestionTypeEnum.SUBJECTIVE,
                QuestionTypeEnum.JUDGE)),
        ZJ_XINGCE(100100173L,Lists.newArrayList(QuestionTypeEnum.SINGLE,
                QuestionTypeEnum.MULTI,
                QuestionTypeEnum.COMPOSITE)),
        DEFAULT(-1L,Lists.newArrayList(QuestionTypeEnum.SINGLE,
                QuestionTypeEnum.MULTI,
                QuestionTypeEnum.INFINITIVE,
                QuestionTypeEnum.JUDGE,
                QuestionTypeEnum.COMPOSITE,
                QuestionTypeEnum.SUBJECTIVE,
                QuestionTypeEnum.SHORT_ANSWER,
                QuestionTypeEnum.DISCRIMINATION,
                QuestionTypeEnum.MATERIAL_ANALYSIS,
                QuestionTypeEnum.SHORT_ESSAY,
                QuestionTypeEnum.TEACHING_DESIGN,
                QuestionTypeEnum.EXPLAIN_NOUNS,
                QuestionTypeEnum.BLANKS,
                QuestionTypeEnum.CASE_ANALYSIS,
                QuestionTypeEnum.DISCUSS,
                QuestionTypeEnum.ACTIVITY_DESIGN,
                QuestionTypeEnum.ANALYSIS_OF_TEACHING_SITUATIONS,
                QuestionTypeEnum.JUDGEMENT_REASONING
                )),

        ;
        private Long key;
        private List<QuestionTypeEnum> value;

        public static SubjectQuestionTypeEnum create(long subjectId){
            for (SubjectQuestionTypeEnum subjectQuestionTypeEnum : SubjectQuestionTypeEnum.values()) {
                if(subjectQuestionTypeEnum.getKey().longValue() == subjectId){
                    return  subjectQuestionTypeEnum;
                }
            }
            return DEFAULT;
        }
    }
}