package com.huatu.ztk.paper.bean;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 描述：
 *
 * @author biguodong
 *         Create time 2019-01-14 上午11:34
 **/

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class PaperVo {

    /**
     * 试卷id
     */
    private int matchId;

    /**
     * 大赛名称
     */
    private String name;

    /**
     * 总共多少人作答
     */
    private int answerCount;

    /**
     * 0 未完成 - 继续答题
     * 1 已完成 - 开始答题
     */
    private int answerStatus;

    /**
     * 待完成答题卡ID
     */
    private String practiceId;

    /**
     * 我的完成次数
     */
    private int completeCount;
    /**
     * 课程ID
     */
    private int courseId;
}
