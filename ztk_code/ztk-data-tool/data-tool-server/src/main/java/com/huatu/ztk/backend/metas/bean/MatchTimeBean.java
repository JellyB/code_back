package com.huatu.ztk.backend.metas.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by huangqp on 2018\3\28 0028.
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class MatchTimeBean {
    private long id;
    //模考大赛
    private int paperId;
    //用户维度
    private long userId;
    //模块id
    private int moduleId;
    //模块名称
    private String moduleName;
    //题目数量
    private int questionNum;
    //题目耗时
    private int time;
}
