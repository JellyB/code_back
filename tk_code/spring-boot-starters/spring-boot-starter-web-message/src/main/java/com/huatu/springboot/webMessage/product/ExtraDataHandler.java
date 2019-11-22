package com.huatu.springboot.webMessage.product;

import com.huatu.tiku.common.bean.report.ReportMessage;

/**
 * Created by junli on 2018/4/10.
 */
public interface ExtraDataHandler {
    Object extra(ReportMessage reportMessage);
}
