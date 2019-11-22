package com.huatu.tiku.interview.constant;

/**
 * Created by x6 on 2018/1/17.
 */
public enum ReportTypeConstant {
    DAILY_REPORT(0),
    TOTAL_REPORT(1);


    ReportTypeConstant(int code) {
        this.code = code;
    }
    private int code;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
