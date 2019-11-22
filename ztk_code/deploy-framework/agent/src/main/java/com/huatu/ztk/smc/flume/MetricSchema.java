package com.huatu.ztk.smc.flume;

import com.google.common.base.Preconditions;

/**
 * User: shijinkui
 * Date: 12-7-19
 * Time: 上午11:20
 */
public class MetricSchema {
    private final String command = "put";
    private String prefix = "smc-";
    private String key;
    private int value;
    private String tags;
    private long timestamp;

    private final String format = "%s %s %d %d %s";

    public MetricSchema(String key, int value, String tags) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(value);
        Preconditions.checkNotNull(tags);
        this.key = key;
        this.value = value;
        this.tags = tags;
        this.timestamp = System.currentTimeMillis() / 1000;
    }

    public String getPrefix() {
        return prefix + key;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String toString() {
        return String.format(format, command, getPrefix(), timestamp, value, tags);
    }


}
