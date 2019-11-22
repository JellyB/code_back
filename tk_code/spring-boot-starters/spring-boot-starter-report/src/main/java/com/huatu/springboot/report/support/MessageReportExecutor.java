package com.huatu.springboot.report.support;

import java.util.concurrent.Future;

/**
 * @author hanchao
 * @date 2018/1/11 16:15
 */
public interface MessageReportExecutor {
    void execute(Runnable runnable);
}
