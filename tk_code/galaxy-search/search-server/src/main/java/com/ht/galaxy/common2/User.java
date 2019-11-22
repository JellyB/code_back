package com.ht.galaxy.common2;

/**
 * @author gaoyuchao
 * @create 2018-07-27 10:45
 */
public class User {

    private int terminal;
    private int count;
    private String time;

    public User() {
    }

    public User(int terminal, int count, String time) {
        this.terminal = terminal;
        this.count = count;
        this.time = time;
    }

    public int getTerminal() {
        return terminal;
    }

    public void setTerminal(int terminal) {
        this.terminal = terminal;
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
