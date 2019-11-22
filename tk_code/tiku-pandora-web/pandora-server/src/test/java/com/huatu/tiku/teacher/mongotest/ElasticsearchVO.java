package com.huatu.tiku.teacher.mongotest;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author jbzm
 * @date 2018/7/20 2:20 PM
 **/
@Getter
@Setter
public class ElasticsearchVO {
    private int id;//id
    private int year;//试题年份
    private int area;//试题区域
    private int difficult;//难度系数
    private int subject;//科目
    private String from;//来源
    private String material;//材料
    private int mode;//题的模式 如：真题，模拟题
    private String stem;//题干
    private int percent;
    private String analysis;//解析
}