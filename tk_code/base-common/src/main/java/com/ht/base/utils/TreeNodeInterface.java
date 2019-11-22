package com.ht.base.utils;

/**
 * Created by lijun on 2018/5/16
 */
public interface TreeNodeInterface {

    Integer getTreeId();

    default String getTreeCode(){
        return "0";
    }

    String getTreeTitle();

    Integer getTreeParentId();

    default Integer getSortNum() {
        return 0;
    }
}
