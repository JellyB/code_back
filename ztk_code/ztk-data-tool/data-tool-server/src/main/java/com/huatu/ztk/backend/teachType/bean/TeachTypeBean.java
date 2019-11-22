package com.huatu.ztk.backend.teachType.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by linkang on 3/3/17.
 */


@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class TeachTypeBean {
    private int id;         //教研题型类型id
    private String name;    //教研题型名称
    private int subject;    //考试科目
    private long createBy;   //创建人id
    private Date createTime;//创建时间
    private int status; //状态
}