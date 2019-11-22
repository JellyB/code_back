package com.huatu.tiku.essay.vo.resp;

import com.huatu.tiku.essay.vo.resp.correct.report.RemarkVo;
import lombok.Data;

import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/8/30
 * @描述
 */
@Data
public class ManualCorrectReportVo {


    private Integer audioId;

    private String audioToken;

    private List<RemarkVo> remarkList;

    private int feedBackStatus;

    private int feedBackStar;

    private String feedBackContent;


}

