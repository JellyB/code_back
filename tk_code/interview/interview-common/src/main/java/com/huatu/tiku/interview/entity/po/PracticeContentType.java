package com.huatu.tiku.interview.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by x6 on 2018/4/11.
 * 练习内容类型
 */
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Table(name = "t_practice_content_type")
public class PracticeContentType extends BaseEntity{

    //名称
    private String name;
    //上级id(没有上级，默认是-1)
    private long pid;
    //优先级
    private Integer sort;


}
