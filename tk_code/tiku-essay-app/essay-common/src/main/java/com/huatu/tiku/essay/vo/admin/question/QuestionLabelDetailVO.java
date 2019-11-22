package com.huatu.tiku.essay.vo.admin.question;

import com.huatu.tiku.essay.vo.admin.correct.LabelCommentRelationVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
@Builder
public class QuestionLabelDetailVO {

    /**
     * 批注ID
     */
    private Long id;
    /**
     * 批注对应的图片ID
     */
    private Long imageId;
    /**
     * 批注的文字内容
     */
    private String content;
    /**
     * 其他评语
     */
    private String elseRemark;
    /**
     * 批注所在图片坐标
     */
    private String imageAxis;
    /**
     * 图片上的整体坐标快照
     */
    private String imageAllAxis;
    /**
     * 携带批注标签的整体文字内容
     */
    private String labeledContent;
    /**
     * 用户试题答题卡ID
     */
    private Long questionAnswerCardId;
    /**
     * 批注ID
     */
    private Long totalId;
    /**
     * 模版式评语
     */
    private List<LabelCommentRelationVO> remarkList;

    /**
     * 试题ID(非必填项)
     */
    private Long questionId;
}
