package com.huatu.ztk.commons;

import java.io.Serializable;
import java.util.List;

/**
 * 区域bean
 * Created by shaojieyue
 * Created time 2016-07-02 14:42
 */
public class Area implements Serializable{
    private static final long serialVersionUID = 1L;
    private int id;
    private String name;
    private int parentId;
    private List<Area> children;

    public Area(int id, String name, int parentId) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public List<Area> getChildren() {
        return children;
    }

    public void setChildren(List<Area> children) {
        this.children = children;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
