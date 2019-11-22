package com.huatu.tiku.essay.vo.admin.question;

import com.huatu.tiku.essay.entity.correct.CorrectImage;
import com.huatu.tiku.essay.vo.admin.correct.CorrectImageVO;
import com.huatu.tiku.essay.vo.admin.correct.CorrectOrderSimpleVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author huangqingpeng
 * @title: QuestionLabelVO
 * @description: 单题基础信息返回内容
 * @date 2019-07-0917:01
 */
@AllArgsConstructor
@Data
@NoArgsConstructor
public class QuestionLabelVO {
    /**
     * 试题信息
     */
    private long questionBaseId;

    private long questionDetailId;
    /**
     * 总批注信息
     */
    private long totalId;
    /**
     * TODO 总批注是否完成(EssayLabelBizStatusEnum)（维护）
     */
    private long bizStatus;
    /**
     * 批改方式（correctModeEnum）
     */
    private int correctMode;

    private String correctModeName;

    /**
     * TODO 批注用时（维护）
     */
    private long spendTime;

    /**
     * 批注图片信息
     */
    private List<CorrectImageVO> imageInfoList;

    /**
     * 所属详细批注信息
     */
    private List<QuestionLabelDetailSimpleVO> detailList;

    /**
     * 当前批注总内容
     */
    private String labelContent;

    /**
     * 答题卡内容（原始答案，计算字数等）
     */
    private String content;

    /**
     * 抄袭度
     */
    private Double copyRatio;

    /**
     * 老师订单信息
     */
    private CorrectOrderSimpleVO orderInfo;
}
