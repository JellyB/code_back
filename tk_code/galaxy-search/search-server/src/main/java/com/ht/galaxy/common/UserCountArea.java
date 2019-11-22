package com.ht.galaxy.common;



/**
 * @author gaoyuchao
 * @create 2018-07-03 18:16
 */

public class UserCountArea {

    private int area;
    private int count;
    private String time;

    public UserCountArea() {
    }

    public UserCountArea(int area, int count, String time) {
        this.area = area;
        this.count = count;
        this.time = time;
    }

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
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
