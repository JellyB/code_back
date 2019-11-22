package com.huatu.tiku.course.common;


/**
 * 运单状态描述
 */
public enum ExpressStatus {
    SENDING(0, "配送中"),
    GETTING(1, "已揽件"),
    PROBLEM(2, "疑难问题件"),
    RECIEVED(3, "已签收"),
    REJECT(4, "拒签已退回"),
    DELIVERY(5, "正在派件"),
    INBACK(6, "正在退件");

    private final Integer code;
    private final String des;

    private ExpressStatus(int code, String des) {
        this.code = code;
        this.des = des;
    }

    public static ExpressStatus getByCode(Integer code) {
        for (ExpressStatus op : ExpressStatus.values()) {
            if (op.getCode().equals(code)) {
                return op;
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public String getDes() {
        return des;
    }

}
