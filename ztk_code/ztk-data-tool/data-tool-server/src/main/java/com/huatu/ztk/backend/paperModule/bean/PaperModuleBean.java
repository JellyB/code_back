package com.huatu.ztk.backend.paperModule.bean;

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
public class PaperModuleBean {
    private int id;         //模块id
    private String name;    //模块名称
    private String description; //描述
    private long createBy;   //创建人id
    private Date createTime;//创建时间
    private int subject;
    private int status; //状态
}