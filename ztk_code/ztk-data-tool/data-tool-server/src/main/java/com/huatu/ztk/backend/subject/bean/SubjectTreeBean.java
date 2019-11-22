package com.huatu.ztk.backend.subject.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by linkang on 3/3/17.
 */


@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class SubjectTreeBean {
    private int id;         //科目id
    private String name;    //科目名称

    private int status; //状态

    private int parent; //父节点id

    private List<SubjectTreeBean> childrens; //子节点列表

    private int type; //节点类型
}