package com.huatu.tiku.match.bo.paper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 试题信息 - bo
 * Created by lijun on 2019/1/2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class QuestionSimpleBo implements Serializable {

    private Integer id;

    /**
     * 父类ID
     */
    private Integer parentId;

    /**
     * 类型
     */
    private Integer type;

    /**
     * 类型名称
     */
    private String teachType;

    /**
     * 材料
     */
    private List<String> materialList;

    /**
     * 题目
     */
    private String stem;

    /**
     * 模块名称
     */
    private String moduleName;

}
