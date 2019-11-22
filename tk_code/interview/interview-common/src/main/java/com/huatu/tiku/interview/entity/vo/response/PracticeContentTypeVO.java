package com.huatu.tiku.interview.entity.vo.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by x6 on 2018/4/11.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PracticeContentTypeVO {
    //id
    private long id;
    //名称
    private String name;
    //子模块
    private List<PracticeContentTypeVO> subList;

    //题目列表
    private List<Object>  questionList;

}
