package com.huatu.tiku.match.dao.manual.provider;

import org.apache.ibatis.annotations.Param;

/**
 * 模考试题统计信息查询 - 自定义sql
 * Created by huangqingpeng on 2019/05/15
 */
public class MatchQuestionMetaProvider {

    /**
     * 分页查询所有的试题统计信息
     * @param index
     * @param limit
     * @return
     */
    public String findByCursor(int index, int limit){
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ")
                .append(" * ")
                .append(" from ")
                .append(" match_question_meta ")
                .append(" WHERE ")
                .append(" id > ").append(index)
                .append(" limit ").append(limit);
        System.out.println("findCursor = | "+sb.toString());
        return sb.toString();
    }
}
