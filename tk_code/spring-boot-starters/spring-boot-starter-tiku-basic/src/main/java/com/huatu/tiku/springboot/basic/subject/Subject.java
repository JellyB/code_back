package com.huatu.tiku.springboot.basic.subject;

import java.io.Serializable;
import java.util.List;

/**
 * 隐藏set方法后，只有jackson可以设置属性
 * @author hanchao
 * @date 2017/10/6 9:46
 */
public class Subject implements Serializable{
    private static final long serialVersionUID = 1L;
    private int id;
    private String name;
    private Subject parent;
    private List<Subject> childrens;
    private boolean tiku;


    public int getId() {
        return id;
    }

    void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    public Subject getParent() {
        return parent;
    }

    void setParent(Subject parent) {
        this.parent = parent;
    }

    public boolean isTiku() {
        return tiku;
    }

    void setTiku(boolean tiku) {
        this.tiku = tiku;
    }

    public List<Subject> getChildrens() {
        return childrens;
    }

    void setChildrens(List<Subject> childrens) {
        this.childrens = childrens;
    }

    public static class Type {
        //最早的分类节点
        public static final int CATEGORY = 1;
        //最底级的具体科目节点
        public static final int SUBJECT = 2;
        //历史原因，用这个来连接上面的信息，最终成为一棵树
        public static final int INFO = 3;
    }


}
