package com.huatu.ztk.backend.paperUpload.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by lenovo on 2017/4/23.
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class WordElement {
    private int start;    //开始节点
    private String id;     //元素属性对应的唯一标识
    private String name;        //元素校验的正则结构
    private String content;         //标签内容
    private int end;      //结束节点

}
