package com.huatu.tiku.essay.essayEnum;


import java.util.List;

import com.google.common.collect.Lists;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-07-03 11:51 AM
 **/

public enum TemplateEnum {

    ;

    /**
     * 评语模版类型
     */
    @Getter
    @AllArgsConstructor
    public enum CommentTemplateEnum {
        DDPZ(1, "单个批注"),
        BTYJ(2, "本题阅卷"),
        ZHPJ(3, "综合评价"),
        KFX(4, "扣分项");


        private int type;

        private String value;
    }

    /**
     * @Code{LabelTypeEnum} 4 大题型与 CommentTemplateEnum 关联关系
     */
    @Getter
    @AllArgsConstructor
    public enum QuestionLabelEnum {

        XT(1, "小题", Lists.newArrayList(CommentTemplateEnum.DDPZ,
                CommentTemplateEnum.BTYJ, CommentTemplateEnum.KFX)),

        YYW(2, "应用文", Lists.newArrayList(CommentTemplateEnum.DDPZ,
                CommentTemplateEnum.BTYJ, CommentTemplateEnum.KFX)),

        YLW(3, "文章写作（议论文）", Lists.newArrayList(CommentTemplateEnum.DDPZ,
                CommentTemplateEnum.BTYJ)),

        TT(4, "套题", Lists.newArrayList(CommentTemplateEnum.ZHPJ));

        private int code;

        private String value;

        private List<CommentTemplateEnum> labels;

        /**
         * 获取每种题型所有的 type
         *
         * @param type
         * @return
         */
        public static List<CommentTemplateEnum> allLalel(int type) {
            for (QuestionLabelEnum questionLabelEnum : values()) {
                if (questionLabelEnum.code == type) {
                    return questionLabelEnum.getLabels();
                }
            }
            return Lists.newArrayList();
        }

        /**
         * @param type
         * @return
         */
        public static QuestionLabelEnum create(int type) {
            for (QuestionLabelEnum questionLabelEnum : values()) {
                if (questionLabelEnum.getCode() == type) {
                    return questionLabelEnum;
                }
            }
            return XT;
        }
    }

    /**
     * 评语业务类型
     */
    @Getter
    @AllArgsConstructor
    public enum BizTypeEnum {
        /**
         * 1论点（选中的评语需关联论点-关键句id）
         * 2论据（选中的评语需关联详细批改id）
         * 3其他（选中的评语，如有子评语，需关联子评语ID）
         */
        THESIS(1, "论点"),
        ARGUMENT(2, "论据"),
        REGULAR(3, "固定选择模式"),
        ;

        private int id;

        private String name;

        /**
         * 如果创建不成功，返回默认值 REGULAR
         * @param id
         * @return
         */
        public static BizTypeEnum create(int id){
            for(BizTypeEnum bizTypeEnum : values()){
                if(bizTypeEnum.getId() == id){
                    return bizTypeEnum;
                }
            }
            return REGULAR;
        }
    }


    /**
     * 批注类型（1单题详细批注 2单题总批注 3套题批注）
     */
    @Getter
    @AllArgsConstructor
    public enum LabelTypeEnum {
        QUESTION_DETAIL(1, "单题详细批注"),
        QUESTION_TOTAL(2, "单题总批注"),
        PAPER_TOTAL(3, "套题批注"),
        ;

        private int type;

        private String name;
    }
}
