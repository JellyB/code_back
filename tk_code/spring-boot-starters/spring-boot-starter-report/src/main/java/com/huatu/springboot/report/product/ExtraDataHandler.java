package com.huatu.springboot.report.product;

import com.huatu.tiku.common.bean.report.ReportMessage;

/**
 * @author hanchao
 * @date 2018/1/15 13:23
 */
public interface ExtraDataHandler {
    Object extra(ReportMessage reportMessage);
}
