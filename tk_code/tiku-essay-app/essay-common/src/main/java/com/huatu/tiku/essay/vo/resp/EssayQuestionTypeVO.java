package com.huatu.tiku.essay.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhouwei
 * @Description: 题目类型VO
 * @create 2017-12-16 下午2:33
 **/
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EssayQuestionTypeVO implements Serializable{
    long id;

    //题目类型名称
    private String name;

    //优先级
    private int sort;

    //上级id
    private long pid;

    //下级题目类型
    private List<EssayQuestionTypeVO> subList;
}
