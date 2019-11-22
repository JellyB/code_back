package com.ht.base.common.area;

import java.util.ArrayList;
import java.util.List;

/**
 * 区域bean
 * Created by shaojieyue
 * Created time 2016-07-02 14:42
 */
public class Area {
    // add by hanchao,2017-08-24
    // 去掉对外访问的set方法以及set权限
    private int id;

    private String name;
    private int parentId;
    /**
     * global id
     */
    private int gid;
    /**
     * netschool id,mapping query course
     */
    private int cid;

    private List<Area> children;


    public Area(int id, String name, int parentId,int gid,int cid) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.cid = cid;
        this.children = new ArrayList(10);
    }

    public int getParentId() {
        return parentId;
    }

    public List<Area> getChildren() {
        return children;
    }

    /**
     * 限制外部包访问
     * @param children
     */
    void setChildren(List<Area> children) {
        this.children = children;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getGid() {
        return gid;
    }

    public int getCid() {
        return cid;
    }
}
