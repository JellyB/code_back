package com.ht.galaxy.common;

/**
 * @author gaoyuchao
 * @create 2018-07-04 15:40
 */
public class UserCount {

    private int count;
    private String time;

    public UserCount() {
    }

    public UserCount(int count, String time) {
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
