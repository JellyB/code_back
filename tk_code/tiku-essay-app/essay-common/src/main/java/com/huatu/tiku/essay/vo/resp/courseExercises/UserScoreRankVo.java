package com.huatu.tiku.essay.vo.resp.courseExercises;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/8/26
 * @描述 课后作业 优秀成绩排名信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserScoreRankVo {

    //用户排名
    private Integer rank;

    //学员用户名
    private String userName;

    //得分
    private Double examScore;

    //用时
    private  Long spendTime;

    //提交时间
    private String submitTime;
    
    //用户头像
    private String avatar;
    
    //答题卡id
    private Long answerId;

}
