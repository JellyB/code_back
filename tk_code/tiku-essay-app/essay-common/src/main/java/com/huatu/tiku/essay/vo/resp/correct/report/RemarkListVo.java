package com.huatu.tiku.essay.vo.resp.correct.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/22
 * @描述
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RemarkListVo {

    /**
     * 试卷～综合评价
     */
    private List<RemarkVo> paperRemarkList;
    /**
     * 试题～扣分项
     */
    private List<RemarkVo> deRemarkList;

    /**
     * 试题～本题阅卷
     */
    private List<RemarkVo> questionRemarkList;
    //详细的不需要这里看了
}
