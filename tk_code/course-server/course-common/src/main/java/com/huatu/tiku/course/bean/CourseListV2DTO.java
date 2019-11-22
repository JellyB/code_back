package com.huatu.tiku.course.bean;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author hanchao
 * @date 2017/9/13 13:23
 */
@Data
public class CourseListV2DTO implements Serializable{
    private static final long serialVersionUID = 1L;


    private int next;
    private List<Map> result;

}
