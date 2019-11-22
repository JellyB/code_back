package com.huatu.tiku.common.bean.report;

/**
 * @author hanchao
 * @date 2018/1/11 15:02
 */
public abstract class ReportMessage {
    private String application;
    private String host;
    private long timestamp;//时间戳，上报时间

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

}
