package com.huatu.tiku.match.dao.manual.provider;

import org.apache.ibatis.annotations.Param;

/**
 * 模考统计信息查询 - 自定义sql
 * Created by huangqingpeng on 2018/12/23.
 */
public class MatchUserMetaProvider {

    /**
     * 用户每个人的分数
     * @param paperId
     * @return
     */
    public String findMatchUserScoreByPaperId(@Param("paperId") int paperId){
        String sql = " SELECT " +
                " practice_id as practiceId, " +
                " score " +
                " FROM " +
                " match_user_meta " +
                " WHERE " +
                " match_id = " + paperId +
                " AND `status` = 1";
        return sql;
    }

    /**
     * 用户最大分数
     * @param paperId
     * @return
     */
    public String findMatchUserMaxScore(@Param("paperId") int paperId){
        String sql = "select max(score) from match_user_meta where match_id = " + paperId +
                " And `status` = 1";
        return sql;
    }

    /**
     * 模考大赛分数累加值
     * @param paperId
     * @return
     */
    public String findMatchUserSumScore(@Param("paperId") int paperId){
        String sql = "select SUM(score) from match_user_meta where match_id = " + paperId +
                " And `status` = 1";
        return sql;
    }

    /**
     * 测试专用
     * @return
     */
    public String findUserIds(){
        String sql = "select user_id  from match_user_meta limit 1000";
        return sql;
    }


    /**
     * 分页查询所有的用户统计信息
     * @param index
     * @param limit
     * @return
     */
    public String findByCursor(int index, int limit){
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ")
                .append(" * ")
                .append(" from ")
                .append(" match_user_meta ")
                .append(" WHERE ")
                .append(" id > ").append(index)
                .append(" limit ").append(limit);
        System.out.println("findCursor = | "+sb.toString());
        return sb.toString();
    }
}
