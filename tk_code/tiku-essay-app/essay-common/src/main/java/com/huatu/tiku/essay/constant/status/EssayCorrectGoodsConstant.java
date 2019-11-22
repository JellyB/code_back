package com.huatu.tiku.essay.constant.status;

import com.google.common.collect.Lists;
import com.huatu.tiku.essay.entity.correct.CorrectOrderSnapshot;
import com.huatu.tiku.essay.essayEnum.CorrectModeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

public class EssayCorrectGoodsConstant {
    //操作类型（不是商品状态）
    public static final Integer ON_LINE = 0;//商品上线
    public static final Integer OFF_LINE = 1;//商品下线s
    public static final Integer DELETE = 2;//删除商品


    public  enum CorrectGoodsBizStatusEnum {

        SELLING(1, "上线"), OFFLINE(0, "下线");

        private CorrectGoodsBizStatusEnum(int bizStatus, String description) {
            this.bizStatus = bizStatus;
            this.description = description;
        }

        private int bizStatus;
        private String description;

        public int getBizStatus() {
            return bizStatus;
        }

        public void setBizStatus(int bizStatus) {
            this.bizStatus = bizStatus;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }




    public   enum CorrectGoodsStatusEnum {
        NORMAL(1, "正常"), DELETED(-1, "已删除");

        private CorrectGoodsStatusEnum(int status, String description) {
            this.status  = status;
            this.description = description;
        }

        private int status;
        private String description;

        public int getStatus() {
            return status;
        }



        public void setStatus(int status) {
            this.status = status;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

    }


    /**
     * 批改商品枚举类型
     */
    @Getter
    @AllArgsConstructor
    public enum GoodsTypeEnum{

        QUESTION_SINGLE_INTELLIGENCE(0,"标准答案批改-智能",CorrectTypeEnum.INTELLIGENCE),
        PAPER_INTELLIGENCE(1,"套题批改-智能",CorrectTypeEnum.INTELLIGENCE),
        ARGUMENT_INTELLIGENCE(2,"议论文批改-智能",CorrectTypeEnum.INTELLIGENCE),
        QUESTION_SINGLE_MANUAL(3,"标准答案批改-人工",CorrectTypeEnum.MANUAL),
        PAPER_MANUAL(4,"套题批改-人工",CorrectTypeEnum.MANUAL),
        ARGUMENT_MANUAL(5,"议论文批改-人工",CorrectTypeEnum.MANUAL),
        ;
        private int type;
        private String name;
        private CorrectTypeEnum correctTypeEnum;

        public static GoodsTypeEnum create(int type){
            for (GoodsTypeEnum value : GoodsTypeEnum.values()) {
                if(value.getType() == type){
                    return value;
                }
            }
            return null;
        }

        /**
         * 获取商品批改类型
         * @param type      申论前端类型
         * @param correctMode
         * @return
         */
        public static GoodsTypeEnum create(int type,int correctMode){
            if(correctMode == CorrectModeEnum.MANUAL.getMode()){
                type = type + 3;
            }
            return create(type);
        }

        /**
         * type correctMode 大于 0 返回 获取商品批改类型
         * @param type
         * @param correctMode
         * @return
         */
        public static List<GoodsTypeEnum> createDefaultNull(int type, int correctMode){
            List<GoodsTypeEnum> result = Lists.newArrayList();
            if(type > -1 && correctMode > 0){
                result.add(create(type, correctMode));
            }else if(correctMode == 0 && type > -1){
                if(type == 0){
                    result.add(GoodsTypeEnum.QUESTION_SINGLE_INTELLIGENCE);
                    result.add(GoodsTypeEnum.QUESTION_SINGLE_MANUAL);
                }else if(type == 1){
                    result.add(GoodsTypeEnum.PAPER_INTELLIGENCE);
                    result.add(GoodsTypeEnum.PAPER_MANUAL);
                }else{
                    result.add(GoodsTypeEnum.ARGUMENT_INTELLIGENCE);
                    result.add(GoodsTypeEnum.ARGUMENT_MANUAL);
                }
            }
            return result;
        }
        /**
         * 单题类型转换为商品类型
         * @param questionType      0表示套卷1-5代表单题（5=议论文）
         * @param correctMode
         * @return
         */
        public static GoodsTypeEnum getGoodsType(int questionType, int correctMode) {
            //智能
            if(correctMode == CorrectModeEnum.INTELLIGENCE.getMode()) {
                if(questionType == 0) {
                    //为套卷
                    return GoodsTypeEnum.PAPER_INTELLIGENCE;
                }
                switch (questionType){
                    case 5:
                        return GoodsTypeEnum.ARGUMENT_INTELLIGENCE;
                    default:
                        return GoodsTypeEnum.QUESTION_SINGLE_INTELLIGENCE;
                }
            }else {
                if(questionType == 0) {
                    //为套卷
                    return GoodsTypeEnum.PAPER_MANUAL;
                }
                switch (questionType){
                    case 5:
                        return GoodsTypeEnum.ARGUMENT_MANUAL;
                    default:
                        return GoodsTypeEnum.QUESTION_SINGLE_MANUAL;
                }
            }
        }
    }

    @AllArgsConstructor
    @Getter
    public enum CorrectTypeEnum {
        INTELLIGENCE(1, "智能批改"),
        MANUAL(2, "人工批改"),
        ;

        private int type;
        private String name;

        public static CorrectTypeEnum of(Integer value) {
            for (CorrectTypeEnum correctTypeEnum : values()) {
                if (correctTypeEnum.getType() == value) {
                    return correctTypeEnum;
                }
            }

            return null;
        }
    }

    @Getter
    @AllArgsConstructor
    public enum GoodsExpireFlagEnum{
        LIMITED(1, "有有效期"),
        UN_LIMITED(0, "无有效期"),
        ;

        private int type;
        private String name;
    }
}
