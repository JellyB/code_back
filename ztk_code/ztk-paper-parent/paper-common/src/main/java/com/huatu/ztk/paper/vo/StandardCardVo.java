package com.huatu.ztk.paper.vo;

import com.huatu.ztk.paper.bo.PaperBo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 答题卡信息
 * @author shanjigang
 * @date 2019/2/25 16:52
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class StandardCardVo {
    /**
     * 用户答案
     */
    private String[] answers;

    /**
     * 用户答案是否正确
     */
    private int[] corrects;

    /**
     * 用户是否有疑问
     */
    private int[] doubts;

    /**
     * 试卷名称（阶段测试名称）
     */
    private String name;

    /**
     * 答题卡Id
     */
    private Long practiceId;
    
    /**
     * 答题卡id字符串
     */
    private String practiceIdStr;

    /**
     * 剩余时间
     */
    private int remainTime;

    /**
     * 用户耗时
     */
    private int[] times;

    /**
     * 试卷
     */
    private PaperBo paper;

    /**
     * 试卷状态
     */
    private Integer status;

    /**
     * 本次答题做到第几题
     */
    private int lastIndex;
}
