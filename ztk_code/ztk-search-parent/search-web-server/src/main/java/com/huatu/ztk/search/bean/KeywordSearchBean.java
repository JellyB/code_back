package com.huatu.ztk.search.bean;

import lombok.Builder;
import lombok.Data;

/**
 * 关键字搜索
 */
@Data
@Builder
public class KeywordSearchBean {


    private long id;//id
    private long uid;//用户id
    private int catgory; // 考试科目
    private String keyword;//关键字
    private int count; //搜索次数
    private long updateTime; // 更新时间

    public KeywordSearchBean() {
    }

    public KeywordSearchBean(long id, long uid, int catgory, String keyword, int count, long updateTime) {
        this.id = id;
        this.uid = uid;
        this.catgory = catgory;
        this.keyword = keyword;
        this.count = count;
        this.updateTime = updateTime;
    }
}