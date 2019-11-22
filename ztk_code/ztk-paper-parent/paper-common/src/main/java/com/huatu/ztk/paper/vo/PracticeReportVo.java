package com.huatu.ztk.paper.vo;

import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.paper.bo.PaperBo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 随堂练习报告Vo
 * @author shanjigang
 * @date 2019/3/15 13:59
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class PracticeReportVo {
    /**
     * 用户答案
     */
    private String[] answers;

    /**
     * 平均用时
     */
    private int averageTime;

    /**
     * 班级平均答对数量
     */
    private int classAverageRcount;

    /**
     * 用户答案是否正确
     */
    private int[] corrects;

    /**
     * 用户是否有疑问
     */
    private int[] doubts;

    /**
     * 班级平均用时
     */
    private int classAverageTime;

    /**
     * 图币
     */
    private int coin;

    /**
     * 试题数量
     */
    private int tcount;

    /**
     * 作对题量
     */
    private int rcount;

    /**
     * 做错数量
     */
    private int wcount;

    /**
     * 未做数量
     */
    private int ucount;

    /**
     * 总耗时
     */
    private int timesTotal;

    /**
     * 试卷
     */
    private PaperBo paper;

    /**
     * 答题卡id
     */
    private String id;

    /**
     * 答题卡创建时间
     */
    private long submitTimeInfo;

    /**
     * 只返回三级汇总知识点
     */
    private List<QuestionPointTree> points;


}
