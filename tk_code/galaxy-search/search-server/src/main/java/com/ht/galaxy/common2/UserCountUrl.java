package com.ht.galaxy.common2;

/**
 * @author gaoyuchao
 * @create 2018-07-03 18:17
 */
public class UserCountUrl {

    private String url;
    private int count;
    private String time;

    public UserCountUrl() {
    }

    public UserCountUrl(String url, int count, String time) {
        this.url = url;
        this.count = count;
        this.time = time;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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
