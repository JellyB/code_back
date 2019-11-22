package com.huatu.ztk.paper.service.v4.impl;

import org.springframework.data.redis.core.ZSetOperations;

/**
 * Created by huangqingpeng on 2019/3/7.
 */
public class ScoreSortUtil {

    public static Long getTotal(ZSetOperations<String, String> zSetOperations, String paperPracticeIdSore) {
        Long total = zSetOperations.size(paperPracticeIdSore);//总记录数
        if (total == null || total == 0) {//不存在总记录数，正常来说应该不存在
            total = 1L;
        }
        return total;
    }

    public static Long getRank(ZSetOperations<String, String> zSetOperations, String id, String paperPracticeIdSore, Long total) {
        //本答题卡排名,redis rank命令,从0开始,也就是第一名的rank值为0
        Long rank = zSetOperations.reverseRank(paperPracticeIdSore, id + "");//排名,按照分数倒排
        if (rank == null) {//排名不存在
            rank = total;//不存在说明是最后一名
        } else {
            rank = rank + 1;//排名是从0开始的所以要+1才是真的名次
        }
        return rank;
    }
}
