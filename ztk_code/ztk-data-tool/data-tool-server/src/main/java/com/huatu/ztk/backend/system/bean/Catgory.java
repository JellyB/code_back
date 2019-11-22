package com.huatu.ztk.backend.system.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Author: xuhuiqiang
 * Time: 2017-04-26  20:23 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class Catgory {
    private int id;         //考试类型id
    private String name;    //考试类型名称
    private long createBy;   //创建人id
    private Date createTime;//创建时间
    private int status; //状态
    private int lookup;//查看权限，1为能查看该考试类型下的所有试卷，0为只能查看自己创建的试卷
    private int isBelong;//是否属于某个角色
}
