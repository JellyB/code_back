package com.ht.galaxy.common;

public class activeSum {

    private Long time;
    private int count;

    public activeSum() {
    }

    public activeSum(Long time, int count) {
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
