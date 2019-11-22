package com.ht.galaxy.common2;



/**
 * @author gaoyuchao
 * @create 2018-07-03 18:16
 */

public class UserCountRegfrom {

    private String regfrom;
    private int count;
    private String time;

    public UserCountRegfrom() {
    }

    public UserCountRegfrom(String regfrom, int count, String time) {
        this.regfrom = regfrom;
        this.count = count;
        this.time = time;
    }

    public String getRegfrom() {
        return regfrom;
    }

    public void setRegfrom(String regfrom) {
        this.regfrom = regfrom;
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
