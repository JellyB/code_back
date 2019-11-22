package com.base.model;

import com.base.enums.UsingTypeEnum;

import java.io.Serializable;
import java.util.Date;

/**
 * 所有类的父类- 序列化
 * Created by junli on 2017/9/1.
 */
public abstract class RootObject implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private Date createTime;
    private Date modifyTime;
    private Integer usingType;

    public RootObject() {
        this.usingType = UsingTypeEnum.in_using.getCode();
        this.setModifyTime();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
        if (this.id == null) {
            this.createTime = modifyTime;
        }
    }

    public void setModifyTime() {
        this.setModifyTime(new Date());
    }


    public Integer getUsingType() {
        return usingType;
    }

    public void setUsingType(Integer usingType) {
        this.usingType = usingType;
    }
}
