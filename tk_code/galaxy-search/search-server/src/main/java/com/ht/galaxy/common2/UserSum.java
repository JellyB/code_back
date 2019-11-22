package com.ht.galaxy.common2;

/**
 * @author gaoyuchao
 * @create 2018-07-27 10:55
 */
public class UserSum {

    private int count;
    private String time;

    public UserSum() {
    }

    public UserSum(int count, String time) {
        this.count = count;
        this.time = time;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
