package com.huatu.ztk.backend.teacher.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: xuhuiqiang
 * Time: 2017-05-12  11:16 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class Teacher {
    private int id;
    private String name;
    private String avatar;//头像地址
    private String trait;//教学风格
    private String begood;//专注学科
    private String des;//教师简介
    private String createTime;//创建时间
    private int status;//教师状态，0为删除，1为正常
}
