package com.huatu.springboot.report.support;

import com.huatu.tiku.common.bean.report.ReportMessage;

/**
 * 可以考虑使用kafka等
 * @author hanchao
 * @date 2018/1/11 15:23
 */
public interface MessageReporter {
    void report(ReportMessage message);
}
