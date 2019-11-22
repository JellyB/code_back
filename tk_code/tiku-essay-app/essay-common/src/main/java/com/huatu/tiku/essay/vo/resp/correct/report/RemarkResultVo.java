package com.huatu.tiku.essay.vo.resp.correct.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/10
 * @描述
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RemarkResultVo {

    private String content;
    private Double score;
    private int sort;
    private int labelType;

    private List<RemarkResultVo> addRemarkList;

    private List<RemarkResultVo> deRemarkList;


}
