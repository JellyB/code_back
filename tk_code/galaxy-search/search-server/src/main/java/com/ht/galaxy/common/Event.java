package com.ht.galaxy.common;

import java.util.List;

/**
 * @author gaoyuchao
 * @create 2018-07-04 17:03
 */
public class Event {

    private List<Type> list;

    public Event() {
    }

    public Event(List<Type> list) {
        this.list = list;
    }

    public List<Type> getList() {
        return list;
    }

    public void setList(List<Type> list) {
        this.list = list;
    }
}
