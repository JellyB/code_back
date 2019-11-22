package com.huatu.ztk.search.bean;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zhengyi
 * @date 2019-03-07 16:34
 **/
@Getter
@Setter
public class KeyWordSearchBeanNew extends KeywordSearchBean {
    private Option option;

    public static final String HOT_WORD_REDIS_KEY = "HOT_WORD_REDIS_KEY";

    public KeyWordSearchBeanNew() {
        super();
    }

    public enum Option {
        /**
         * OPTION
         */
        INSERT,
        DELETE,
        UPDATE;
        private String option;
    }

    public Object getRedisKey() {
        return HOT_WORD_REDIS_KEY + this.getUid() + this.getCatgory();
    }
}