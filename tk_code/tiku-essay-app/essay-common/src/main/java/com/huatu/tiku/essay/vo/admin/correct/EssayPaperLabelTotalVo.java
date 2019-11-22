package com.huatu.tiku.essay.vo.admin.correct;

import com.huatu.tiku.essay.vo.resp.correct.report.RemarkResultVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/3
 * @描述 试卷标签表
 */

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class EssayPaperLabelTotalVo {

    /**
     * 套卷批注id
     */
    private long id;

    /**
     * 试卷答题卡id
     */
    private long paperAnswerCardId;

    /**
     * 试卷ID
     */
    private long paperId;

    /**
     * 其他批注
     */
    private String elseRemark;

    /**
     * 音频地址
     */
    private int audioId;

    /**
     * 评语信息
     */
    private List<LabelCommentRelationVO> remarkList;

    /**
     * 试卷得分,需要保存到答题卡
     */
    private Double paperScore;

    private List<RemarkResultVo> addRemarkList;

}
