package com.huatu.tiku.essay.vo.admin.question;

import com.huatu.tiku.essay.vo.admin.correct.LabelCommentRelationVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author huangqingpeng
 * @title: QuestionLabelTotalVO
 * @description: 单题阅卷批注返回数据
 * @date 2019-07-0816:15
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class QuestionLabelTotalVO {

    /**
     * 批注ID
     */
    private Long id;
    /**
     * 答题卡ID
     */
    private Long questionAnswerCardId;
    /**
     * 阅卷得分
     */
    private Double score;
    /**
     * 音频URL
     */
    private int audioId;
    /**
     * 其他自定义评语
     */
    private String elseRemark;
    /**
     * 批注耗时
     */
    private Long spendTime;
    /**
     * 扣分项评语
     */
    private List<LabelCommentRelationVO> deRemarkList;
    /**
     * 得分项评语
     */
    private List<LabelCommentRelationVO> remarkList;
    /**
     * 试题ID(非必填)
     */
    private Long questionId;
    /**
     * 试题总分（展示用）
     */
    private Double totalScore;
    /**
     * 抄袭度（展示用）
     */
    private Double copyRatio;

    private Integer inputWordNumMax;

    private Integer inputWordNumMin;
    /**
     * 字数得分
     */
    private String wordNumScore;
    /**
     * ArticleLevelEnum  几类文
     */
    private Integer articleLevel;
}
