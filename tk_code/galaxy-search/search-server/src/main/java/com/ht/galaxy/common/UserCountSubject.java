package com.ht.galaxy.common;

/**
 * @author gaoyuchao
 * @create 2018-07-03 18:17
 */
public class UserCountSubject {

    private int subject;
    private int count;
    private String time;

    public UserCountSubject() {
    }

    public UserCountSubject(int subject, int count, String time) {
        this.subject = subject;
        this.count = count;
        this.time = time;
    }

    public int getSubject() {
        return subject;
    }

    public void setSubject(int subject) {
        this.subject = subject;
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
