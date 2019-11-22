package com.huatu.ztk.backend.subject.bean;

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
public class SubjectBean {
    private int id;         //科目id
    private String name;    //科目名称
    private int catgory;    //考试类型
    private long createBy;   //创建人id
    private Date createTime;//创建时间
    private int status; //状态
}