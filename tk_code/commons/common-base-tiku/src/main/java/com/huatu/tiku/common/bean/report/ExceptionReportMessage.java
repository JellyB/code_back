package com.huatu.tiku.common.bean.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * gw抛出的异常信息，服务待确定
 * @author hanchao
 * @date 2018/1/11 15:00
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExceptionReportMessage extends ReportMessage {
    private String exception;
    private String message;
    private String stacktrace;

    //请求信息，方便异常排查
    private String url;
    private String urlParameters;
    private String method;
    private String body;
    private Map<String,List<String>> requestHeaders;
}
