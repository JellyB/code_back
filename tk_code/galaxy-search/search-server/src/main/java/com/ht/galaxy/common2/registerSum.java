package com.ht.galaxy.common2;

public class registerSum {

    private Long time;
    private int count;

    public registerSum() {
    }

    public registerSum(Long time, int count) {
        this.time = time;
        this.count = count;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
