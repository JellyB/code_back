package com.huatu.ztk.paper.vo;

import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.paper.bo.PaperBo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author shanjigang
 * @date 2019/3/4 11:49
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class PeriodTestReportVo {
    /**
     * 用户答案
     */
    private String[] answers;

    /**
     * 平均分
     */
    private int averageScore;

    /**
     * 击败比例
     */
    private int beatRate;
    /**
     * 用户答案是否正确
     */
    private int[] corrects;

    /**
     * 用户是否有疑问
     */
    private int[] doubts;

    /**
     * 总耗时
     */
    private int expendTime;

    /**
     * 最高分
     */
    private int maxScore;

    /**
     * 试卷名称（阶段测试名称）
     */
    private String name;

    /**
     * 答题卡Id
     */
    private Long practiceId;

    /**
     * 试题数量
     */
    private int qcount;

    /**
     * 知识点信息
     */
    private List<QuestionPointTree> questionPointTrees;

    /**
     * 总做题量
     */
    private int rNum;

    /**
     * 排名
     */
    private long rank;

    /**
     * 剩余时间
     */
    private int remainTime;

    /**
     * 统计更新时间
     */
    private long reportTime;

    /**
     * 试题总分数
     */
    private Integer score;

    /**
     * 交卷时间
     */
    private long submitTime;

    /**
     * 老师评语
     */
    private TeacherRemarkVo teacherRemark;

    /**
     * 用户耗时(每道题的耗时)
     */
    private int[] times;

    /**
     * 练习类型
     */
    private String typeInfo;

    /**
     * 未做题量
     */
    private int unum;

    /**
     * 用户得分
     */
    private int userScore;

    /**
     * 做错题量
     */
    private int wnum;

    /**
     * 试卷
     */
    private PaperBo paper;

    /**
     * 是否可以查看完整报告
     * 在规定时间内作答可查看完整报告 true
     * 考试结束后作答的只能查看部分报告 false
     */
    private Boolean isViewAllReport;

    /**
     * 平均每道题用时
     */
    private Integer averageTime;

    /**
     * 做题正确率
     */
    private Integer accuracy;
    
    /**
     * 时间是否有效（无效时不需要弹出提示）
     */
    private Integer startTimeIsEffective;
}
