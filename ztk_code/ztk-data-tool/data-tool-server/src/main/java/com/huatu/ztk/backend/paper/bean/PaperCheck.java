package com.huatu.ztk.backend.paper.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by aatrox on 2017/3/3.
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class PaperCheck {
    private int id;//试卷审核id
    private int paperId;//试卷id
    private long applyTime;//提交审核时间
    private long checkTime;//审核时间
    private long applierId;//提交审核人id
    private long checkId;//审核人id
    private String suggestion;//审核意见
    private int checkStatus;//审核状态   3 审核拒绝  5 审核中 6 审核通过
}
