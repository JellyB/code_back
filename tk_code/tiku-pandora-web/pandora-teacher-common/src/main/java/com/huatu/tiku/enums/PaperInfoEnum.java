package com.huatu.tiku.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * Created by lijun on 2018/8/2
 */
public enum PaperInfoEnum {
    ;

    /**
     * 试卷类型
     */
    @AllArgsConstructor
    @Getter
    public enum TypeInfo implements EnumCommon {
        ENTITY(1, "实体卷"),
        SIMULATION(2, "活动卷"),
        ASSEMBLY(3, "手工组卷"),;
        private Integer code;
        private String name;


        @Override
        public int getKey() {
            return this.code;
        }

        @Override
        public String getValue() {
            return this.name;
        }

        public static TypeInfo create(int code) {
            Optional<TypeInfo> info = Arrays.stream(TypeInfo.values())
                    .filter(typeInfo -> typeInfo.getCode().equals(code))
                    .findAny();
            return info.get();
        }

        public boolean equals(TypeInfo typeInfo) {
            return this.getCode() == typeInfo.getCode();
        }

        /**
         * 该类型试卷下的试题是否需要同步至mongoDB
         *
         * @return 需要同步返回true
         */
        public final static boolean isNeedImportToMongoDB(TypeInfo typeInfo) {
            return ENTITY.equals(typeInfo);
        }
    }


    /**
     * 试卷分类
     */
    @AllArgsConstructor
    @Getter
    public enum ModeEnum implements EnumCommon {
        TRUE_PAPER(1, "真题卷"),
        TEST_PAPER(2, "模拟卷");
        private Integer code;
        private String name;


        @Override
        public int getKey() {
            return this.code;
        }

        @Override
        public String getValue() {
            return this.name;
        }
    }


    /**
     * 试卷状态
     */
    @AllArgsConstructor
    @Getter
    public enum BizStatus implements EnumCommon {
        NO_PUBLISH(0, "未发布"),
        PUBLISH(1, "已发布"),;
        private Integer code;
        private String name;


        @Override
        public int getKey() {
            return this.code;
        }

        @Override
        public String getValue() {
            return this.name;
        }
    }

    /**
     * 组卷类型
     */
    @AllArgsConstructor
    @Getter
    public enum PaperAssemblyType implements EnumCommon {
        MANUAL(1, "手工组卷"),
        AUTOMATIC(2, "自动组卷"),;

        private Integer code;
        private String name;


        @Override
        public int getKey() {
            return this.code;
        }

        @Override
        public String getValue() {
            return this.name;
        }
    }

    /**
     * 活动卷搜索类型
     */
    @AllArgsConstructor
    @Getter
    public enum SearchTypeEnum implements EnumCommon {

        SEARCH_ID(1, "名称搜索"),
        SEARCH_NAME(2, "ID搜索");

        private Integer code;
        private String content;

        public int getKey() {
            return this.code;
        }

        public String getValue() {
            return this.content;
        }
    }


}
