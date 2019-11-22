package com.huatu.tiku.match.bo.report;

import com.huatu.ztk.chart.Line;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-01-09 下午2:46
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportBo {

    private Line line;
    private List<ReportListBo> list;
}
