package com.ht.galaxy.common2;

/**
 * @author gaoyuchao
 * @create 2018-07-03 18:17
 */
public class UserCountTerminal {

    private int terminal;
    private int count;
    private String time;

    public UserCountTerminal() {
    }

    public UserCountTerminal(int terminal, int count, String time) {
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
