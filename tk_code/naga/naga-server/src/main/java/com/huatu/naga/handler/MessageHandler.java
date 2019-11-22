package com.huatu.naga.handler;

import com.huatu.tiku.common.bean.report.ReportMessage;

/**
 * @author hanchao
 * @date 2018/3/16 10:03
 */
public interface MessageHandler {
    boolean supports(ReportMessage message);
    void handleMessage(ReportMessage message);
}
